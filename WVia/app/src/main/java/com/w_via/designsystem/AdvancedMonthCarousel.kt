package com.w_via.designsystem

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdvancedMonthCarousel(
    modifier: Modifier = Modifier,
    allowFutureMonths: Boolean = false,
    onDateSelected: ((year: Int, month: Int) -> Unit)? = null
) {
    val today = LocalDate.now()
    val currentYear = today.year
    val currentMonthIndex = today.monthValue - 1 // índice base 0 (0 = Janeiro, 11 = Dezembro)

    val monthNames = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    val itemWidth = 72.dp
    val itemSpacing = 8.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = currentMonthIndex
    )
    val snapBehavior = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center
    )

    val highlightedIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }?.index ?: currentMonthIndex
        }
    }

    LaunchedEffect(highlightedIndex) {
        onDateSelected?.invoke(currentYear, highlightedIndex + 1)
    }

    fun centerOffsetPx(viewportWidthPx: Int): Int {
        val itemWidthPx = with(density) { itemWidth.roundToPx() }
        return -((viewportWidthPx - itemWidthPx) / 2)
    }

    // Trava rolagem para meses futuros se allowFutureMonths for false
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!allowFutureMonths && available.x < 0 && highlightedIndex >= currentMonthIndex) return available
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!allowFutureMonths && available.x < 0 && highlightedIndex >= currentMonthIndex) return available
                return Velocity.Zero
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val viewportWidthPx = with(density) { maxWidth.roundToPx() }
        val itemWidthPx = with(density) { itemWidth.roundToPx() }
        val sidePadding = with(density) { ((viewportWidthPx - itemWidthPx) / 2).toDp() }

        LazyRow(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            contentPadding = PaddingValues(
                start = sidePadding,
                end = sidePadding,
                top = 8.dp,
                bottom = 16.dp
            )
        ) {
            items(monthNames.size) { index ->
                val isHighlighted = index == highlightedIndex
                val distance = abs(index - highlightedIndex)
                val scale = (1f - distance * 0.13f).coerceAtLeast(0.68f)
                val alpha = (1f - distance * 0.22f).coerceAtLeast(0.30f)

                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            if (isHighlighted) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .border(
                            width = if (isHighlighted) 2.dp else 1.dp,
                            color = if (isHighlighted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .clickable {
                            val target = if (allowFutureMonths) index else index.coerceAtMost(currentMonthIndex)
                            scope.launch {
                                listState.animateScrollToItem(
                                    index = target,
                                    scrollOffset = centerOffsetPx(viewportWidthPx)
                                )
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = monthNames[index].take(3).uppercase(),
                            fontSize = if (isHighlighted) 15.sp else 13.sp,
                            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                            color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = currentYear.toString(),
                            fontSize = 10.sp,
                            color = if (isHighlighted) Color.White.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}