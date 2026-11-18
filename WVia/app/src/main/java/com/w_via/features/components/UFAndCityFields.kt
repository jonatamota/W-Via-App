package com.w_via.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w_via.features.utils.MunicipioData

/**
 * Campos de UF + Cidade exibidos em linha.
 *
 * A UF usa um [UfDropdownSelect] com as siglas presentes em [municipios]; a
 * cidade usa um [SuggestionAutocompleteField] filtrado pelos municípios da UF
 * selecionada. Ao trocar a UF, o valor da cidade é limpo.
 *
 * @param uf              Sigla da UF selecionada (nulo quando nada foi escolhido).
 * @param onUfSelected    Chamado quando uma UF é escolhida.
 * @param city            Texto atual da cidade.
 * @param onCityChange    Chamado com o novo texto da cidade a cada alteração.
 * @param municipios      Mapa `UF -> municípios` (ver [MunicipioData.fromJson]).
 * @param modifier        Modificador aplicado ao [Row] externo.
 */
@Composable
fun UFAndCityFields(
    uf: String?,
    onUfSelected: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    municipios: Map<String, List<String>>,
    modifier: Modifier = Modifier,
    showUfIcon: Boolean = true,
    showUfPlaceholder: Boolean = true
) {
    UFAndPlaceFields(
        placeLabel = "Cidade",
        placeValue = city,
        onPlaceChange = onCityChange,
        uf = uf,
        onUfSelected = onUfSelected,
        municipios = municipios,
        modifier = modifier,
        showUfIcon = showUfIcon,
        showUfPlaceholder = showUfPlaceholder
    )
}

/**
 * Campos de "Natural de" + UF exibidos em linha, no mesmo padrão do
 * [UFAndCityFields], usando o rótulo "Natural de" para a cidade de nascimento.
 *
 * @param naturalDe         Texto atual da cidade de nascimento.
 * @param onNaturalDeChange Chamado com o novo texto a cada alteração.
 * @param uf                Sigla da UF selecionada (nulo quando nada foi escolhido).
 * @param onUfSelected      Chamado quando uma UF é escolhida.
 * @param municipios        Mapa `UF -> municípios` (ver [MunicipioData.fromJson]).
 * @param modifier          Modificador aplicado ao [Row] externo.
 */
@Composable
fun NaturalDeFields(
    naturalDe: String,
    onNaturalDeChange: (String) -> Unit,
    uf: String?,
    onUfSelected: (String) -> Unit,
    municipios: Map<String, List<String>>,
    modifier: Modifier = Modifier,
    showUfIcon: Boolean = true,
    showUfPlaceholder: Boolean = true
) {
    UFAndPlaceFields(
        placeLabel = "Natural de",
        placeValue = naturalDe,
        onPlaceChange = onNaturalDeChange,
        uf = uf,
        onUfSelected = onUfSelected,
        municipios = municipios,
        modifier = modifier,
        showUfIcon = showUfIcon,
        showUfPlaceholder = showUfPlaceholder
    )
}

/**
 * Layout compartilhado entre [UFAndCityFields] e [NaturalDeFields]: UF como
 * [UfDropdownSelect] à esquerda e campo de texto com autocomplete à direita.
 * Trocar a UF limpa o texto do campo de lugar.
 */
@Composable
private fun UFAndPlaceFields(
    placeLabel: String,
    placeValue: String,
    onPlaceChange: (String) -> Unit,
    uf: String?,
    onUfSelected: (String) -> Unit,
    municipios: Map<String, List<String>>,
    modifier: Modifier = Modifier,
    showUfIcon: Boolean = true,
    showUfPlaceholder: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UfDropdownSelect(
            label = "UF",
            options = MunicipioData.ufs(municipios),
            selectedValue = uf,
            onValueSelected = { selectedUf ->
                onUfSelected(selectedUf)
                onPlaceChange("")
            },
            showIcon = showUfIcon,
            showPlaceholder = showUfPlaceholder,
            modifier = Modifier.weight(0.32f)
        )

        SuggestionAutocompleteField(
            value = placeValue,
            onValueChange = onPlaceChange,
            suggestions = MunicipioData.citiesForUf(municipios, uf),
            placeholder = placeLabel,
            modifier = Modifier.weight(0.68f)
        )
    }
}
