package com.w_via.designsystem.input

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OutlinedFieldBox(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    placeholder: String? = null,
    placeholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val cornerRadius = 0.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(width = 1.5.dp.toPx()),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let {
                    Box(modifier = Modifier.padding(end = 8.dp)) { it() }
                }
                Text(
                    text = value.ifEmpty { placeholder.orEmpty() },
                    color = if (value.isNotEmpty()) textColor else placeholderColor,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                trailingIcon?.let {
                    Box(modifier = Modifier.padding(start = 8.dp)) { it() }
                }
            }
        }

        Text(
            text = label,
            color = labelColor,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = (-8).dp)
                .background(backgroundColor)
                .padding(horizontal = 4.dp)
        )
    }
}