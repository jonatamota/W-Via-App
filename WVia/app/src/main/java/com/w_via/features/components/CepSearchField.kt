package com.w_via.features.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.w_via.features.utils.CepFormatter

/**
 * Campo de busca de CEP com máscara automática (`00000-000`) e ícone de lupa,
 * no molde do [CpfSearchField] e do [com.w_via.designsystem.input.CnpjSearchField].
 *
 * @param value          Valor atual do campo (com ou sem formatação).
 * @param onValueChange  Chamado com o valor **formatado** a cada alteração.
 * @param label          Rótulo do campo.
 * @param isError        Marca o campo como erro.
 * @param supportingText Texto de apoio exibido abaixo do campo.
 * @param onSearchClick  Chamado ao clicar no botão de lupa.
 * @param isLoading      Exibe um [CircularProgressIndicator] no lugar da lupa quando `true`.
 * @param modifier       Modificador aplicado ao [Row] externo.
 */
@Composable
fun CepSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "CEP",
    isError: Boolean = false,
    supportingText: String? = null,
    onSearchClick: () -> Unit = {},
    isLoading: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(CepFormatter.format(newValue))
            },
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } }
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
                    contentDescription = "Buscar CEP"
                )
            }
        }
    }
}
