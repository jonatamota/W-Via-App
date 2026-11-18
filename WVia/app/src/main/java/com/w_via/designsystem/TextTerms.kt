package com.w_via.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TextTerms(
    text: String = "",
    modifier: Modifier = Modifier,
    textStyle: TextStyle? = null,
    textAlign: TextAlign = TextAlign.Justify,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 8.dp,
    fillMaxWidth: Boolean = true,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    Row(
        modifier = modifier
            .then(
                if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier
            )
            .padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            ),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        Text(
            text = text,
            textAlign = textAlign,
            style = textStyle ?: MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = maxLines,
            overflow = overflow,
            modifier = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier
        )
    }
}


