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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.w_via.features.components.MedicoFields
import com.w_via.features.components.SuggestionAutocompleteField
import com.w_via.features.components.UFAndCityFields
import com.w_via.features.components.rememberLocaisBrasil
import com.w_via.features.components.rememberMedicos
import com.w_via.features.components.rememberMunicipios
import com.w_via.features.declaration.DeclaracaoObitoThresholds
import com.w_via.features.declaration.DeclarationFieldErrors
import com.w_via.features.declaration.LabeledTextField
import com.w_via.features.declaration.SingleChoiceSelector
import com.w_via.features.declaration.TipoLocalObito
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.utils.LocaisBrasilData

/** Rótulos das três abas de "Dados do Óbito", expostos de forma testável. */
internal val OBITO_TABS: List<String> = listOf("Local", "Sepultamento", "Médico")

/**
 * Seção "Dados do óbito", dividida em três abas: Local (onde e quando faleceu),
 * Sepultamento (velório, cemitério e horários) e Médico (atestado, causa da
 * morte e médicos responsáveis).
 */
@Composable
fun DadosObitoSectionScreen(
    navController: NavController,
    viewModel: DeclarationDraftViewModel,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = OBITO_TABS

    DeclarationSectionHost(
        section = DeclarationSection.OBITO,
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
            0 -> LocalTab(s, viewModel)
            1 -> SepultamentoTab(s, viewModel)
            2 -> MedicoTab(s, viewModel)
        }
    }
}

@Composable
private fun LocalTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    val municipios = rememberMunicipios()
    val locais = rememberLocaisBrasil()

    SingleChoiceSelector(
        label = "Local de falecimento",
        options = TipoLocalObito.entries,
        selected = s.tipoLocalObito,
        onSelect = { v -> viewModel.update { it.copy(tipoLocalObito = v) } },
        optionLabel = { it.label },
        labelAbove = true
    )
    UFAndCityFields(
        uf = s.ufObito.ifBlank { null },
        onUfSelected = { v -> viewModel.update { it.copy(ufObito = v) } },
        city = s.cidadeObito,
        onCityChange = { v -> viewModel.update { it.copy(cidadeObito = v) } },
        municipios = municipios
    )
    val hospitais = if (s.tipoLocalObito == TipoLocalObito.HOSPITAL) {
        LocaisBrasilData.hospitaisPara(locais, s.ufObito, s.cidadeObito)
    } else {
        emptyList()
    }
    SuggestionAutocompleteField(
        value = s.nomeLocalObito,
        onValueChange = { v -> viewModel.update { it.copy(nomeLocalObito = v) } },
        suggestions = hospitais,
        placeholder = "Nome do local"
    )
    DeclarationDateField(
        value = s.dataObito,
        label = "Data do óbito",
        onValueChange = { v -> viewModel.update { it.copy(dataObito = v) } }
    )
    DeclarationTimeField(
        value = s.horaFalecimento,
        label = "Hora do falecimento",
        onValueChange = { v -> viewModel.update { it.copy(horaFalecimento = v) } }
    )
}

@Composable
private fun SepultamentoTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    val municipios = rememberMunicipios()
    val locais = rememberLocaisBrasil()
    val cepVelorioError = DeclarationFieldErrors.cep(s.cepVelorio)
    val buscandoCep by viewModel.buscandoCep.collectAsState()
    val cepBuscaError by viewModel.cepBuscaError.collectAsState()

    DeclarationTimeField(
        value = s.horaSepultamento,
        label = "Hora do sepultamento",
        onValueChange = { v -> viewModel.update { it.copy(horaSepultamento = v) } }
    )
    val cemiterios = LocaisBrasilData.cemiteriosPara(locais, s.ufVelorio, s.cidadeVelorio)
    SuggestionAutocompleteField(
        value = s.cemiterio,
        onValueChange = { v -> viewModel.update { it.copy(cemiterio = v) } },
        suggestions = cemiterios,
        placeholder = "Cemitério"
    )
    LabeledTextField("Local do velório", s.localVelorio, { v -> viewModel.update { it.copy(localVelorio = v) } })
    DeclarationTimeField(
        value = s.horaVelorio,
        label = "Hora do velório",
        onValueChange = { v -> viewModel.update { it.copy(horaVelorio = v) } }
    )
    CepSearchField(
        value = s.cepVelorio,
        onValueChange = { v ->
            viewModel.clearCepBuscaError()
            viewModel.update { it.copy(cepVelorio = v) }
        },
        onSearchClick = viewModel::buscarCepVelorio,
        isLoading = buscandoCep,
        isError = cepVelorioError != null || cepBuscaError != null,
        supportingText = cepBuscaError ?: cepVelorioError
    )

    var enderecoMais by remember {
        mutableStateOf(
            s.enderecoVelorio.isNotBlank() ||
                s.ufVelorio.isNotBlank() ||
                s.cidadeVelorio.isNotBlank() ||
                s.numeroVelorio.isNotBlank() ||
                s.bairroVelorio.isNotBlank()
        )
    }
    val setaRotacao by animateFloatAsState(
        targetValue = if (enderecoMais) 180f else 0f,
        label = "seta_sepultamento"
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
                uf = s.ufVelorio.ifBlank { null },
                onUfSelected = { v -> viewModel.update { it.copy(ufVelorio = v) } },
                city = s.cidadeVelorio,
                onCityChange = { v -> viewModel.update { it.copy(cidadeVelorio = v) } },
                municipios = municipios
            )
            LabeledTextField("Endereço", s.enderecoVelorio, { v -> viewModel.update { it.copy(enderecoVelorio = v) } })
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LabeledTextField(
                    label = "N°",
                    value = s.numeroVelorio,
                    onValueChange = { v -> viewModel.update { it.copy(numeroVelorio = v) } },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(0.35f)
                )
                LabeledTextField(
                    label = "Bairro",
                    value = s.bairroVelorio,
                    onValueChange = { v -> viewModel.update { it.copy(bairroVelorio = v) } },
                    modifier = Modifier.weight(0.65f)
                )
            }
        }
    }
}

@Composable
private fun MedicoTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    val medicos = rememberMedicos()

    LabeledTextField(
        label = "N° do atestado de óbito",
        value = s.numeroAtestadoObito,
        onValueChange = { v -> viewModel.update { it.copy(numeroAtestadoObito = v) } },
        keyboardType = KeyboardType.Number
    )

    Text("Causas da morte", style = MaterialTheme.typography.titleMedium)
    s.causasMorte.forEachIndexed { index, causa ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabeledTextField(
                label = "Causa ${index + 1}",
                value = causa,
                onValueChange = { v -> viewModel.atualizarCausa(index, v) },
                singleLine = false,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = { viewModel.removerCausa(index) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Remover causa")
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = viewModel::adicionarCausa,
            enabled = s.causasMorte.size < DeclaracaoObitoThresholds.MAX_CAUSAS_MORTE
        ) {
            Text("Adicionar causa")
        }
        OutlinedButton(
            onClick = { viewModel.removerCausa(s.causasMorte.size - 1) },
            enabled = s.causasMorte.size > 1
        ) {
            Text("Remover causa")
        }
    }
    if (s.causasMorte.size >= DeclaracaoObitoThresholds.MAX_CAUSAS_MORTE) {
        Text(
            text = "Limite de ${DeclaracaoObitoThresholds.MAX_CAUSAS_MORTE} causas atingido.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    MedicoFields(
        titulo = "Médico(a) 1",
        uf = s.ufMedico1.ifBlank { null },
        onUfSelected = { v -> viewModel.update { it.copy(ufMedico1 = v) } },
        crm = s.crm1,
        onCrmChange = { v -> viewModel.update { it.copy(crm1 = v) } },
        nome = s.medico1,
        onNomeChange = { v -> viewModel.update { it.copy(medico1 = v) } },
        medicos = medicos
    )

    Spacer(modifier = Modifier.height(12.dp))

    MedicoFields(
        titulo = "Médico(a) 2 (opcional)",
        uf = s.ufMedico2.ifBlank { null },
        onUfSelected = { v -> viewModel.update { it.copy(ufMedico2 = v) } },
        crm = s.crm2,
        onCrmChange = { v -> viewModel.update { it.copy(crm2 = v) } },
        nome = s.medico2,
        onNomeChange = { v -> viewModel.update { it.copy(medico2 = v) } },
        medicos = medicos
    )
}
