package com.w_via.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun DynamicBlurBox(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    isLocked: Boolean = true,
    fadeColor: Color = Color.White,
) {
    if (isLocked) {
        Box(
            modifier = modifier
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = fadeColor,
                        tints = listOf(HazeTint(fadeColor.copy(alpha = 0.5f))),
                        blurRadius = 15.dp,
                    )
                )
        )
    }
}