package com.w_via.designsystem.bottomsheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w_via.R
import com.w_via.designsystem.appbuttons.CloseButton
import com.w_via.designsystem.appbuttons.ConfirmButton
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// VISIBLE_ITEMS deve ser ímpar para que CENTER_INDEX seja exato.
private const val VISIBLE_ITEMS = 5
private const val CENTER_INDEX = VISIBLE_ITEMS / 2   // = 2  (0-based, linha do meio)
private val ITEM_HEIGHT = 52.dp

// ─── Bottom Sheet principal ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerBottomSheet(
    date: LocalDate = LocalDate.now(),
    currentValue: Float? = null,
    minTime: Float? = null,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int, isNextDay: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var selectedHour by remember {
        val h = currentValue?.toInt()?.let { if (it >= 24) it - 24 else it }
            ?: if (minTime != null && minTime > 0f) (minTime.toInt() + 1) % 24
            else 10
        mutableStateOf(h)
    }
    var selectedMinute by remember {
        mutableStateOf(
            if (currentValue != null) ((currentValue % 1) * 60).roundToInt()
            else 0  // sempre minutos 00
        )
    }
    var isNextDay by remember {
        val rawHour = currentValue?.toInt() ?: 0
        mutableStateOf(
            when {
                rawHour >= 24 -> true
                minTime != null && minTime > 0f && (minTime.toInt() + 1) >= 24 -> true
                else -> false
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .width(48.dp)
                        .height(4.5.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        },
        shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp),
        tonalElevation = 0.dp
    ) {
        TimePickerContent(
            selectedHour = selectedHour,
            selectedMinute = selectedMinute,
            isNextDay = isNextDay,
            date = date,
            minTime = minTime,
            onHourOptionChanged = { option ->
                selectedHour = option.hour
                isNextDay = option.isNextDay
            },
            onMinuteChanged = { selectedMinute = it },
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            },
            onConfirm = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    val finalTime = selectedHour + selectedMinute / 60f
                    // isNextDay é tratado externamente via DateSelector
                    onConfirm(selectedHour, selectedMinute, isNextDay)
                }
            }
        )
    }
}

private data class HourOption(val hour: Int, val isNextDay: Boolean)

// ─── Conteúdo interno ─────────────────────────────────────────────────────────
@Composable
private fun TimePickerContent(
    selectedHour: Int,
    selectedMinute: Int,
    isNextDay: Boolean,
    date: LocalDate,
    minTime: Float?,
    onHourOptionChanged: (HourOption) -> Unit,
    onMinuteChanged: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val hourOptions = remember(minTime) {
        if (minTime == null || minTime <= 0f) {
            (0..23).map { HourOption(it, false) }
        } else {
            val mHour = minTime.toInt()
            val today = (mHour..23).map { HourOption(it, false) }
            val tomorrow = (0..23).map { HourOption(it, true) }
            today + tomorrow
        }
    }

    val currentHourOption = HourOption(selectedHour, isNextDay)
    val showNextDayOption = minTime != null && minTime > 0f

    val formatter = DateTimeFormatter.ofPattern("E, d MMM", Locale("pt", "BR"))
    val initialDateLabel = date.format(formatter)
    val nextDateLabel = date.plusDays(1).format(formatter)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Cabeçalho ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            CloseButton(
                size = 40.dp,
                onClose = {
                    onDismiss()
                }
            )

            Text(
                text = "Selecionar horário",
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
            )

            ConfirmButton(
                size = 40.dp,
                onClick = onConfirm
            )

        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(Modifier.height(8.dp))

        CustomDateSelector(
            initialDateLabel = initialDateLabel,
            nextDateLabel = nextDateLabel,
            isNextDay = isNextDay,
            showNextDayOption = showNextDayOption
        )
        Spacer(Modifier.height(8.dp))

        TimePreview(hour = selectedHour, minute = selectedMinute)

        Spacer(Modifier.height(16.dp))

        // ── Labels ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "HORA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "MINUTO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Pickers ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            SelectionHighlight()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GenericNumberPicker(
                    items = hourOptions,
                    initialValue = currentHourOption,
                    onItemSelected = onHourOptionChanged,
                    displayTransform = { "%02d".format(it.hour) },
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(ITEM_HEIGHT * VISIBLE_ITEMS),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ":",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                GenericNumberPicker(
                    items = (0..59).toList(),
                    initialValue = selectedMinute,
                    onItemSelected = onMinuteChanged,
                    displayTransform = { "%02d".format(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─── Preview do horário ────────────────────────────────────────────────────────
@Composable
private fun TimePreview(hour: Int, minute: Int) {
    val timeText = remember(hour, minute) { "%02d:%02d".format(hour, minute) }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            .padding(horizontal = 32.dp, vertical = 10.dp)
    ) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 4.sp
        )
    }
}

// ─── Highlight na linha CENTRAL ───────────────────────────────────────────────
@Composable
private fun SelectionHighlight() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ITEM_HEIGHT * VISIBLE_ITEMS)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = ITEM_HEIGHT * CENTER_INDEX)  // mesmo offset da lista
                .fillMaxWidth()
                .height(ITEM_HEIGHT)
                .clip(RoundedCornerShape(0.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
private fun <T> GenericNumberPicker(
    items: List<T>,
    initialValue: T,
    onItemSelected: (T) -> Unit,
    displayTransform: (T) -> String,
    modifier: Modifier = Modifier
) {
    val initialIndex = remember(initialValue, items) {
        items.indexOf(initialValue).coerceAtLeast(0)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Sem + CENTER_INDEX — o firstVisibleItemIndex JÁ É o item centralizado
    // porque o contentPadding empurra os itens para que o primeiro visível
    // fique exatamente no centro
    val highlightedIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    val isScrolling = listState.isScrollInProgress

    // Dispara quando scroll para
    LaunchedEffect(isScrolling) {
        if (!isScrolling) {
            items.getOrNull(highlightedIndex)?.let { onItemSelected(it) }
        }
    }

    // Dispara quando índice muda durante scroll
    LaunchedEffect(highlightedIndex) {
        items.getOrNull(highlightedIndex)?.let { onItemSelected(it) }
    }

    val backgroundColor = MaterialTheme.colorScheme.surface
    val fadeZone = ITEM_HEIGHT

    Box(modifier = modifier.height(ITEM_HEIGHT * VISIBLE_ITEMS)) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = ITEM_HEIGHT * CENTER_INDEX)
        ) {
            items(items.size) { index ->
                val isHighlighted = index == highlightedIndex

                val itemAlpha by animateFloatAsState(
                    targetValue = if (isHighlighted) 1f else 0.28f,
                    animationSpec = tween(150),
                    label = "alpha_$index"
                )
                val itemScale by animateFloatAsState(
                    targetValue = if (isHighlighted) 1f else 0.80f,
                    animationSpec = tween(150),
                    label = "scale_$index"
                )

                Box(
                    modifier = Modifier
                        .height(ITEM_HEIGHT)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayTransform(items[index]),
                        fontSize = if (isHighlighted) 26.sp else 20.sp,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                        color = if (isHighlighted)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            alpha = itemAlpha
                            scaleX = itemScale
                            scaleY = itemScale
                        }
                    )
                }
            }
        }

        // Fade topo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fadeZone)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(backgroundColor, backgroundColor.copy(alpha = 0f))
                    )
                )
        )

        // Fade base
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fadeZone)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(backgroundColor.copy(alpha = 0f), backgroundColor)
                    )
                )
        )
    }
}

@Composable
private fun CustomDateSelector(
    initialDateLabel: String,
    nextDateLabel: String,
    isNextDay: Boolean,
    showNextDayOption: Boolean
) {
    val activeLabel = if (isNextDay) nextDateLabel else initialDateLabel
    val passiveLabel = if (showNextDayOption && !isNextDay) nextDateLabel else null

    AnimatedContent(
        targetState = activeLabel,
        transitionSpec = {
            (slideInVertically { -it } + fadeIn(tween(300)))
                .togetherWith(slideOutVertically { it } + fadeOut(tween(200)))
                .using(SizeTransform(clip = false))
        },
        label = "DateSelector"
    ) { label ->
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(0.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (passiveLabel != null) {
                Icon(
                    painter = painterResource(R.drawable.ic_do),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = passiveLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
