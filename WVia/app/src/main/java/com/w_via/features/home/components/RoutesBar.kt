package com.w_via.features.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import kotlin.math.roundToInt
import androidx.compose.ui.res.painterResource
import com.w_via.R

@Composable
fun RoutesBar(
    origemValue: String,
    destinoValue: String,
    modifier: Modifier = Modifier,
    swapAnimationTrigger: Int = 0,
    onOrigemClick: () -> Unit = {},
    onDestinoClick: () -> Unit = {},
    onSwapClick: () -> Unit = {}
) {
    val cornerShape = RoundedCornerShape(2.dp)

    val textsVerticalNudge = remember { Animatable(0f) }
    LaunchedEffect(swapAnimationTrigger) {
        if (swapAnimationTrigger == 0) return@LaunchedEffect
        textsVerticalNudge.snapTo(-10f)
        textsVerticalNudge.animateTo(0f, tween(380, easing = FastOutSlowInEasing))
    }

    // Evitar pulo visual durante re-composições
    val currentOffset = remember(textsVerticalNudge.value) {
        textsVerticalNudge.value.roundToInt()
    }

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), cornerShape)
            .background(MaterialTheme.colorScheme.background, cornerShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Coluna dos ícones com linha tracejada
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // Ponto origem
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )

                // Linha tracejada
                Canvas(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                ) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.4f),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(6f, 4f),
                            phase = 0f
                        )
                    )
                }

                // Pin destino
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Coluna dos textos (leve deslize ao trocar origem/destino)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset { IntOffset(0, currentOffset) },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Origem
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOrigemClick
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = origemValue,
                        style = MaterialTheme.typography.titleSmall,
                        color = when (origemValue) {
                            "Ponto de partida", "De onde?" -> MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.7f
                            )

                            "Sua localização atual" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )

                // Destino
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = destinoValue,
                        style = MaterialTheme.typography.titleSmall,
                        color = when (destinoValue) {
                            "Para onde?" -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            "Sua localização atual" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDestinoClick
                            )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.ic_swap_vertical_circle_24dp),
                        contentDescription = "Trocar origem e destino",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSwapClick
                            )
                    )
                }
            }
        }
    }
}