package com.w_via.features.receipt.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.w_via.features.components.AccordionSelect
import com.w_via.features.components.NaturalDeFields
import com.w_via.features.components.rememberMunicipios
import com.w_via.features.utils.CPFFormatter
import com.w_via.features.utils.RGFormatter

@Composable
fun Step2Falecido(
    nomeFalecido: String,
    onNomeFalecidoChange: (String) -> Unit,
    idadeFalecido: String,
    onIdadeFalecidoChange: (String) -> Unit,
    sexoFalecido: String,
    onSexoFalecidoChange: (String) -> Unit,
    naturalidadeFalecido: String,
    onNaturalidadeFalecidoChange: (String) -> Unit,
    ufFalecido: String,
    onUfFalecidoChange: (String) -> Unit,
    rgFalecido: String,
    onRgFalecidoChange: (String) -> Unit,
    cpfFalecido: String,
    onCpfFalecidoChange: (String) -> Unit,
    paiFalecido: String,
    onPaiFalecidoChange: (String) -> Unit,
    maeFalecido: String,
    onMaeFalecidoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var rgState by remember { mutableStateOf(TextFieldValue(RGFormatter.format(rgFalecido))) }
    LaunchedEffect(rgFalecido) {
        if (rgFalecido != rgState.text) rgState = TextFieldValue(rgFalecido)
    }
    var cpfState by remember { mutableStateOf(TextFieldValue(CPFFormatter.formatWithMask(cpfFalecido).orEmpty())) }
    LaunchedEffect(cpfFalecido) {
        val formatted = CPFFormatter.formatWithMask(cpfFalecido).orEmpty()
        if (formatted != cpfState.text) cpfState = TextFieldValue(formatted)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "Dados do Falecido", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nomeFalecido.uppercase(),
                onValueChange = { onNomeFalecidoChange(it.uppercase()) },
                label = { Text("Nome do Falecido") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = idadeFalecido,
                    onValueChange = onIdadeFalecidoChange,
                    label = { Text("Idade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
                AccordionSelect(
                    label = "Sexo",
                    options = listOf("Masculino", "Feminino"),
                    selectedValue = sexoFalecido.ifBlank { null },
                    onValueSelected = onSexoFalecidoChange,
                    placeholder = "",
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            NaturalDeFields(
                naturalDe = naturalidadeFalecido,
                onNaturalDeChange = onNaturalidadeFalecidoChange,
                uf = ufFalecido.ifBlank { null },
                onUfSelected = onUfFalecidoChange,
                municipios = rememberMunicipios()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = rgState,
                    onValueChange = { newValue ->
                        val (formatted, newSelection) = RGFormatter.formatWithCursor(
                            newValue.text,
                            newValue.selection.start
                        )
                        rgState = TextFieldValue(formatted, TextRange(newSelection))
                        onRgFalecidoChange(formatted)
                    },
                    label = { Text("RG") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                OutlinedTextField(
                    value = cpfState,
                    onValueChange = { newValue ->
                        val (formatted, newSelection) = CPFFormatter.formatWithCursor(
                            newValue.text,
                            newValue.selection.start
                        )
                        cpfState = TextFieldValue(formatted, TextRange(newSelection))
                        onCpfFalecidoChange(formatted)
                    },
                    label = { Text("CPF") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Filiação", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = paiFalecido.uppercase(),
                onValueChange = { onPaiFalecidoChange(it.uppercase()) },
                label = { Text("Pai") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = maeFalecido.uppercase(),
                onValueChange = { onMaeFalecidoChange(it.uppercase()) },
                label = { Text("Mãe") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
