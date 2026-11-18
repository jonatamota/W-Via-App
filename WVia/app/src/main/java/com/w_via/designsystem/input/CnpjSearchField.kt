package com.w_via.designsystem.input

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.w_via.opencnpj.CNPJFormatter

/**
 * Campo de busca de CNPJ com máscara automática e ícone de lupa.
 *
 * @param value           Valor atual do campo (com ou sem formatação).
 * @param onValueChange   Chamado com o valor **formatado** a cada alteração.
 * @param onSearchClick   Chamado ao clicar no botão de lupa.
 * @param isLoading       Exibe um [CircularProgressIndicator] no lugar da lupa quando `true`.
 * @param isError         Marca o campo como erro.
 * @param modifier        Modificador aplicado ao [Row] externo.
 */
@Composable
fun CnpjSearchField(
    value: String,
    onValueChange: (formatted: String) -> Unit,
    onSearchClick: () -> Unit,
    isLoading: Boolean = false,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        var state by remember { mutableStateOf(TextFieldValue(value)) }
        LaunchedEffect(value) {
            if (value != state.text) state = TextFieldValue(value)
        }

        OutlinedTextField(
            value = state,
            onValueChange = { newValue ->
                // Suporta CNPJ alfanumérico: mantém letras maiúsculas e dígitos,
                // aplicando a máscara progressivamente e preservando o cursor.
                val (formatted, newSelection) = CNPJFormatter.formatWithCursor(
                    newValue.text,
                    newValue.selection.start
                )
                state = TextFieldValue(formatted, TextRange(newSelection))
                onValueChange(formatted)
            },
            label = { Text("CNPJ") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        IconButton(
            onClick = onSearchClick,
            enabled = !isLoading && value.isNotEmpty(),
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(4.dp)
                )
                .size(56.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar CNPJ"
                )
            }
        }
    }
}
