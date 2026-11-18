package com.w_via.designsystem.input

import android.icu.text.NumberFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Campo de texto para valores monetários no estilo bancário
 * Os números são inseridos da direita para a esquerda como em aplicativos bancários
 * @param value Valor atual do campo em centavos (ex: 1000 = R$ 10,00)
 * @param onValueChange Callback quando o valor muda, retorna o valor em centavos
 * @param modifier Modificador para o campo
 * @param textColor Cor do texto
 * @param borderColor Cor da borda
 * @param backgroundColor Cor de fundo
 * @param enabled Se o campo está habilitado para edição
 * @param maxDigits Máximo de dígitos numéricos digitados (antes da formatação em moeda)
 */
@Composable
fun BankNumberField(
    value: Long = 0,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    enabled: Boolean = true,
    maxDigits: Int = 13,
    showSymbol: Boolean = true
) {
    // Cria um TextFieldValue com o valor formatado e a posição do cursor no final
    var textFieldValue by remember(value, showSymbol) {
        val formattedValue = formatCurrency(value, showSymbol)
        mutableStateOf(
            TextFieldValue(
                text = formattedValue,
                selection = TextRange(formattedValue.length)
            )
        )
    }
    
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                focusRequester.requestFocus()
            },
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (!enabled) return@BasicTextField
                
                if (newValue.text == textFieldValue.text) {
                    textFieldValue = newValue.copy(selection = TextRange(newValue.text.length))
                    return@BasicTextField
                }

                // Filtra para aceitar apenas números
                val cleanString = newValue.text.filter { it.isDigit() }
                val newValueLong = cleanString.toLongOrNull() ?: 0L

                if (cleanString.length <= maxDigits) {
                    val formatted = formatCurrency(newValueLong, showSymbol)
                    textFieldValue = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )
                    onValueChange(newValueLong)
                }
            },
            textStyle = TextStyle(
                color = textColor,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(textColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .focusRequester(focusRequester)
                .align(Alignment.Center),
            readOnly = !enabled,
            enabled = enabled
        )
        
        // Placeholder quando o valor é zero
        if (value == 0L) {
            Text(
                text = "",
                style = MaterialTheme.typography.titleMedium,
                color = textColor.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


private fun formatCurrency(value: Long, showSymbol: Boolean = true): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val formatted = format.format(value / 100.0) // divide por 100.0 para mostrar como moeda
    return if (showSymbol) formatted else formatted.replace("R$", "").replace(" ", "").trim()
}


private fun calculateNewCursorPosition(
    oldText: String,
    newText: String,
    oldSelection: TextRange,
    newSelection: TextRange
): Int {
    val oldNonDigits = oldText.count { !it.isDigit() }
    val newNonDigits = newText.count { !it.isDigit() }

    return when {
        newSelection.start == 0 -> 0
        newSelection.start == oldText.length -> newText.length
        else -> {
            val digitsBeforeCursor = oldText
                .substring(0, oldSelection.start)
                .count { it.isDigit() }

            var newPos = 0
            var digitsCounted = 0

            for (i in newText.indices) {
                if (digitsCounted >= digitsBeforeCursor) break
                if (newText[i].isDigit()) digitsCounted++
                newPos = i + 1
            }

            newPos
        }
    }
}

/**
 * Converte uma string para um valor em centavos
 * @param value String a ser convertida
 * @return Valor em centavos
 */
fun stringToLongValue(value: String): Long {
    return value.filter { it.isDigit() }.toLongOrNull() ?: 0L
}

/**
 * Extrai o valor numérico de uma string formatada como moeda
 * @param formattedValue String formatada como moeda
 * @return Valor em centavos
 */
fun extractValueFromFormatted(formattedValue: String): Long {
    val digitsOnly = formattedValue.filter { it.isDigit() }
    return if (digitsOnly.isEmpty()) 0L else digitsOnly.toLong()
}
