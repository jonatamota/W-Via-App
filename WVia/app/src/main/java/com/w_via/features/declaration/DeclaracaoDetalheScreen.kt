package com.w_via.features.declaration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.designsystem.bottomsheets.DeleteConfirmationBottomSheet
import com.w_via.features.appnavigation.TopBar
import com.w_via.features.declaration.createdeclaration.DeclarationDraftViewModel
import com.w_via.features.declaration.data.DeclaracaoRepository
import com.w_via.features.declaration.layouts.DeclaracaoObitoPDFLayout
import com.w_via.features.declaration.layouts.numeroExibido
import com.w_via.features.receipt.composeToPdf.PdfDocumentGenerator
import com.w_via.features.receipt.composeToPdf.PdfPageSize
import com.w_via.features.receipt.composeToPdf.pdfSizeKb
import com.w_via.features.routes.Routes
import com.w_via.ui.components.DetailSection
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detalhes de uma declaração salva. Carrega o modelo do Room e, no fim, gera
 * o PDF seguindo o fluxo da tela de sucesso.
 */
@Composable
fun DeclaracaoDetalheScreen(
    navController: NavController? = null,
    declaracaoId: Long
) {
    val repository: DeclaracaoRepository = koinInject()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfGenerator = remember { PdfDocumentGenerator(context) }

    var isGenerating by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var isDeleting by rememberSaveable { mutableStateOf(false) }

    val declaracao by produceState<DeclaracaoObito?>(initialValue = null, key1 = declaracaoId) {
        value = repository.getById(declaracaoId)
    }
    val data = declaracao

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            TopBar(
                title = "Detalhes da declaração",
                showBackButton = true,
                onBackPress = { navController?.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (data == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Declaração não encontrada",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 120.dp
                    )
                ) {
                    item { DetailSection("Falecido", falecidoItens(data.falecido)) }
                    item { DetailSection("Óbito", obitoItens(data.obito)) }
                    if (data.filhos.isNotEmpty()) {
                        item {
                            DetailSection(
                                title = "Filhos",
                                items = data.filhos.mapIndexed { index, filho ->
                                    "Filho ${index + 1}" to filho.nome
                                }
                            )
                        }
                    }
                    item { DetailSection("Declarante", declaranteItens(data.declarante)) }
                    if (data.observacoes.isNotBlank()) {
                        item { DetailSection("Observações", listOf("Observações" to data.observacoes)) }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            scope.launch {
                                isGenerating = true
                                errorMessage = null
                                val fileName = "declaracao_obito_${declaracaoId}.pdf"
                                val uri = pdfGenerator.generate(
                                    fileName = fileName,
                                    pageSize = PdfPageSize.A4(72),
                                    pages = listOf {
                                        DeclaracaoObitoPDFLayout(
                                            declaracao = data,
                                            numero = numeroExibido(declaracaoId)
                                        )
                                    }
                                )
                                if (uri != null) {
                                    val pdfSizeKb = pdfSizeKb(context, uri, fileName)
                                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                                    navController?.currentBackStackEntry?.savedStateHandle?.apply {
                                        set("pdfUri", uri.toString())
                                        set("pdfName", uri.lastPathSegment ?: "declaracao_obito.pdf")
                                        set("pdfSize", pdfSizeKb)
                                        set("generatedAt", sdf.format(Date()))
                                    }
                                    navController?.navigate(Routes.DECLARACAO_SUCESSO) {
                                        launchSingleTop = true
                                    }
                                } else {
                                    errorMessage = "Erro ao gerar o PDF. Tente novamente."
                                }
                                isGenerating = false
                            }
                        },
                        text = "Gerar PDF",
                        isLoading = isGenerating,
                        isEnabled = !isGenerating
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrimaryButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                DeclarationDraftViewModel.editSeed = data
                                navController?.navigate(Routes.DECLARATION_GRID)
                            },
                            text = "Editar",
                            isEnabled = !isGenerating && !isDeleting
                        )
                        PrimaryButton(
                            modifier = Modifier.weight(1f),
                            onClick = { showDeleteConfirm = true },
                            text = "Excluir",
                            containerColorEnable = MaterialTheme.colorScheme.error,
                            isEnabled = !isGenerating && !isDeleting
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmationBottomSheet(
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                scope.launch {
                    isDeleting = true
                    repository.delete(declaracaoId)
                    isDeleting = false
                    navController?.popBackStack()
                }
            },
            title = "Excluir declaração?",
            message = "A declaração será excluída."
        )
    }
}

private fun falecidoItens(falecido: DadosFalecido): List<Pair<String, String>> = buildList {
    add("Nome" to falecido.nome)
    add("CPF" to falecido.cpf)
    add("RG" to falecido.rg)
    add("Data de nascimento" to falecido.dataNascimento)
    add("Idade" to falecido.idade.toString())
    add("Sexo" to falecido.sexo.name)
    add("Cor" to falecido.cor)
    add("Profissão" to falecido.profissao)
    add("Natural de" to falecido.natural)
    add("Natural - UF" to falecido.ufNatural)
    add("Cartório" to falecido.cartorio)
    add("Livro" to falecido.livro)
    add("Folha" to falecido.folha)
    add("Número" to falecido.numero)
    add("Endereço" to falecido.endereco)
    add("CEP" to falecido.cep)
    add("Cidade" to falecido.cidade)
    add("UF" to falecido.uf)
    falecido.estadoCivil?.let { add("Estado civil" to it.name) }
    falecido.nomeConjuge?.let { add("Nome do cônjuge" to it) }
    if (falecido.exibirCamposCasamentoCartorio()) {
        falecido.casamentoCartorioInfo?.let {
            add("Casamento - Dia" to it.dia)
            add("Casamento - Livro" to it.livro)
            add("Casamento - Folha" to it.folha)
            add("Casamento - Número" to it.numero)
        }
    }
    falecido.deixaBens?.let { add("Deixa bens" to if (it) "Sim" else "Não") }
    if (falecido.exibirTestamento()) add("Testamento" to "Sim")
    falecido.inss?.let { add("INSS" to it) }
    falecido.numeroBeneficio?.let { add("Nº do benefício" to it) }
    if (falecido.exibirCamposEleitor()) {
        falecido.eleitorInfo?.let {
            add("Eleitor - Cidade" to it.cidade)
            add("Eleitor - UF" to it.uf)
            add("Eleitor - Zona" to it.zona)
            add("Eleitor - Seção" to it.secao)
            add("Eleitor - Nº do título" to it.numeroTitulo)
        }
    }
    if (falecido.exibirCamposReservista()) {
        falecido.reservistaInfo?.let {
            add("Reservista - Carteira" to it.carteira)
            add("Reservista - Nº de série" to it.numeroSerie)
            add("Reservista - CSM" to it.csm)
            add("Reservista - RM" to it.rm)
        }
    }
    with(falecido.filiacao) {
        nomePai?.let { add("Pai" to it) }
        naturalPai?.let { add("Natural (pai)" to it) }
        ufNaturalPai?.let { add("Natural - UF (pai)" to it) }
        estadoCivilPai?.let { add("Estado civil (pai)" to it) }
        profissaoPai?.let { add("Profissão (pai)" to it) }
        nomeMae?.let { add("Mãe" to it) }
        naturalMae?.let { add("Natural (mãe)" to it) }
        ufNaturalMae?.let { add("Natural - UF (mãe)" to it) }
        estadoCivilMae?.let { add("Estado civil (mãe)" to it) }
        profissaoMae?.let { add("Profissão (mãe)" to it) }
    }
}.filterNot { it.second.isBlank() }

private fun obitoItens(obito: DadosObito): List<Pair<String, String>> = buildList {
    add("Tipo de local" to obito.tipoLocal)
    add("Local" to obito.nomeLocal)
    add("Cidade" to obito.cidade)
    add("UF" to obito.uf)
    add("Data do óbito" to obito.dataObito)
    add("Hora do falecimento" to obito.horaFalecimento)
    add("Nº do atestado" to obito.numeroAtestadoObito)
    add("Causa da morte" to obito.causaMorte)
    add("Cemitério" to obito.cemiterio)
    add("Hora do sepultamento" to obito.horaSepultamento)
    add("Local do velório" to obito.localVelorio)
    add("Hora do velório" to obito.horaVelorio)
    add("Endereço" to obito.endereco)
    add("Número" to obito.numero)
    add("Bairro" to obito.bairro)
    add("Cidade do velório" to obito.cidadeVelorio)
    add("CEP" to obito.cep)
    add("UF (velório)" to obito.uf2)
    add("Médico 1" to obito.medico1)
    add("CRM 1" to obito.crm1)
    add("UF médico 1" to obito.ufMedico1)
    obito.medico2?.let { add("Médico 2" to it) }
    obito.crm2?.let { add("CRM 2" to it) }
    obito.ufMedico2?.let { add("UF médico 2" to it) }
}.filterNot { it.second.isBlank() }

private fun declaranteItens(declarante: Declarante): List<Pair<String, String>> = buildList {
    add("Cartório" to declarante.cartorio)
    add("Telefone" to declarante.telefone)
    add("N°" to declarante.numero)
    add("Nome" to declarante.nomeDeclarante)
    add("Grau de parentesco" to declarante.grauParentesco)
    add("Profissão" to declarante.profissao)
    add("RG" to declarante.rg)
    add("CPF" to declarante.cpf)
    add("Residência" to declarante.residencia)
    add("Bairro" to declarante.bairro)
    add("Município" to declarante.municipio)
    add("CEP" to declarante.cep)
    add("UF" to declarante.uf)
    add("Celular" to declarante.celular)
    add("E-mail" to declarante.email)
    add("Endereço do cartório" to declarante.enderecoCartorio)
    add("N° do cartório" to declarante.numeroCartorio)
    add("Cidade do cartório" to declarante.cidadeCartorio)
    add("UF do cartório" to declarante.ufCartorio)
    add("E-mail do cartório" to declarante.emailCartorio)
    add("Oficial do cartório" to declarante.oficialCartorio)
}.filterNot { it.second.isBlank() }
