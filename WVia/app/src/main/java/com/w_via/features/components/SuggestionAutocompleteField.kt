package com.w_via.features.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.distinctUntilChanged

private val FIELD_HEIGHT = 56.dp
private val SUGGESTION_ITEM_HEIGHT = 40.dp

/**
 * Campo de digitação com sugestões em um **único box**.
 *
 * Diferente dos demais, não usa [OutlinedTextField] nem uma [androidx.compose.ui.window.Popup]
 * separada: um [BasicTextField] sem borda própria fica dentro de um único
 * [Surface] com contorno, e as sugestões aparecem **dentro do mesmo box**, acima
 * do campo, separadas apenas por espaçamento interno.
 *
 * Quando o campo ganha foco e há sugestões, o box cresce para cima (sobrepondo o
 * conteúdo anterior, como um dropdown) e exibe a melhor sugestão filtrada via
 * [AutocompleteLogic.filterSuggestions], destacada. Clicar nela preenche o campo.
 *
 * O campo não tem rótulo: o [placeholder] é o indicativo exibido enquanto o
 * campo está vazio, desaparecendo assim que o usuário digita.
 *
 * @param value           Texto atual do campo.
 * @param onValueChange   Chamado com o novo texto a cada alteração.
 * @param suggestions     Lista completa de opções usada para filtrar.
 * @param placeholder     Indicativo exibido enquanto o campo está vazio.
 * @param isError         Marca o campo como erro.
 * @param supportingText  Texto de apoio exibido abaixo do box.
 * @param keyboardOptions Opções de teclado do campo.
 * @param enabled         Habilita/desabilita a edição.
 * @param visibleItems    Quantas sugestões exibidas (padrão 1).
 * @param maxSuggestions  Limite de opções consideradas na filtragem.
 */
@Composable
fun SuggestionAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    visibleItems: Int = 1,
    maxSuggestions: Int = 8
) {
    val textFieldState = rememberTextFieldState(initialText = value.uppercase())
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { newText ->
                if (newText != currentValue) {
                    currentOnValueChange(newText.uppercase())
                }
            }
    }

    LaunchedEffect(currentValue) {
        if (textFieldState.text.toString() != currentValue) {
            textFieldState.setTextAndPlaceCursorAtEnd(currentValue.uppercase())
        }
    }

    var lastSelected by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isFocused) {
        if (!isFocused) lastSelected = null
    }

    val matches = remember(value, suggestions, maxSuggestions) {
        AutocompleteLogic.filterSuggestions(value, suggestions, maxSuggestions)
    }.take(visibleItems)
    val bestSuggestion = matches.firstOrNull()

    val expanded = isFocused && matches.isNotEmpty() && value != lastSelected

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val density = LocalDensity.current
    val baseHeightPx = with(density) { FIELD_HEIGHT.roundToPx() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .zIndex(if (expanded) 1f else 0f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val extra = (placeable.height - baseHeightPx).coerceAtLeast(0)
                    layout(placeable.width, baseHeightPx) {
                        placeable.place(0, -extra)
                    }
                }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column {
                    if (expanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        matches.forEach { option ->
                            SuggestionRow(
                                option = option,
                                highlighted = option == bestSuggestion,
                                onClick = {
                                    textFieldState.setTextAndPlaceCursorAtEnd(option.uppercase())
                                    onValueChange(option.uppercase())
                                    lastSelected = option.uppercase()
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(FIELD_HEIGHT)
                    ) {
                        BasicTextField(
                            state = textFieldState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterStart)
                                .padding(horizontal = 16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            lineLimits = TextFieldLineLimits.SingleLine,
                            keyboardOptions = keyboardOptions,
                            interactionSource = interactionSource,
                            enabled = enabled
                        )

                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        supportingText?.let { it() }
    }
}

/** Uma linha da lista de sugestões, com a melhor sugestão destacada. */
@Composable
private fun SuggestionRow(
    option: String,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(SUGGESTION_ITEM_HEIGHT)
            .background(
                color = if (highlighted) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = option.uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = if (highlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
