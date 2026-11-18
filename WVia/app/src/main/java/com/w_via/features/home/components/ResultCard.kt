package com.w_via.features.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.clip

data class ListItem(
    val iconSource: Int,
    val text: String
)

data class CostProfitItem(
    val iconSource: Int,
    val text: String,
    val value: String,
    val color: Color
)

@Composable
fun ResultCard(
    modifier: Modifier = Modifier,
    onClickAction: (() -> Unit)? = null,
    column1Items: List<ListItem>,
    column2Items: List<ListItem>,
    costItem: CostProfitItem? = null,  // Item de custo
    profitItem: CostProfitItem? = null  // Item de lucro
) {
    val cornerRadius = 2.dp
    val filteredColumn1Items = remember(column1Items) {
        column1Items.filter { it.text.isNotBlank() && !isZeroValue(it.text) }
    }
    val filteredColumn2Items = remember(column2Items) {
        column2Items.filter { it.text.isNotBlank() && !isZeroValue(it.text) }
    }
    val visibleCostItem = remember(costItem) { costItem?.takeIf { !isZeroValue(it.value) } }
    val visibleProfitItem = remember(profitItem) { profitItem?.takeIf { !isZeroValue(it.value) } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        horizontalAlignment = Alignment.End
    ) {
        // Card principal
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(
                topStart = cornerRadius,
                topEnd = cornerRadius,
                bottomStart = cornerRadius,
                bottomEnd = cornerRadius
            ),
            color = Color.Transparent,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF282727),
                            shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Coluna 1
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                        // sem modifier.weight()
                    ) {
                        filteredColumn1Items.forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = item.iconSource),
                                    contentDescription = item.text,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isNumericValue(item.text)) {
                                    AnimatedNumberText(
                                        targetValue = item.text,
                                        color = Color.White,
                                        fontSize = 15,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = item.text,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Coluna 2
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        filteredColumn2Items.forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = item.iconSource),
                                    contentDescription = item.text,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isNumericValue(item.text)) {
                                    AnimatedNumberText(
                                        targetValue = item.text,
                                        color = Color.White,
                                        fontSize = 15,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = item.text,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Linha de Custos e Lucro (alinhada com os itens das colunas)
                if (visibleCostItem != null || visibleProfitItem != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                            )
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Item de Custo
                        if (visibleCostItem != null) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                // Linha com ícone e título
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = visibleCostItem.iconSource),
                                        contentDescription = visibleCostItem.text,
                                        tint = visibleCostItem.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = visibleCostItem.text,
                                        fontWeight = FontWeight.Medium,
                                        color = visibleCostItem.color,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Valor animado embaixo
                                AnimatedNumberText(
                                    targetValue = visibleCostItem.value,
                                    color = visibleCostItem.color,
                                    fontSize = 16,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Item de Lucro
                        if (visibleProfitItem != null) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                // Linha com ícone e título
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = visibleProfitItem.iconSource),
                                        contentDescription = visibleProfitItem.text,
                                        tint = visibleProfitItem.color,
                                        modifier = Modifier.size(18.dp) // Ícone com 18.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = visibleProfitItem.text,
                                        fontWeight = FontWeight.Medium,
                                        color = visibleProfitItem.color,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Valor animado embaixo
                                AnimatedNumberText(
                                    targetValue = visibleProfitItem.value,
                                    color = visibleProfitItem.color,
                                    fontSize = 16,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Botão Editar com delay e animação de expansão
        var showButton by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(350)
            showButton = true
        }

        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + slideInHorizontally(
                initialOffsetX = { it / 2 },
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(end = 20.dp)
                    .clickable { onClickAction?.invoke() },
                shape = RoundedCornerShape(cornerRadius),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Editar rota",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// Função auxiliar para verificar se o texto contém valores numéricos
private fun isNumericValue(text: String): Boolean {
    return text.contains("km") ||
            text.contains("L") ||
            text.contains("min") ||
            text.contains("R$") ||
            text.contains("h") ||
            text.matches(Regex(".*\\d.*"))
}

// Verifica se o texto representa um valor zerado/vazio ("R$ 0,00", "0,0 km",
// "0 min", "—", ...) para não exibi-lo no card de resultado.
private fun isZeroValue(text: String): Boolean {
    if (text.isBlank() || text == "—") return true
    val numeric = text
        .replace(".", "")
        .replace(",", ".")
        .filter { it.isDigit() || it == '.' || it == '-' }
    return numeric.toDoubleOrNull() == 0.0
}

// Componente para texto com animação numérica
@Composable
fun AnimatedNumberText(
    targetValue: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: Int = 14,
    fontWeight: FontWeight = FontWeight.Normal
) {
    AnimatedContent(
        targetState = targetValue,
        transitionSpec = {
            if (targetState.length > initialState.length) {
                (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300))).togetherWith(
                    slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300))
                )
            } else {
                (slideInVertically { height -> -height } + fadeIn(animationSpec = tween(300))).togetherWith(
                    slideOutVertically { height -> height } + fadeOut(animationSpec = tween(300))
                )
            }.using(SizeTransform(clip = false))
        },
        label = "AnimatedNumericText"
    ) { text ->
        Text(
            text = text,
            modifier = modifier,
            fontWeight = fontWeight,
            color = color,
            fontSize = fontSize.sp
        )
    }
}