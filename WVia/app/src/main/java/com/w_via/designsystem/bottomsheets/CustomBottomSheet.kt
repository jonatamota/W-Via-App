package com.w_via.designsystem.bottomsheets

import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.w_via.designsystem.appbuttons.CloseButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ANIM_DURATION = 320

@Composable
fun CustomBottomSheet(
    onDismissRequest: () -> Unit,
    title: String? = null,
    colorContent: Color = MaterialTheme.colorScheme.surface,
    showCloseButton: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Dispara o dismiss com animação de saída antes de fechar o Popup
    val dismiss: () -> Unit = {
        scope.launch {
            visible = false
            delay(ANIM_DURATION.toLong())
            onDismissRequest()
        }
    }

    LaunchedEffect(Unit) { visible = true }

    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = dismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { dismiss() }
                .background(Color.Black.copy(alpha = if (visible) 0.5f else 0f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(ANIM_DURATION)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(ANIM_DURATION)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                        .clip(RectangleShape)
                        .background(colorContent)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (title != null || showCloseButton) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (title != null) {
                                Text(
                                    text = title,
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            if (showCloseButton) {
                                CloseButton(
                                    size = 40.dp,
                                    onClose = dismiss
                                )
                            }
                        }
                    }
                    content()
                }
            }
        }
    }
}