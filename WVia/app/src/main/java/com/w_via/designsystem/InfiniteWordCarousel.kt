package com.w_via.designsystem

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteWordCarousel(
    words: List<String>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    textSecondaryColor: Color = MaterialTheme.colorScheme.background,
    blockColor: Color = MaterialTheme.colorScheme.primary,
    blockSecondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    spacing: Dp = 8.dp,
    verticalPadding: Dp = 8.dp,
    horizontalPadding: Dp = 16.dp,
    blockHeight: Dp = 40.dp,
    cornerRadius: Dp = 12.dp,
    backgroundColor: Color = Color.Transparent,
    rowSpacing: Dp = 6.dp,
    reverseSecondRow: Boolean = true
) {
    val firstRowWords = words.filterIndexed { index, _ -> index % 2 == 0 }
    val secondRowWords = words.filterIndexed { index, _ -> index % 2 != 0 }

    // Mais repetições para garantir cobertura total sem offset
    val firstRowRepeated = List(6) { firstRowWords }.flatten()
    val secondRowRepeated = List(6) { secondRowWords }.flatten()

    Surface(
        modifier = modifier,
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            // --- Primeira linha ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        spacing = MarqueeSpacing(spacing),
                        velocity = 26.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                firstRowRepeated.forEachIndexed { index, word ->
                    WordBlock(
                        word = word,
                        textStyle = textStyle,
                        textColor = textColor,
                        blockColor = if (index % 2 == 0) blockColor else blockSecondaryColor,
                        horizontalPadding = horizontalPadding,
                        blockHeight = blockHeight,
                        cornerRadius = cornerRadius
                    )
                }
            }

            // --- Segunda linha ---
            // Velocidade diferente cria o deslocamento visual natural ao longo do tempo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        spacing = MarqueeSpacing(spacing),
                        velocity = if (reverseSecondRow) 42.dp else 26.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                secondRowRepeated.forEachIndexed { index, word ->
                    WordBlock(
                        word = word,
                        textStyle = textStyle,
                        textColor = textColor,
                        blockColor = if (index % 2 == 0) blockSecondaryColor else blockColor,
                        horizontalPadding = horizontalPadding,
                        blockHeight = blockHeight,
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }
}

@Composable
private fun WordBlock(
    word: String,
    textStyle: TextStyle,
    textColor: Color,
    blockColor: Color,
    horizontalPadding: Dp,
    blockHeight: Dp,
    cornerRadius: Dp
) {
    Box(
        modifier = Modifier
            .height(blockHeight)
            .background(
                color = blockColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = word,
            style = textStyle,
            color = textColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}