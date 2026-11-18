package com.w_via.features.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Estado do [AccordionSelect]. Controla a expansão do box sanfonado e a
 * seleção de uma opção. Isolado em uma classe para permitir testes unitários.
 */
@Stable
class AccordionSelectState(initialExpanded: Boolean = false) {

    var expanded by mutableStateOf(initialExpanded)
        private set

    fun toggle() {
        expanded = !expanded
    }

    fun expand() {
        expanded = true
    }

    fun collapse() {
        expanded = false
    }

    fun onOptionSelected(value: String, onValueSelected: (String) -> Unit) {
        onValueSelected(value)
        collapse()
    }
}

/**
 * Seleção vertical sanfonada (accordion select).
 *
 * Box com o [label] à esquerda e o valor selecionado à direita. Ao clicar,
 * expande suavemente para baixo exibindo as [options]; ao selecionar, recolhe
 * e exibe a opção escolhida no texto à direita.
 *
 * @param label           Texto exibido à esquerda do box.
 * @param options         Lista de opções exibidas quando expandido.
 * @param selectedValue   Valor selecionado exibido à direita (nulo mostra [placeholder]).
 * @param onValueSelected Chamado quando uma opção é escolhida.
 * @param state           Estado de expansão/seleção (lembrado automaticamente).
 * @param placeholder     Texto exibido à direita quando nada foi selecionado.
 * @param errorMessage    Quando não nulo, marca o box com erro e exibe a mensagem.
 * @param shape           Formato do contorno do box.
 * @param optionFontWeight Peso da fonte por opção (permite destacar opções específicas).
 */
@Composable
fun AccordionSelect(
    label: String,
    options: List<String>,
    selectedValue: String?,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    state: AccordionSelectState = remember { AccordionSelectState() },
    placeholder: String = "Selecione",
    errorMessage: String? = null,
    shape: Shape = RectangleShape,
    optionFontWeight: (String) -> FontWeight = { FontWeight.Normal }
) {
    val showError = errorMessage != null

    val borderColor = when {
        showError -> MaterialTheme.colorScheme.error
        state.expanded -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val arrowRotation by animateFloatAsState(
        targetValue = if (state.expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "accordionArrow"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { state.toggle() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = selectedValue ?: placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedValue != null) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedValue != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (state.expanded) "Recolher" else "Expandir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.expanded,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        options.forEach { option ->
                            val isSelected = option == selectedValue

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        state.onOptionSelected(option, onValueSelected)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = optionFontWeight(option),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showError) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
