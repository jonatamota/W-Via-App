package com.w_via.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.w_via.features.utils.MedicoBasico
import com.w_via.features.utils.MedicoData

/**
 * Bloco de um médico da Declaração de Óbito: UF + "Número do CRM" na mesma
 * linha e o campo "Nome do médico" abaixo, preenchido automaticamente a partir
 * de `medicos.json` quando há correspondência de UF + CRM.
 *
 * Por padronização visual, o "Número do CRM" usa o mesmo [SuggestionAutocompleteField]
 * do nome (sem sugestões), e o campo de nome é editável, sendo preenchido
 * automaticamente assim que uma referência de UF + CRM é encontrada.
 */
@Composable
fun MedicoFields(
    titulo: String,
    uf: String?,
    onUfSelected: (String) -> Unit,
    crm: String,
    onCrmChange: (String) -> Unit,
    nome: String,
    onNomeChange: (String) -> Unit,
    medicos: List<MedicoBasico>,
) {
    Text(titulo, style = MaterialTheme.typography.titleMedium)

    LaunchedEffect(uf, crm, medicos) {
        val encontrado = MedicoData.buscarPor(medicos, uf, crm)
        if (encontrado != null) {
            onNomeChange(encontrado)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        UfDropdownSelect(
            label = "UF",
            options = UF_SIGLAS,
            selectedValue = uf,
            onValueSelected = onUfSelected,
            showIcon = false,
            modifier = Modifier.weight(0.4f)
        )
        SuggestionAutocompleteField(
            value = crm,
            onValueChange = onCrmChange,
            suggestions = emptyList(),
            placeholder = "Número do CRM",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(0.6f)
        )
    }

    SuggestionAutocompleteField(
        value = nome,
        onValueChange = onNomeChange,
        suggestions = emptyList(),
        placeholder = "Nome do médico"
    )
}
