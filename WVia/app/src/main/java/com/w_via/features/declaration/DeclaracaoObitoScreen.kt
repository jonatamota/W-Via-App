package com.w_via.features.declaration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.w_via.features.appnavigation.TopBar
import com.w_via.features.components.MedicoFields
import com.w_via.features.components.SuggestionAutocompleteField
import com.w_via.features.components.rememberCartorios
import com.w_via.features.components.rememberMedicos
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.declaration.state.DeclaracaoObitoStep
import com.w_via.features.utils.CartorioData

@Composable
fun DeclaracaoObitoScreen(
    viewModel: DeclaracaoObitoViewModel = viewModel(),
    onConcluir: (DeclaracaoObito) -> Unit,
    navController: NavController,
    bottomBarPadding: Dp
) {
    val state by viewModel.state.collectAsState()
    val steps = DeclaracaoObitoStep.entries
    val progresso = (steps.indexOf(state.step) + 1f) / steps.size

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            TopBar(
                title = "Declaração de óbito",
                showBackButton = true,
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = viewModel::voltar,
                    enabled = state.step != DeclaracaoObitoStep.IDENTIFICACAO
                ) { Text("Voltar") }

                Button(
                    onClick = {
                        if (state.step == DeclaracaoObitoStep.DECLARANTE) {
                            onConcluir(viewModel.toDeclaracaoObito())
                        } else {
                            viewModel.avancar()
                        }
                    },
                    enabled = viewModel.stepAtualValido()
                ) { Text(if (state.step == DeclaracaoObitoStep.DECLARANTE) "Concluir" else "Avançar") }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = tituloStep(state.step), modifier = Modifier.padding(bottom = 8.dp))

            when (state.step) {
                DeclaracaoObitoStep.IDENTIFICACAO -> StepIdentificacao(state, viewModel)
                DeclaracaoObitoStep.ESTADO_CIVIL -> StepEstadoCivil(state, viewModel)
                DeclaracaoObitoStep.DOCUMENTOS -> StepDocumentos(state, viewModel)
                DeclaracaoObitoStep.BENS -> StepBens(state, viewModel)
                DeclaracaoObitoStep.ENDERECO_CARTORIO_INSS -> StepEnderecoCartorioInss(state, viewModel)
                DeclaracaoObitoStep.FILIACAO -> StepFiliacao(state, viewModel)
                DeclaracaoObitoStep.OBITO -> StepObito(state, viewModel)
                DeclaracaoObitoStep.FILHOS -> StepFilhos(state, viewModel)
                DeclaracaoObitoStep.DECLARANTE -> StepDeclarante(state, viewModel)
            }
        }
    }
}

private fun tituloStep(step: DeclaracaoObitoStep): String = when (step) {
    DeclaracaoObitoStep.IDENTIFICACAO -> "Identificação do falecido"
    DeclaracaoObitoStep.ESTADO_CIVIL -> "Estado civil"
    DeclaracaoObitoStep.DOCUMENTOS -> "Documentos (eleitor / reservista)"
    DeclaracaoObitoStep.BENS -> "Bens e testamento"
    DeclaracaoObitoStep.ENDERECO_CARTORIO_INSS -> "Endereço, cartório e INSS"
    DeclaracaoObitoStep.FILIACAO -> "Filiação"
    DeclaracaoObitoStep.OBITO -> "Dados do óbito"
    DeclaracaoObitoStep.FILHOS -> "Filhos"
    DeclaracaoObitoStep.DECLARANTE -> "Dados do declarante"
}

@Composable
private fun StepIdentificacao(s: DeclaracaoObitoFormState, vm: DeclaracaoObitoViewModel) {
    LabeledTextField("Nome completo", s.nome, { v -> vm.update { it.copy(nome = v) } })
    LabeledTextField("Data de nascimento", s.dataNascimento, { v -> vm.update { it.copy(dataNascimento = v) } })
    LabeledTextField("CPF", s.cpf, { v -> vm.update { it.copy(cpf = v) } }, keyboardType = KeyboardType.Number)
    LabeledTextField("RG", s.rg, { v -> vm.update { it.copy(rg = v) } })
    SingleChoiceSelector(
        label = "Sexo",
        options = Sexo.entries,
        selected = s.sexo,
        onSelect = { v -> vm.update { it.copy(sexo = v) } },
        optionLabel = { it.name.lowercase().replaceFirstChar(Char::uppercase) }
    )
    LabeledTextField("Cor", s.cor, { v -> vm.update { it.copy(cor = v) } })
    LabeledTextField("Profissão", s.profissao, { v -> vm.update { it.copy(profissao = v) } })
    LabeledTextField("Natural de", s.natural, { v -> vm.update { it.copy(natural = v) } })
}

@Composable
private fun StepEstadoCivil(s: DeclaracaoObitoFormState, vm: DeclaracaoObitoViewModel) {
    if (!s.perguntarEstadoCivilCasamento()) {
        Text("Não aplicável para a idade informada.")
        return
    }
    SingleChoiceSelector(
        label = "Estado civil",
        options = EstadoCivil.entries,
        selected = s.estadoCivil,
        onSelect = { v -> vm.update { it.copy(estadoCivil = v) } },
        optionLabel = { it.name.lowercase().replaceFirstChar(Char::uppercase) }
    )
    if (s.estadoCivil == EstadoCivil.CASADO) {
        LabeledTextField("Nome do cônjuge", s.nomeConjuge, { v -> vm.update { it.copy(nomeConjuge = v) } })
    }
    if (s.perguntarCasamentoEmCartorio()) {
        SimNaoSelector("Casado em cartório?", s.casamentoEmCartorio, { v -> vm.update { it.copy(casamentoEmCartorio = v) } })
    }
    if (s.exibirCamposCasamentoCartorio()) {
        LabeledTextField("Dia", s.casamentoDia, { v -> vm.update { it.copy(casamentoDia = v) } })
        LabeledTextField("Livro", s.casamentoLivro, { v -> vm.update { it.copy(casamentoLivro = v) } })
        LabeledTextField("Folha", s.casamentoFolha, { v -> vm.update { it.copy(casamentoFolha = v) } })
        LabeledTextField("N°", s.casamentoNumero, { v -> vm.update { it.copy(casamentoNumero = v) } })
    }
}

@Composable
private fun StepDocumentos(s: DeclaracaoObitoFormState, vm: DeclaracaoObitoViewModel) {
    if (s.perguntarEleitor()) {
        SimNaoSelector("Eleitor?", s.eleitor, { v -> vm.update { it.copy(eleitor = v) } })
    }
    if (s.exibirCamposEleitor()) {
        LabeledTextField("UF (título)", s.eleitorUf, { v -> vm.update { it.copy(eleitorUf = v) } })
        LabeledTextField("Cidade (título)", s.eleitorCidade, { v -> vm.update { it.copy(eleitorCidade = v) } })
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledTextField("Zona", s.eleitorZona, { v -> vm.update { it.copy(eleitorZona = v) } }, modifier = Modifier.weight(1f))
            LabeledTextField("Seção", s.eleitorSecao, { v -> vm.update { it.copy(eleitorSecao = v) } }, modifier = Modifier.weight(1f))
        }
        LabeledTextField("N° do título", s.eleitorNumeroTitulo, { v -> vm.update { it.copy(eleitorNumeroTitulo = v) } })
    }

    if (s.perguntarReservista()) {
        SimNaoSelector("Reservista?", s.reservista, { v -> vm.update { it.copy(reservista = v) } })
    }
    if (s.exibirCamposReservista()) {
        LabeledTextField("Carteira", s.reservistaCarteira, { v -> vm.update { it.copy(reservistaCarteira = v) } })
        LabeledTextField("N° série", s.reservistaNumeroSerie, { v -> vm.update { it.copy(reservistaNumeroSerie = v) } })
        LabeledTextField("CSM", s.reservistaCsm, { v -> vm.update { it.copy(reservistaCsm = v) } })
        LabeledTextField("RM", s.reservistaRm, { v -> vm.update { it.copy(reservistaRm = v) } })
    }

    if (!s.perguntarEleitor() && !s.perguntarReservista()) {
        Text("Não aplicável para a idade/sexo informados.")
    }
}

@Composable
private fun StepBens(s: DeclaracaoObitoFormState, vm: DeclaracaoObitoViewModel) {
    SimNaoSelector("Deixa bens?", s.deixaBens, { v -> vm.update { it.copy(deixaBens = v) } })
    if (s.exibirTestamento()) {
        SimNaoSelector("Testamento?", s.testamento, { v -> vm.update { it.copy(testamento = v) } })
    }
}

@Composable
private fun StepEnderecoCartorioInss(s: DeclaracaoObitoFormState, vm: DeclaracaoObitoViewModel) {
    LabeledTextField("Cartório", s.cartorio, { v -> vm.update { it.copy(cartorio = v) } })
    LabeledTextField("Livro", s.livro, { v -> vm.update { it.copy(livro = v) } })
    LabeledTextField("Folha", s.folha, { v -> vm.update { it.copy(folha = v) } })
    LabeledTextField("N°", s.numero, { v -> vm.update { it.copy(numero = v) } })
    LabeledTextField("Endereço", s.endereco, { v -> vm.update { it.copy(endereco = v) } })
    LabeledTextField("CEP", s.cep, { v -> vm.update { it.copy(cep = v) } }, keyboardType = KeyboardType.Number)
    LabeledTextField("Cidade", s.cidade, { v -> vm.update { it.copy(cidade = v) } })
    LabeledTextField("INSS", s.inss, { v -> vm.update { it.copy(inss = v) } })
    LabeledTextField("N° benefício", s.numeroBeneficio, { v -> vm.update { it.copy(numeroBeneficio = v) } })
}

@Composable
private fun StepFiliacao(s: DeclaracaoObitoFormState, vm: DeclaracaoObitoViewModel) {
    Text("Pai")
    LabeledTextField("Nome do pai", s.nomePai, { v -> vm.update { it.copy(nomePai = v) } })
    LabeledTextField("Natural de", s.naturalPai, { v -> vm.update { it.copy(naturalPai = v) } })
    LabeledTextField("Estado civil", s.estadoCivilPai, { v -> vm.update { it.copy(estadoCivilPai = v) } })
    LabeledTextField("Profissão", s.profissaoPai, { v -> vm.update { it.copy(profissaoPai = v) } })
    Text("Mãe")
    LabeledTextField("Nome da mãe", s.nomeMae, { v -> vm.update { it.copy(nomeMae = v) } })
    LabeledTextField("Natural de", s.naturalMae, { v -> vm.update { it.copy(naturalMae = v) } })
    LabeledTextField("Estado civil", s.estadoCivilMae, { v -> vm.update { it.copy(estadoCivilMae = v) } })
    LabeledTextField("Profissão", s.profissaoMae, { v -> vm.update { it.copy(profissaoMae = v) } })
    if (s.alertaFiliacaoPodeSerDesconhecidaCompat()) {
        Text("Falecido com idade avançada — filiação pode ser desconhecida pela declarante.")
    }
}

// Extensão local para reaproveitar a checagem sem duplicar o threshold aqui
private fun DeclaracaoObitoFormState.alertaFiliacaoPodeSerDesconhecidaCompat(): Boolean =
    (idade.toIntOrNull() ?: 0) >= DeclaracaoObitoThresholds.IDADE_ALERTA_FILIACAO_DESCONHECIDA

@Composable
private fun StepObito(s: DeclaracaoObitoFormState, vm: DeclaracaoObitoViewModel) {
    val medicos = rememberMedicos()
    SingleChoiceSelector(
        label = "Local de falecimento",
        options = TipoLocalObito.entries,
        selected = s.tipoLocalObito,
        onSelect = { v -> vm.update { it.copy(tipoLocalObito = v) } },
        optionLabel = { it.label }
    )
    LabeledTextField("Nome do local", s.nomeLocalObito, { v -> vm.update { it.copy(nomeLocalObito = v) } })
    LabeledTextField("Cidade", s.cidadeObito, { v -> vm.update { it.copy(cidadeObito = v) } })
    LabeledTextField("UF", s.ufObito, { v -> vm.update { it.copy(ufObito = v) } })
    LabeledTextField("Data do óbito", s.dataObito, { v -> vm.update { it.copy(dataObito = v) } })
    LabeledTextField("Hora do falecimento", s.horaFalecimento, { v -> vm.update { it.copy(horaFalecimento = v) } })
    LabeledTextField("N° do atestado de óbito", s.numeroAtestadoObito, { v -> vm.update { it.copy(numeroAtestadoObito = v) } })
    Text("Causas da morte")
    s.causasMorte.forEachIndexed { index, causa ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabeledTextField(
                label = "Causa ${index + 1}",
                value = causa,
                onValueChange = { v -> vm.atualizarCausa(index, v) },
                singleLine = false,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = { vm.removerCausa(index) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Remover causa")
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = vm::adicionarCausa) { Text("Adicionar causa") }
        OutlinedButton(
            onClick = { vm.removerCausa(s.causasMorte.size - 1) },
            enabled = s.causasMorte.size > 1
        ) { Text("Remover causa") }
    }
    LabeledTextField("Cemitério", s.cemiterio, { v -> vm.update { it.copy(cemiterio = v) } })
    LabeledTextField("Hora do sepultamento", s.horaSepultamento, { v -> vm.update { it.copy(horaSepultamento = v) } })
    LabeledTextField("Local do velório", s.localVelorio, { v -> vm.update { it.copy(localVelorio = v) } })
    LabeledTextField("Hora do velório", s.horaVelorio, { v -> vm.update { it.copy(horaVelorio = v) } })
    LabeledTextField("Endereço", s.enderecoVelorio, { v -> vm.update { it.copy(enderecoVelorio = v) } })
    LabeledTextField("N°", s.numeroVelorio, { v -> vm.update { it.copy(numeroVelorio = v) } })
    LabeledTextField("Bairro", s.bairroVelorio, { v -> vm.update { it.copy(bairroVelorio = v) } })
    LabeledTextField("Cidade", s.cidadeVelorio, { v -> vm.update { it.copy(cidadeVelorio = v) } })
    LabeledTextField("CEP", s.cepVelorio, { v -> vm.update { it.copy(cepVelorio = v) } }, keyboardType = KeyboardType.Number)
    LabeledTextField("UF", s.ufVelorio, { v -> vm.update { it.copy(ufVelorio = v) } })
    MedicoFields(
        titulo = "Médico(a) 1",
        uf = s.ufMedico1.ifBlank { null },
        onUfSelected = { v -> vm.update { it.copy(ufMedico1 = v) } },
        crm = s.crm1,
        onCrmChange = { v -> vm.update { it.copy(crm1 = v) } },
        nome = s.medico1,
        onNomeChange = { v -> vm.update { it.copy(medico1 = v) } },
        medicos = medicos
    )
    MedicoFields(
        titulo = "Médico(a) 2 (opcional)",
        uf = s.ufMedico2.ifBlank { null },
        onUfSelected = { v -> vm.update { it.copy(ufMedico2 = v) } },
        crm = s.crm2,
        onCrmChange = { v -> vm.update { it.copy(crm2 = v) } },
        nome = s.medico2,
        onNomeChange = { v -> vm.update { it.copy(medico2 = v) } },
        medicos = medicos
    )
}

@Composable
private fun StepFilhos(s: DeclaracaoObitoFormState, vm: DeclaracaoObitoViewModel) {
    if (!s.perguntarFilhos()) {
        Text("Não aplicável para a idade informada.")
        return
    }
    s.filhos.forEachIndexed { index, nome ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabeledTextField(
                label = "Filho(a) ${index + 1}",
                value = nome,
                onValueChange = { v -> vm.atualizarFilho(index, v) },
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = { vm.removerFilho(index) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Remover filho")
            }
        }
    }
    OutlinedButton(onClick = vm::adicionarFilho) { Text("Adicionar filho(a)") }
}

@Composable
private fun StepDeclarante(s: DeclaracaoObitoFormState, vm: DeclaracaoObitoViewModel) {
    val cartorios = rememberCartorios()
    val cartorioNomes = remember(cartorios, s.ufDeclarante, s.municipioDeclarante) {
        val uf = s.ufDeclarante.trim().uppercase()
        val infos = when {
            s.municipioDeclarante.isNotBlank() ->
                CartorioData.cartoriosPara(cartorios, uf, s.municipioDeclarante)
            uf.isNotBlank() ->
                cartorios[uf]?.values?.flatten().orEmpty()
            else -> emptyList()
        }
        infos.map { it.nome }
    }
    SuggestionAutocompleteField(
        value = s.declaranteCartorio,
        onValueChange = { v -> vm.update { it.copy(declaranteCartorio = v) } },
        suggestions = cartorioNomes,
        placeholder = "Cartório"
    )
    LabeledTextField("Telefone", s.declaranteTelefone, { v -> vm.update { it.copy(declaranteTelefone = v) } }, keyboardType = KeyboardType.Phone)
    LabeledTextField("Nome do declarante", s.nomeDeclarante, { v -> vm.update { it.copy(nomeDeclarante = v) } })
    LabeledTextField("Grau de parentesco", s.grauParentesco, { v -> vm.update { it.copy(grauParentesco = v) } })
    LabeledTextField("Profissão", s.profissaoDeclarante, { v -> vm.update { it.copy(profissaoDeclarante = v) } })
    LabeledTextField("RG", s.rgDeclarante, { v -> vm.update { it.copy(rgDeclarante = v) } })
    LabeledTextField("CPF", s.cpfDeclarante, { v -> vm.update { it.copy(cpfDeclarante = v) } }, keyboardType = KeyboardType.Number)
    LabeledTextField("Residência", s.residenciaDeclarante, { v -> vm.update { it.copy(residenciaDeclarante = v) } })
    LabeledTextField("Bairro", s.bairroDeclarante, { v -> vm.update { it.copy(bairroDeclarante = v) } })
    LabeledTextField("Município", s.municipioDeclarante, { v -> vm.update { it.copy(municipioDeclarante = v) } })
    LabeledTextField("CEP", s.cepDeclarante, { v -> vm.update { it.copy(cepDeclarante = v) } }, keyboardType = KeyboardType.Number)
    LabeledTextField("UF", s.ufDeclarante, { v -> vm.update { it.copy(ufDeclarante = v) } })
    LabeledTextField("Celular", s.celularDeclarante, { v -> vm.update { it.copy(celularDeclarante = v) } }, keyboardType = KeyboardType.Phone)
    LabeledTextField("E-mail", s.emailDeclarante, { v -> vm.update { it.copy(emailDeclarante = v) } }, keyboardType = KeyboardType.Email, uppercase = false)
}