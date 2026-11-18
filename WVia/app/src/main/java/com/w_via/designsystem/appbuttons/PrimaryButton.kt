package com.w_via.designsystem.appbuttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Primary Button",
    icon: @Composable (() -> Unit)? = null,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    showOutline: Boolean = false,
    containerColorEnable: Color = MaterialTheme.colorScheme.primary,
    disabledContainerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    disabledContentColor: Color = MaterialTheme.colorScheme.outline,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
    height: Dp = 46.dp,
    horizontalPadding: Dp = 0.dp,
    textSize: TextUnit = 18.sp,
    textWeight: FontWeight = FontWeight.Medium
) {
    val isDarkTheme = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "button scale"
    )

    // Durante o carregamento mantém o fundo sólido em vez de aplicar o estilo
    // "disabled" do Material (que renderiza o container transparente).
    val showSolidState = isLoading || isEnabled

    OutlinedButton(
        onClick = { if (!isLoading && isEnabled) onClick() },
        shape = RoundedCornerShape(0.dp),
        modifier = modifier
            .height(height)
            .padding(horizontal = horizontalPadding)
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale },
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (showSolidState) containerColorEnable else disabledContainerColor,
            contentColor = if (showSolidState) contentColor else disabledContentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        ),
        border = if (showOutline || isDarkTheme) {
            BorderStroke(1.dp, borderColor)
        } else {
            BorderStroke(0.dp, MaterialTheme.colorScheme.surface)
        },
        enabled = showSolidState
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = if (showSolidState) contentColor else disabledContentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = textSize,
                    fontWeight = textWeight,
                    color = if (showSolidState) contentColor else disabledContentColor
                )
            }
        }
    }
}