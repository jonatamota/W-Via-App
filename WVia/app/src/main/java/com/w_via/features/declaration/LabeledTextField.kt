package com.w_via.features.declaration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    uppercase: Boolean = true
) {
    OutlinedTextField(
        value = if (uppercase) value.uppercase() else value,
        onValueChange = { newValue -> onValueChange(if (uppercase) newValue.uppercase() else newValue) },
        label = { Text(label) },
        singleLine = singleLine,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

/**
 * [LabeledTextField] com máscara aplicada a cada tecla (RG, INSS, CPF etc.),
 * preservando a posição do cursor durante a edição.
 *
 * @param format fn(texto atual, posição do cursor) -> (texto formatado, nova posição).
 */
@Composable
fun MaskedLabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    format: (text: String, cursor: Int) -> Pair<String, Int>,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var state by remember { mutableStateOf(TextFieldValue(value.uppercase())) }
    LaunchedEffect(value) {
        val uppercased = value.uppercase()
        if (uppercased != state.text) state = TextFieldValue(uppercased)
    }

    OutlinedTextField(
        value = state,
        onValueChange = { new ->
            val (formatted, newSelection) = format(new.text.uppercase(), new.selection.start)
            state = TextFieldValue(formatted, TextRange(newSelection))
            onValueChange(formatted)
        },
        label = { Text(label) },
        singleLine = singleLine,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

/**
 * Seletor de opção única em formato de chips — usado para Sexo, Estado civil,
 * Tipo de local do óbito, e qualquer enum de domínio.
 *
 * Por padrão o rótulo fica na mesma linha do scroll ([labelAbove] == false),
 * ideal para poucas opções curtas (ex.: Sexo). Quando há muitas opções, passe
 * [labelAbove] == true para exibir o rótulo acima do scroll.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SingleChoiceSelector(
    label: String,
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    labelAbove: Boolean = false
) {
    val chipsRow: @Composable () -> Unit = {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (labelAbove) {
                items(options) { option ->
                    FilterChip(
                        selected = option == selected,
                        onClick = { onSelect(option) },
                        label = { Text(optionLabel(option)) }
                    )
                }
            } else {
                item { Text(text = label) }
                items(options) { option ->
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(
                        selected = option == selected,
                        onClick = { onSelect(option) },
                        label = { Text(optionLabel(option)) }
                    )
                }
            }
        }
    }

    if (labelAbove) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall
            )
            chipsRow()
        }
    } else {
        chipsRow()
    }
}


/** Atalho para perguntas Sim/Não (deixaBens, eleitor, reservista, casamentoEmCartorio...). */
@Composable
fun SimNaoSelector(
    label: String,
    selected: Boolean?,
    onSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = label)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selected == true,
                onClick = { onSelect(true) },
                label = { Text("Sim") })
            FilterChip(
                selected = selected == false,
                onClick = { onSelect(false) },
                label = { Text("Não") })
        }
    }
}
