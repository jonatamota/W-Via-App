package com.w_via.features.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Lógica pura de filtro/sugestão do [AutocompleteTextField]. Isolada para
 * permitir testes unitários sem dependência de UI.
 */
object AutocompleteLogic {

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)

    /**
     * Filtra [options] a partir de [query], ignorando caixa e acentuação.
     * Opções que começam com a consulta vêm antes das que apenas a contêm.
     */
    fun filterSuggestions(
        query: String,
        options: List<String>,
        maxSuggestions: Int = 8
    ): List<String> {
        val normalizedQuery = normalize(query.trim())
        if (normalizedQuery.isEmpty()) return emptyList()

        val startsWith = mutableListOf<String>()
        val contains = mutableListOf<String>()

        for (option in options) {
            val normalizedOption = normalize(option)
            when {
                normalizedOption.startsWith(normalizedQuery) -> startsWith.add(option)
                normalizedOption.contains(normalizedQuery) -> contains.add(option)
            }
        }

        return (startsWith + contains).distinct().take(maxSuggestions)
    }

    /**
     * Retorna a melhor sugestão inline para [query]: a primeira opção que começa
     * com a consulta e é mais longa que ela. Nulo quando não há sugestão útil.
     */
    fun currentSuggestion(query: String, options: List<String>): String? {
        val normalizedQuery = normalize(query.trim())
        if (normalizedQuery.isEmpty()) return null

        return options.firstOrNull { option ->
            val normalizedOption = normalize(option)
            normalizedOption.startsWith(normalizedQuery) &&
                normalizedOption.length > normalizedQuery.length
        }
    }
}

/**
 * Campo com autocomplete inline (estilo busca do Google).
 *
 * Baseado no [OutlinedTextField] da Material 3 com estado interno. Enquanto o
 * usuário digita, a melhor sugestão aparece na mesma linha, em cor
 * apagada/secundária, imediatamente à frente do cursor. Clicar na sugestão
 * preenche o campo com o texto completo.
 *
 * A sugestão é renderizada via [OutputTransformation]: ela é apenas visual e
 * nunca altera o valor efetivo do campo.
 *
 * @param value           Texto atual do campo.
 * @param onValueChange   Chamado com o novo texto a cada alteração.
 * @param suggestions     Lista completa de opções usada para filtrar a sugestão.
 * @param maxSuggestions  Limite de opções consideradas para a sugestão.
 * @param label           Rótulo do campo.
 * @param placeholder     Placeholder exibido quando vazio.
 * @param isError         Marca o campo como erro.
 * @param supportingText  Texto de apoio exibido abaixo do campo.
 * @param keyboardOptions Opções de teclado do campo.
 * @param enabled         Habilita/desabilita a edição.
 * @param readOnly        Impede a edição mantendo o foco/clique na sugestão.
 * @param leadingIcon     Ícone inicial do campo.
 * @param trailingIcon    Ícone final do campo.
 * @param shape           Formato do contorno do campo.
 */
@Composable
fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    maxSuggestions: Int = 8,
    label: String = "",
    placeholder: String = "",
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = RectangleShape
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

    val suggestion = remember(value, suggestions, maxSuggestions) {
        AutocompleteLogic.currentSuggestion(value, suggestions.take(maxSuggestions))?.uppercase()
    }
    val ghostSuffix = suggestion
        ?.takeIf { value.length < it.length }
        ?.substring(value.length)
        ?.takeIf { it.isNotEmpty() }
    val showGhost = isFocused && ghostSuffix != null

    val ghostColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val outputTransformation = remember(showGhost, ghostSuffix, ghostColor) {
        if (showGhost) {
            OutputTransformation {
                val ghostStart = length
                append(ghostSuffix)
                addStyle(SpanStyle(color = ghostColor), ghostStart, length)
            }
        } else {
            null
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.bodyLarge
    val typedTextWidth = remember(value, textMeasurer, textStyle) {
        if (value.isEmpty()) {
            0
        } else {
            textMeasurer.measure(
                text = AnnotatedString(value),
                style = textStyle
            ).size.width
        }
    }
    val ghostTextWidth = remember(ghostSuffix, textMeasurer, textStyle) {
        if (ghostSuffix.isNullOrEmpty()) {
            0
        } else {
            textMeasurer.measure(
                text = AnnotatedString(ghostSuffix),
                style = textStyle
            ).size.width
        }
    }

    val density = LocalDensity.current
    val minInteractiveSize = LocalMinimumInteractiveComponentSize.current
    val iconPadding = ((minInteractiveSize - 24.dp) / 2).coerceAtLeast(0.dp)
    val contentStart = if (leadingIcon != null) {
        minInteractiveSize + (16.dp - iconPadding).coerceAtLeast(0.dp)
    } else {
        16.dp
    }
    val ghostOffsetX = with(density) { typedTextWidth.toDp() } + contentStart
    val ghostWidth = with(density) { ghostTextWidth.toDp() } + 8.dp

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            state = textFieldState,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            label = if (label.isNotEmpty()) {
                { Text(text = label) }
            } else {
                null
            },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(text = placeholder) }
            } else {
                null
            },
            supportingText = supportingText,
            isError = isError,
            lineLimits = TextFieldLineLimits.SingleLine,
            outputTransformation = outputTransformation,
            keyboardOptions = keyboardOptions,
            interactionSource = interactionSource,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            shape = shape
        )

        if (suggestion != null && showGhost) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = ghostOffsetX)
                    .width(ghostWidth)
                    .fillMaxHeight()
                    .zIndex(1f)
                    .clickable {
                        textFieldState.setTextAndPlaceCursorAtEnd(suggestion)
                        onValueChange(suggestion)
                    }
            )
        }
    }
}
