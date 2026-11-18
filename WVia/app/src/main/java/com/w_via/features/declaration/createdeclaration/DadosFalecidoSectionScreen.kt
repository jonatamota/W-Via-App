package com.w_via.features.declaration.createdeclaration

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.w_via.features.components.AccordionSelect
import com.w_via.features.components.CepSearchField
import com.w_via.features.components.CpfSearchFieldWithFeedback
import com.w_via.features.components.NaturalDeFields
import com.w_via.features.components.SuggestionAutocompleteField
import com.w_via.features.components.UFAndCityFields
import com.w_via.features.components.rememberMunicipios
import com.w_via.features.components.rememberProfissoes
import com.w_via.features.declaration.DeclarationFieldErrors
import com.w_via.features.declaration.EstadoCivil
import com.w_via.features.declaration.ExcecaoAutoInfo
import com.w_via.features.declaration.LabeledTextField
import com.w_via.features.declaration.MaskedLabeledField
import com.w_via.features.declaration.Sexo
import com.w_via.features.declaration.SimNaoSelector
import com.w_via.features.declaration.SingleChoiceSelector
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.utils.AgeCalculator
import com.w_via.features.utils.InssFormatter
import com.w_via.features.utils.RGFormatter

/**
 * Regras de liberação das abas de "Dados do Falecido", isoladas para testes.
 * As abas só aparecem quando os campos de que dependem foram preenchidos.
 */
internal object FalecidoTabs {
    const val IDENTIFICACAO = "Identificação"
    const val ESTADO_CIVIL = "Estado civil"
    const val DOCUMENTOS = "Documentos"
    const val ENDERECO = "Endereço"
    const val CARTORIO_INSS = "Cartório/INSS"

    /** Abas visíveis no estado atual, sempre na ordem fixa. */
    fun availableTabs(s: DeclaracaoObitoFormState): List<String> {
        val tabs = mutableListOf(IDENTIFICACAO)
        if (s.dataNascimento.isNotBlank()) {
            tabs += ESTADO_CIVIL
        }
        if (s.sexo != null) {
            tabs += DOCUMENTOS
        }
        if (identificacaoCompleta(s)) {
            tabs += ENDERECO
            tabs += CARTORIO_INSS
        }
        return tabs
    }

    private fun identificacaoCompleta(s: DeclaracaoObitoFormState): Boolean =
        s.nome.isNotBlank() &&
            s.cpf.isNotBlank() &&
            s.dataNascimento.isNotBlank() &&
            s.sexo != null &&
            s.rg.isNotBlank() &&
            s.cor.isNotBlank() &&
            s.profissao.isNotBlank() &&
            s.natural.isNotBlank() &&
            s.ufNatural.isNotBlank()
}

/**
 * Seção "Dados do Falecido". Como concentra muitos campos, é dividida em abas:
 * Identificação, Estado civil, Documentos, Endereço e Cartório/INSS. As regras de
 * exibição/exclusão (menor de idade, sexo, estado civil) são aplicadas por aba.
 * As abas ficam ocultas até que os campos de que dependem sejam preenchidos.
 */
@Composable
fun DadosFalecidoSectionScreen(
    navController: NavController,
    viewModel: DeclarationDraftViewModel,
) {
    val s by viewModel.state.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = FalecidoTabs.availableTabs(s)
    val safeIndex = selectedTab.coerceIn(0, tabs.lastIndex.coerceAtLeast(0))
    val tabLabel = tabs.getOrNull(safeIndex) ?: FalecidoTabs.IDENTIFICACAO

    DeclarationSectionHost(
        section = DeclarationSection.FALECIDO,
        navController = navController,
        viewModel = viewModel,
        tabCount = tabs.size,
        currentTab = safeIndex,
        onTabChange = { selectedTab = it.coerceIn(0, tabs.lastIndex.coerceAtLeast(0)) }
    ) { state ->
        ScrollableTabRow(
            selectedTabIndex = safeIndex,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = safeIndex == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (tabLabel) {
            FalecidoTabs.IDENTIFICACAO -> IdentificacaoTab(state, viewModel)
            FalecidoTabs.ESTADO_CIVIL -> EstadoCivilTab(state, viewModel)
            FalecidoTabs.DOCUMENTOS -> DocumentosTab(state, viewModel)
            FalecidoTabs.ENDERECO -> EnderecoTab(state, viewModel)
            FalecidoTabs.CARTORIO_INSS -> CartorioInssTab(state, viewModel)
        }
    }
}

private val RACAS = listOf("Branca", "Preta", "Parda", "Amarela", "Indígena")

private val ESTADO_CIVIL_OPTIONS = mapOf(
    "Solteiro" to EstadoCivil.SOLTEIRO,
    "Casado" to EstadoCivil.CASADO,
    "União estável" to EstadoCivil.UNIAO_ESTAVEL,
    "Divorciado" to EstadoCivil.DIVORCIADO,
    "Viúvo" to EstadoCivil.VIUVO
)

private fun estadoCivilLabel(estado: EstadoCivil?): String? =
    estado?.let { selected ->
        ESTADO_CIVIL_OPTIONS.entries.firstOrNull { it.value == selected }?.key
    }

@Composable
private fun IdentificacaoTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    val municipios = rememberMunicipios()
    val profissoes = rememberProfissoes()
    val cpfError = DeclarationFieldErrors.cpf(s.cpf)

    LabeledTextField("Nome completo", s.nome, { v -> viewModel.update { it.copy(nome = v) } })
    DeclarationDateField(
        value = s.dataNascimento,
        label = "Data de nascimento",
        onValueChange = { v ->
            viewModel.update {
                it.copy(
                    dataNascimento = v,
                    idade = AgeCalculator.yearsOnly(v)
                )
            }
        }
    )

    SingleChoiceSelector(
        label = "Sexo",
        options = Sexo.entries,
        selected = s.sexo,
        onSelect = { v -> viewModel.update { it.copy(sexo = v) } },
        optionLabel = { it.name.lowercase().replaceFirstChar(Char::uppercase) }
    )
    AccordionSelect(
        label = "Cor/Raça",
        options = RACAS,
        selectedValue = s.cor.ifBlank { null },
        onValueSelected = { v -> viewModel.update { it.copy(cor = v) } }
    )
    SuggestionAutocompleteField(
        value = s.profissao,
        onValueChange = { v -> viewModel.update { it.copy(profissao = v) } },
        suggestions = profissoes,
        placeholder = "Profissão"
    )
    NaturalDeFields(
        naturalDe = s.natural,
        onNaturalDeChange = { v -> viewModel.update { it.copy(natural = v) } },
        uf = s.ufNatural.ifBlank { null },
        onUfSelected = { v -> viewModel.update { it.copy(ufNatural = v) } },
        municipios = municipios
    )
    CpfSearchFieldWithFeedback(
        value = s.cpf,
        onValueChange = { v -> viewModel.update { it.copy(cpf = v) } },
        errorMessage = cpfError
    )
    MaskedLabeledField(
        label = "RG",
        value = s.rg,
        onValueChange = { v -> viewModel.update { it.copy(rg = v) } },
        format = RGFormatter::formatWithCursor,
        keyboardType = KeyboardType.Number
    )
}

@Composable
private fun EstadoCivilTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    if (s.mostrarEstadoCivil()) {
        AccordionSelect(
            label = "Estado civil",
            options = ESTADO_CIVIL_OPTIONS.keys.toList(),
            selectedValue = estadoCivilLabel(s.estadoCivil),
            onValueSelected = { label ->
                viewModel.update { it.copy(estadoCivil = ESTADO_CIVIL_OPTIONS[label]) }
            }
        )

        if (s.mostrarConjuge()) {
            val conjugeLabel = if (s.conjugeObrigatorio()) {
                "Nome do cônjuge"
            } else {
                "Nome do cônjuge (opcional)"
            }
            LabeledTextField(
                label = conjugeLabel,
                value = s.nomeConjuge,
                onValueChange = { v -> viewModel.update { it.copy(nomeConjuge = v) } }
            )
        }

        if (s.perguntarCasamentoEmCartorio()) {
            SimNaoSelector(
                label = "Casado em cartório?",
                selected = s.casamentoEmCartorio,
                onSelect = { v -> viewModel.update { it.copy(casamentoEmCartorio = v) } }
            )
        }
        if (s.exibirCamposCasamentoCartorio()) {
            LabeledTextField(
                label = "Dia do casamento",
                value = s.casamentoDia,
                onValueChange = { v -> viewModel.update { it.copy(casamentoDia = v) } },
                keyboardType = KeyboardType.Number
            )
            LabeledTextField("Livro", s.casamentoLivro, { v -> viewModel.update { it.copy(casamentoLivro = v) } })
            LabeledTextField(
                label = "Folha",
                value = s.casamentoFolha,
                onValueChange = { v -> viewModel.update { it.copy(casamentoFolha = v) } },
                keyboardType = KeyboardType.Number
            )
            LabeledTextField(
                label = "N°",
                value = s.casamentoNumero,
                onValueChange = { v -> viewModel.update { it.copy(casamentoNumero = v) } },
                keyboardType = KeyboardType.Number
            )
        }
    } else if (s.idade.toIntOrNull() != null && s.estadoCivilAutomaticoSolteiro()) {
        ExcecaoAutoInfo(
            mensagem = "Estado civil automático: Solteiro (menor de 16 anos).",
            acao = "Caso excepcional — informar estado civil mesmo assim",
            onReveal = { viewModel.update { it.copy(excecaoEstadoCivil = true) } }
        )
    } else {
        Text(
            text = "Informe a data de nascimento para habilitar o estado civil.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DocumentosTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    val municipios = rememberMunicipios()
    if (s.perguntarEleitor()) {
        SimNaoSelector(
            label = "Eleitor?",
            selected = s.eleitor,
            onSelect = { v -> viewModel.update { it.copy(eleitor = v) } }
        )
    }
    if (s.exibirCamposEleitor()) {
        UFAndCityFields(
            uf = s.eleitorUf.ifBlank { null },
            onUfSelected = { v -> viewModel.update { it.copy(eleitorUf = v) } },
            city = s.eleitorCidade,
            onCityChange = { v -> viewModel.update { it.copy(eleitorCidade = v) } },
            municipios = municipios
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabeledTextField(
                label = "Zona",
                value = s.eleitorZona,
                onValueChange = { v -> viewModel.update { it.copy(eleitorZona = v) } },
                keyboardType = KeyboardType.Number,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            )
            LabeledTextField(
                label = "Seção",
                value = s.eleitorSecao,
                onValueChange = { v -> viewModel.update { it.copy(eleitorSecao = v) } },
                keyboardType = KeyboardType.Number,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }
        LabeledTextField(
            label = "N° do título",
            value = s.eleitorNumeroTitulo,
            onValueChange = { v -> viewModel.update { it.copy(eleitorNumeroTitulo = v) } },
            keyboardType = KeyboardType.Number
        )
    }

    if (s.mostrarReservista()) {
        SimNaoSelector(
            label = "Reservista?",
            selected = s.reservista,
            onSelect = { v -> viewModel.update { it.copy(reservista = v) } }
        )
        if (s.exibirCamposReservista()) {
            LabeledTextField("Carteira", s.reservistaCarteira, { v -> viewModel.update { it.copy(reservistaCarteira = v) } })
            LabeledTextField(
                label = "N° série",
                value = s.reservistaNumeroSerie,
                onValueChange = { v -> viewModel.update { it.copy(reservistaNumeroSerie = v) } },
                keyboardType = KeyboardType.Number
            )
            LabeledTextField("CSM", s.reservistaCsm, { v -> viewModel.update { it.copy(reservistaCsm = v) } })
            LabeledTextField("RM", s.reservistaRm, { v -> viewModel.update { it.copy(reservistaRm = v) } })
        }
    } else {
        val mensagem = when {
            s.sexo == Sexo.FEMININO -> "Reservista não aplicável para o sexo feminino."
            s.idade.toIntOrNull() == null -> "Informe a idade e o sexo para avaliar o reservista."
            else -> "Reservista não aplicável para a faixa etária informada."
        }
        ExcecaoAutoInfo(
            mensagem = mensagem,
            acao = "Caso excepcional — informar reservista mesmo assim",
            onReveal = { viewModel.update { it.copy(excecaoReservista = true) } }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    SimNaoSelector(
        label = "Deixa bens?",
        selected = s.deixaBens,
        onSelect = { v -> viewModel.update { it.copy(deixaBens = v) } }
    )
    if (s.exibirTestamento()) {
        SimNaoSelector(
            label = "Testamento?",
            selected = s.testamento,
            onSelect = { v -> viewModel.update { it.copy(testamento = v) } }
        )
    }
}

@Composable
private fun EnderecoTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    val municipios = rememberMunicipios()
    val cepError = DeclarationFieldErrors.cep(s.cep)
    val buscandoCep by viewModel.buscandoCep.collectAsState()
    val cepBuscaError by viewModel.cepBuscaError.collectAsState()

    CepSearchField(
        value = s.cep,
        onValueChange = { v ->
            viewModel.clearCepBuscaError()
            viewModel.update { it.copy(cep = v) }
        },
        onSearchClick = viewModel::buscarCepFalecido,
        isLoading = buscandoCep,
        isError = cepError != null || cepBuscaError != null,
        supportingText = cepBuscaError ?: cepError
    )
    UFAndCityFields(
        uf = s.uf.ifBlank { null },
        onUfSelected = { v -> viewModel.update { it.copy(uf = v) } },
        city = s.cidade,
        onCityChange = { v -> viewModel.update { it.copy(cidade = v) } },
        municipios = municipios
    )
    LabeledTextField("Endereço", s.endereco, { v -> viewModel.update { it.copy(endereco = v) } })
}

@Composable
private fun CartorioInssTab(s: DeclaracaoObitoFormState, viewModel: DeclarationDraftViewModel) {
    val municipios = rememberMunicipios()

    LabeledTextField("Cartório", s.cartorio, { v -> viewModel.update { it.copy(cartorio = v) } })
    LabeledTextField("Livro", s.livro, { v -> viewModel.update { it.copy(livro = v) } })
    LabeledTextField(
        label = "Folha",
        value = s.folha,
        onValueChange = { v -> viewModel.update { it.copy(folha = v) } },
        keyboardType = KeyboardType.Number
    )
    LabeledTextField(
        label = "N°",
        value = s.numero,
        onValueChange = { v -> viewModel.update { it.copy(numero = v) } },
        keyboardType = KeyboardType.Number
    )
    UFAndCityFields(
        uf = s.uf.ifBlank { null },
        onUfSelected = { v -> viewModel.update { it.copy(uf = v) } },
        city = s.cidade,
        onCityChange = { v -> viewModel.update { it.copy(cidade = v) } },
        municipios = municipios
    )
    MaskedLabeledField(
        label = "INSS",
        value = s.inss,
        onValueChange = { v -> viewModel.update { it.copy(inss = v) } },
        format = InssFormatter::formatWithCursor
    )
    LabeledTextField(
        label = "N° benefício",
        value = s.numeroBeneficio,
        onValueChange = { v -> viewModel.update { it.copy(numeroBeneficio = v) } },
        keyboardType = KeyboardType.Number
    )
}
