package com.w_via.features.declaration.createdeclaration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.w_via.features.components.CepSearchField
import com.w_via.features.components.CpfSearchFieldWithFeedback
import com.w_via.features.components.SuggestionAutocompleteField
import com.w_via.features.components.UFAndCityFields
import com.w_via.features.components.rememberCartorios
import com.w_via.features.components.rememberGrausParentesco
import com.w_via.features.components.rememberMunicipios
import com.w_via.features.components.rememberProfissoes
import com.w_via.features.declaration.DeclarationFieldErrors
import com.w_via.features.declaration.LabeledTextField
import com.w_via.features.declaration.MaskedLabeledField
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.utils.CartorioData
import com.w_via.features.utils.PhoneFormatter
import com.w_via.features.utils.RGFormatter

/** Rótulos das duas abas de "Dados do Declarante", expostos de forma testável. */
internal val DECLARANTE_TABS: List<String> = listOf("Declarante", "Cartório")

/**
 * Seção "Dados do Declarante", dividida em duas abas:
 * - Declarante: dados pessoais, profissão, grau de parentesco e endereço.
 * - Cartório: nome, telefone e endereço do cartório.
 */
@Composable
fun DeclaranteSectionScreen(
    navController: NavController,
    viewModel: DeclarationDraftViewModel,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = DECLARANTE_TABS

    DeclarationSectionHost(
        section = DeclarationSection.DECLARANTE,
        navController = navController,
        viewModel = viewModel,
        tabCount = tabs.size,
        currentTab = selectedTab,
        onTabChange = { selectedTab = it }
    ) { s ->
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> DeclaranteTab(s, viewModel)
            1 -> CartorioTab(s, viewModel)
        }
    }
}

@Composable
private fun DeclaranteTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    val municipios = rememberMunicipios()
    val profissoes = rememberProfissoes()
    val grausParentesco = rememberGrausParentesco()
    val cepError = DeclarationFieldErrors.cep(s.cepDeclarante)
    val celularError = DeclarationFieldErrors.phone(s.celularDeclarante)
    val emailError = DeclarationFieldErrors.email(s.emailDeclarante)
    val buscandoCep by viewModel.buscandoCep.collectAsState()
    val cepBuscaError by viewModel.cepBuscaError.collectAsState()

    LabeledTextField("Nome completo", s.nomeDeclarante, { v -> viewModel.update { it.copy(nomeDeclarante = v) } })
    MaskedLabeledField(
        label = "RG",
        value = s.rgDeclarante,
        onValueChange = { v -> viewModel.update { it.copy(rgDeclarante = v) } },
        format = RGFormatter::formatWithCursor,
        keyboardType = KeyboardType.Number
    )
    CpfSearchFieldWithFeedback(
        value = s.cpfDeclarante,
        onValueChange = { v -> viewModel.update { it.copy(cpfDeclarante = v) } },
        errorMessage = DeclarationFieldErrors.cpf(s.cpfDeclarante)
    )
    LabeledTextField(
        label = "E-mail",
        value = s.emailDeclarante,
        onValueChange = { v -> viewModel.update { it.copy(emailDeclarante = v) } },
        keyboardType = KeyboardType.Email,
        isError = emailError != null,
        supportingText = emailError,
        uppercase = false
    )
    MaskedLabeledField(
        label = "Celular",
        value = s.celularDeclarante,
        onValueChange = { v -> viewModel.update { it.copy(celularDeclarante = v) } },
        format = PhoneFormatter::formatWithCursor,
        keyboardType = KeyboardType.Phone,
        isError = celularError != null,
        supportingText = celularError
    )
    SuggestionAutocompleteField(
        value = s.profissaoDeclarante,
        onValueChange = { v -> viewModel.update { it.copy(profissaoDeclarante = v) } },
        suggestions = profissoes,
        placeholder = "Profissão"
    )
    SuggestionAutocompleteField(
        value = s.grauParentesco,
        onValueChange = { v -> viewModel.update { it.copy(grauParentesco = v) } },
        suggestions = grausParentesco,
        placeholder = "Grau de parentesco"
    )
    CepSearchField(
        value = s.cepDeclarante,
        onValueChange = { v ->
            viewModel.clearCepBuscaError()
            viewModel.update { it.copy(cepDeclarante = v) }
        },
        onSearchClick = viewModel::buscarCepDeclarante,
        isLoading = buscandoCep,
        isError = cepError != null || cepBuscaError != null,
        supportingText = cepBuscaError ?: cepError
    )

    var enderecoMais by remember {
        mutableStateOf(
            s.residenciaDeclarante.isNotBlank() ||
                s.ufDeclarante.isNotBlank() ||
                s.municipioDeclarante.isNotBlank() ||
                s.numeroDeclarante.isNotBlank() ||
                s.bairroDeclarante.isNotBlank()
        )
    }
    val setaRotacao by animateFloatAsState(
        targetValue = if (enderecoMais) 180f else 0f,
        label = "seta_declarante"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable { enderecoMais = !enderecoMais }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (enderecoMais) "Ocultar endereço" else "Ver mais",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = if (enderecoMais) "Recolher endereço" else "Expandir endereço",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .graphicsLayer { rotationZ = setaRotacao }
                .size(20.dp)
        )
    }

    AnimatedVisibility(visible = enderecoMais) {
        Column {
            UFAndCityFields(
                uf = s.ufDeclarante.ifBlank { null },
                onUfSelected = { v -> viewModel.update { it.copy(ufDeclarante = v) } },
                city = s.municipioDeclarante,
                onCityChange = { v -> viewModel.update { it.copy(municipioDeclarante = v) } },
                municipios = municipios
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LabeledTextField(
                    label = "N°",
                    value = s.numeroDeclarante,
                    onValueChange = { v -> viewModel.update { it.copy(numeroDeclarante = v) } },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(0.35f)
                )
                LabeledTextField(
                    label = "Bairro",
                    value = s.bairroDeclarante,
                    onValueChange = { v -> viewModel.update { it.copy(bairroDeclarante = v) } },
                    modifier = Modifier.weight(0.65f)
                )
            }
            LabeledTextField("Residência", s.residenciaDeclarante, { v -> viewModel.update { it.copy(residenciaDeclarante = v) } })
        }
    }
}

@Composable
private fun CartorioTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    val municipios = rememberMunicipios()
    val cartorios = rememberCartorios()
    val telefoneError = DeclarationFieldErrors.phone(s.declaranteTelefone)
    val cartoriosInfo = remember(cartorios, s.ufCartorio, s.cidadeCartorio) {
        when {
            s.cidadeCartorio.isNotBlank() ->
                CartorioData.cartoriosPara(cartorios, s.ufCartorio, s.cidadeCartorio)
            s.ufCartorio.isNotBlank() ->
                cartorios[s.ufCartorio]?.values?.flatten().orEmpty()
            else -> emptyList()
        }
    }
    val cartorioNomes = remember(cartoriosInfo) { cartoriosInfo.map { it.nome } }
    var mostrarMais by rememberSaveable { mutableStateOf(false) }

    UFAndCityFields(
        uf = s.ufCartorio.ifBlank { null },
        onUfSelected = { v -> viewModel.update { it.copy(ufCartorio = v) } },
        city = s.cidadeCartorio,
        onCityChange = { v -> viewModel.update { it.copy(cidadeCartorio = v) } },
        municipios = municipios
    )
    SuggestionAutocompleteField(
        value = s.declaranteCartorio,
        onValueChange = { v ->
            viewModel.update {
                var novo = it.copy(declaranteCartorio = v)
                val info = cartoriosInfo.firstOrNull { c -> c.nome.equals(v, ignoreCase = true) }
                if (info != null) {
                    novo = novo.copy(
                        declaranteTelefone = PhoneFormatter.format(info.telefone),
                        enderecoCartorio = info.endereco,
                        emailCartorio = info.email,
                        oficialCartorio = info.oficial
                    )
                }
                novo
            }
        },
        suggestions = cartorioNomes,
        placeholder = "Cartório",
        visibleItems = 3
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { mostrarMais = !mostrarMais }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (mostrarMais) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (mostrarMais) "Ocultar informações" else "Ver mais informações",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (mostrarMais) "Ocultar informações" else "Ver mais informações",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
    if (mostrarMais) {
        MaskedLabeledField(
            label = "Telefone",
            value = s.declaranteTelefone,
            onValueChange = { v -> viewModel.update { it.copy(declaranteTelefone = v) } },
            format = PhoneFormatter::formatWithCursor,
            keyboardType = KeyboardType.Phone,
            isError = telefoneError != null,
            supportingText = telefoneError
        )
        LabeledTextField("Endereço", s.enderecoCartorio, { v -> viewModel.update { it.copy(enderecoCartorio = v) } })
        LabeledTextField(
            label = "E-mail",
            value = s.emailCartorio,
            onValueChange = { v -> viewModel.update { it.copy(emailCartorio = v) } },
            keyboardType = KeyboardType.Email,
            uppercase = false
        )
        LabeledTextField("Oficial", s.oficialCartorio, { v -> viewModel.update { it.copy(oficialCartorio = v) } })
    }
}
