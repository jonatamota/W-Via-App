package com.w_via.designsystem.appbuttons

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.w_via.ui.theme.colorBrand

@Composable
fun PillSelection(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    pillColor: Color = colorBrand,
    backgroundColor: Color = Color.LightGray.copy(alpha = 0.3f),
    textColorSelected: Color = Color.White,
    textColorUnselected: Color = Color.Gray,
    pillHorizontalPadding: Dp = 24.dp,
    pillVerticalPadding: Dp = 8.dp
) {
    val density = LocalDensity.current

    // Mede a largura de cada opção para garantir que a pílula seja maior que o texto
    val itemWidths = remember(options) { mutableStateListOf<Dp>().apply { repeat(options.size) { add(0.dp) } } }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
    ) {
        // Row invisível apenas para medir as larguras reais de cada item
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .onGloballyPositioned { coords ->
                            itemWidths[index] = with(density) { coords.size.width.toDp() }
                        }
                )
            }
        }

        // Pílula animada
        if (itemWidths.all { it > 0.dp }) {
            val offsetX = remember(selectedIndex, itemWidths.toList()) {
                itemWidths.take(selectedIndex).fold(0.dp) { acc, w -> acc + w }
            }
            val animatedOffset by animateDpAsState(
                targetValue = offsetX,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                label = "pillOffset"
            )
            val pillWidth = itemWidths.getOrElse(selectedIndex) { 0.dp }

            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(pillWidth)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(pillColor)
                    .zIndex(1f)
            )
        }

        // Itens clicáveis com padding garantido
        Row(modifier = Modifier.fillMaxSize().zIndex(2f)) {
            options.forEachIndexed { index, option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onOptionSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (selectedIndex == index) textColorSelected else textColorUnselected,
                        fontWeight = if (selectedIndex == index) FontWeight.Medium else FontWeight.Normal,
                        modifier = Modifier.padding(
                            horizontal = pillHorizontalPadding,
                            vertical = pillVerticalPadding
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}