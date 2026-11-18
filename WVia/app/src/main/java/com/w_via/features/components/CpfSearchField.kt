package com.w_via.features.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.RectangleShape
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
import com.w_via.features.utils.CPFFormatter

/**
 * Campo de busca de CPF com máscara automática e ícone de lupa, no molde do
 * [CnpjSearchField].
 *
 * @param value         Valor atual do campo (com ou sem formatação).
 * @param onValueChange Chamado com o valor **formatado** a cada alteração.
 * @param onSearchClick Chamado ao clicar no botão de lupa.
 * @param isLoading     Exibe um [CircularProgressIndicator] no lugar da lupa quando `true`.
 * @param isError       Marca o campo como erro.
 * @param modifier      Modificador aplicado ao [Row] externo.
 */
@Composable
fun CpfSearchField(
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
                // Aplica a máscara progressivamente e preserva o cursor.
                val (formatted, newSelection) = CPFFormatter.formatWithCursor(
                    newValue.text,
                    newValue.selection.start
                )
                state = TextFieldValue(formatted, TextRange(newSelection))
                onValueChange(formatted)
            },
            label = { Text("CPF") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isError
        )

        IconButton(
            onClick = onSearchClick,
            enabled = !isLoading && value.isNotEmpty(),
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RectangleShape
                )
                .size(56.dp),
            shape = RectangleShape
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar CPF"
                )
            }
        }
    }
}

/**
 * [CpfSearchField] com validação disparada pelo clique na lupa.
 *
 * O clique na lupa valida o CPF: se vazio ou inválido, [errorMessage] é exibido
 * logo abaixo do campo. Digitar novamente limpa o erro. Usado em todos os pontos
 * do fluxo onde o CPF pode ser buscado (falecido, declarante, cônjuge etc.).
 *
 * @param value           Valor atual do campo (com ou sem formatação).
 * @param onValueChange   Chamado com o valor **formatado** a cada alteração.
 * @param errorMessage    Mensagem de erro progressiva do campo; nulo quando ok.
 * @param modifier        Modificador aplicado à [Column] externa.
 */
@Composable
fun CpfSearchFieldWithFeedback(
    value: String,
    onValueChange: (formatted: String) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var showError by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        CpfSearchField(
            value = value,
            onValueChange = {
                onValueChange(it)
                showError = false
            },
            onSearchClick = {
                showError = errorMessage != null || value.isBlank()
            },
            isError = showError && (errorMessage != null || value.isBlank())
        )
        if (showError && (errorMessage != null || value.isBlank())) {
            Text(
                text = errorMessage ?: "Informe o CPF",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
