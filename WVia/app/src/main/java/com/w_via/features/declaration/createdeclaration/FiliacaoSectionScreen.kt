package com.w_via.features.declaration.createdeclaration

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.w_via.features.components.AccordionSelect
import com.w_via.features.components.NaturalDeFields
import com.w_via.features.components.SuggestionAutocompleteField
import com.w_via.features.components.rememberMunicipios
import com.w_via.features.components.rememberProfissoes
import com.w_via.features.declaration.DeclaracaoObitoThresholds
import com.w_via.features.declaration.LabeledTextField
import com.w_via.features.declaration.state.DeclaracaoObitoFormState

private val ESTADO_CIVIL_STRINGS = listOf(
    "Solteiro", "Casado", "União estável", "Divorciado", "Viúvo", "Falecido"
)

/** Peso da fonte por opção: destaca "Falecido" com Medium nas opções. */
private fun estadoCivilOptionFontWeight(option: String): FontWeight =
    if (option == "Falecido") FontWeight.Medium else FontWeight.Normal

@Composable
fun FiliacaoSectionScreen(
    navController: NavController,
    viewModel: DeclarationDraftViewModel,
) {
    DeclarationSectionHost(
        section = DeclarationSection.FILIACAO,
        navController = navController,
        viewModel = viewModel
    ) { s ->
        val municipios = rememberMunicipios()
        val profissoes = rememberProfissoes()

        Text("Pai", style = MaterialTheme.typography.titleMedium)
        LabeledTextField("Nome do pai", s.nomePai, { v -> viewModel.update { it.copy(nomePai = v) } })
        NaturalDeFields(
            naturalDe = s.naturalPai,
            onNaturalDeChange = { v -> viewModel.update { it.copy(naturalPai = v) } },
            uf = s.ufNaturalPai.ifBlank { null },
            onUfSelected = { v -> viewModel.update { it.copy(ufNaturalPai = v) } },
            municipios = municipios
        )
        AccordionSelect(
            label = "Estado civil",
            options = ESTADO_CIVIL_STRINGS,
            selectedValue = s.estadoCivilPai.ifBlank { null },
            onValueSelected = { v -> viewModel.update { it.copy(estadoCivilPai = v) } },
            optionFontWeight = ::estadoCivilOptionFontWeight
        )
        SuggestionAutocompleteField(
            value = s.profissaoPai,
            onValueChange = { v -> viewModel.update { it.copy(profissaoPai = v) } },
            suggestions = profissoes,
            placeholder = "Profissão"
        )

        Text("Mãe", style = MaterialTheme.typography.titleMedium)
        LabeledTextField("Nome da mãe", s.nomeMae, { v -> viewModel.update { it.copy(nomeMae = v) } })
        NaturalDeFields(
            naturalDe = s.naturalMae,
            onNaturalDeChange = { v -> viewModel.update { it.copy(naturalMae = v) } },
            uf = s.ufNaturalMae.ifBlank { null },
            onUfSelected = { v -> viewModel.update { it.copy(ufNaturalMae = v) } },
            municipios = municipios
        )
        AccordionSelect(
            label = "Estado civil",
            options = ESTADO_CIVIL_STRINGS,
            selectedValue = s.estadoCivilMae.ifBlank { null },
            onValueSelected = { v -> viewModel.update { it.copy(estadoCivilMae = v) } },
            optionFontWeight = ::estadoCivilOptionFontWeight
        )
        SuggestionAutocompleteField(
            value = s.profissaoMae,
            onValueChange = { v -> viewModel.update { it.copy(profissaoMae = v) } },
            suggestions = profissoes,
            placeholder = "Profissão"
        )

        if ((s.idade.toIntOrNull() ?: 0) >= DeclaracaoObitoThresholds.IDADE_ALERTA_FILIACAO_DESCONHECIDA) {
            Text(
                text = "Falecido com idade avançada — a filiação pode ser desconhecida pela declarante.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
