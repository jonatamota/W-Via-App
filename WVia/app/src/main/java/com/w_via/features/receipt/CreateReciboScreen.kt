package com.w_via.features.receipt

import android.icu.text.SimpleDateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.designsystem.bottomsheets.SaveConfirmationBottomSheet
import com.w_via.features.appnavigation.TopBar
import com.w_via.features.company.data.EmpresaRepository
import com.w_via.features.datamycnpj.LogoStorage
import com.w_via.features.datamycnpj.logoDisponivel
import com.w_via.features.receipt.composeToPdf.PdfDocumentGenerator
import com.w_via.features.receipt.composeToPdf.PdfPageSize
import com.w_via.features.receipt.composeToPdf.pdfSizeKb
import com.w_via.features.receipt.data.ReciboRepository
import com.w_via.features.receipt.layouts.ReciboPDFLayout
import com.w_via.features.receipt.steps.Step1Contratante
import com.w_via.features.receipt.steps.Step2Falecido
import com.w_via.features.receipt.steps.Step3Funeral
import com.w_via.features.receipt.steps.Step4ServicoPagamento
import com.w_via.features.routes.Routes
import com.w_via.opencnpj.CNPJFormatter
import com.w_via.features.components.MissingFieldsBottomSheet
import com.w_via.features.sync.CompanyProfileSync
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.w_via.ui.theme.colorBrand
import java.util.Date
import java.util.Locale

@Composable
fun CreateReciboScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: CreateReciboViewModel = viewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val isLoadingCNPJ by viewModel.isLoadingCNPJ.collectAsState()
    val cnpjError by viewModel.cnpjError.collectAsState()

    val nomeContratante by viewModel.nomeContratante.collectAsState()
    val cpf_cnpj by viewModel.cpf_cnpj.collectAsState()
    val cpf_cnpj_mode by viewModel.cpf_cnpj_mode.collectAsState()
    val cep_contratante by viewModel.cep_contratante.collectAsState()
    val buscandoCepContratante by viewModel.buscandoCepContratante.collectAsState()
    val cepContratanteError by viewModel.cepContratanteError.collectAsState()
    val endereco_contratante by viewModel.endereco_contratante.collectAsState()
    val uf_contratante by viewModel.uf_contratante.collectAsState()
    val cidade_contratante by viewModel.cidade_contratante.collectAsState()
    val numero_contratante by viewModel.numero_contratante.collectAsState()
    val bairro_contratante by viewModel.bairro_contratante.collectAsState()
    val tel by viewModel.tel.collectAsState()
    val email by viewModel.email.collectAsState()

    val nome_falecido by viewModel.nome_falecido.collectAsState()
    val idade_falecido by viewModel.idade_falecido.collectAsState()
    val sexo_falecido by viewModel.sexo_falecido.collectAsState()
    val naturalidade_falecido by viewModel.naturalidade_falecido.collectAsState()
    val uf_falecido by viewModel.uf_falecido.collectAsState()
    val rg_falecido by viewModel.rg_falecido.collectAsState()
    val cpf_falecido by viewModel.cpf_falecido.collectAsState()
    val pai_falecido by viewModel.pai_falecido.collectAsState()
    val mae_falecido by viewModel.mae_falecido.collectAsState()

    val local_funeral by viewModel.local_funeral.collectAsState()
    val tipo_local_saida by viewModel.tipo_local_saida.collectAsState()
    val uf_local_saida by viewModel.uf_local_saida.collectAsState()
    val cidade_local_saida by viewModel.cidade_local_saida.collectAsState()
    val hora_saida_funeral by viewModel.hora_saida_funeral.collectAsState()
    val dia_funeral by viewModel.dia_funeral.collectAsState()
    val cemiterio_sepultamento by viewModel.cemiterio_sepultamento.collectAsState()
    val tipo_sepultamento by viewModel.tipo_sepultamento.collectAsState()
    val uf_sepultamento by viewModel.uf_sepultamento.collectAsState()
    val cidade_sepultamento by viewModel.cidade_sepultamento.collectAsState()

    val descricao by viewModel.descricao.collectAsState()

    val valor by viewModel.valor.collectAsState()
    val total_pago by viewModel.total_pago.collectAsState()
    val data_pagamento by viewModel.data_pagamento.collectAsState()

    var showPreview by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfGenerator = remember { PdfDocumentGenerator(context) }
    val repository: ReciboRepository = koinInject()
    val empresaRepository: EmpresaRepository = koinInject()
    val companyProfileSync: CompanyProfileSync = koinInject()
    var showExitDialog by remember { mutableStateOf(false) }
    var showMissingSheet by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }

    fun handleBackPress() {
        if (currentStep > 1) {
            viewModel.previousStep()
        } else {
            showExitDialog = true
        }
    }

    BackHandler(enabled = true) {
        handleBackPress()
    }

    suspend fun saveRecibo() {
        isGeneratingPdf = true
        pdfMessage = null
        // Garante a logo da funerária antes de emitir o recibo: se a imagem local
        // não estiver disponível, força o download para emitir com ela.
        if (!logoDisponivel(context, viewModel.logoUri.value)) {
            runCatching { companyProfileSync.sync() }
            viewModel.refreshLogo(LogoStorage.load(context))
        }
        val numero = (repository.nextNumero()).toString().padStart(4, '0')
        val empresa = empresaRepository.get()
        val recibo = viewModel.criarRecibo(
            numero = numero,
            enderecoAgente = empresa?.endereco.orEmpty(),
            cnpjAgente = CNPJFormatter.format(empresa?.cnpj) ?: ""
        )
        val fileName = "recibo_${System.currentTimeMillis()}.pdf"
        val uri = pdfGenerator.generate(
            fileName = fileName,
            pageSize = PdfPageSize.A4(72),
            pages = listOf { ReciboPDFLayout(recibo) }
        )

        if (uri != null) {
            val saveOk = runCatching { repository.save(recibo) }.isSuccess

            if (saveOk) {
                isGeneratingPdf = false
                pdfMessage = null
                viewModel.markObsoleteOnEdit(repository)

                val pdfSizeKb = pdfSizeKb(context, uri, fileName)
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                val generatedAt = sdf.format(Date())

                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("pdfUri", uri.toString())
                    set("pdfName", uri.lastPathSegment ?: "recibo.pdf")
                    set("pdfSize", pdfSizeKb)
                    set("generatedAt", generatedAt)
                }

                navController.navigate(Routes.RECIBO_SUCESSO) {
                    launchSingleTop = true
                }
            } else {
                isGeneratingPdf = false
                pdfMessage = "PDF gerado, mas houve erro ao salvar. Tente novamente."
            }
        } else {
            isGeneratingPdf = false
            pdfMessage = "Erro ao gerar PDF"
        }
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            Column {
                TopBar(
                    title = "Criar recibo",
                    showBackButton = true,
                    onBackPress = { handleBackPress() }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (step in 1..4) {
                        StepIndicator(
                            step = step,
                            currentStep = currentStep,
                            onClick = { viewModel.goToStep(step) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (currentStep) {
                    1 -> PrimaryButton(
                        text = "Confirmar",
                        onClick = {
                            val blocking = ReciboEssentials.step1Blocking(
                                nomeContratante, cpf_cnpj, endereco_contratante
                            )
                            if (blocking.isEmpty()) {
                                viewModel.nextStep()
                            } else {
                                showMissingSheet = true
                                // No need to call nextStep when missing
                            }
                        },
                        isEnabled = true // Always enabled now
                    )
                    2 -> PrimaryButton(
                        text = "Confirmar",
                        onClick = {
                             val blocking = ReciboEssentials.step2Blocking(
                                nome_falecido, cpf_falecido, rg_falecido,
                                naturalidade_falecido, uf_falecido, pai_falecido, mae_falecido
                            )
                            if (blocking.isEmpty()) {
                                viewModel.nextStep()
                            } else {
                                showMissingSheet = true
                            }
                        },
                        isEnabled = true
                    )
                    3 -> PrimaryButton(
                        text = "Confirmar",
                        onClick = {
                            val blocking = ReciboEssentials.step3Blocking(
                                local_funeral, dia_funeral, cemiterio_sepultamento
                            )
                            if (blocking.isEmpty()) {
                                viewModel.nextStep()
                            } else {
                                showMissingSheet = true
                            }
                        },
                        isEnabled = true
                    )
                    4 -> PrimaryButton(
                        text = if (isGeneratingPdf) "Salvando..." else "Salvar recibo",
                        onClick = {
                            val blocking = ReciboEssentials.step4Blocking(
                                descricao, total_pago, data_pagamento
                            )
                            if (blocking.isEmpty()) {
                                showSaveConfirm = true
                            } else {
                                showMissingSheet = true
                            }
                        },
                        isEnabled = !isGeneratingPdf // Only disable when generating PDF
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (currentStep) {
                1 -> {
                    Step1Contratante(
                        nomeContratante = nomeContratante,
                        onNomeContratanteChange = viewModel::updateNomeContratante,
                        cpfCnpj = cpf_cnpj,
                        onCpfCnpjChange = viewModel::updateCpfCnpj,
                        cpfCnpjMode = cpf_cnpj_mode,
                        onCpfCnpjModeChange = viewModel::updateCpfCnpjMode,
                        cepContratante = cep_contratante,
                        onCepContratanteChange = viewModel::updateCepContratante,
                        buscandoCep = buscandoCepContratante,
                        cepError = cepContratanteError,
                        onSearchCep = viewModel::buscarCepContratante,
                        enderecoContratante = endereco_contratante,
                        onEnderecoContratanteChange = viewModel::updateEnderecoContratante,
                        ufContratante = uf_contratante,
                        onUfContratanteChange = viewModel::updateUfContratante,
                        cidadeContratante = cidade_contratante,
                        onCidadeContratanteChange = viewModel::updateCidadeContratante,
                        numeroContratante = numero_contratante,
                        onNumeroContratanteChange = viewModel::updateNumeroContratante,
                        bairroContratante = bairro_contratante,
                        onBairroContratanteChange = viewModel::updateBairroContratante,
                        tel = tel,
                        onTelChange = viewModel::updateTel,
                        email = email,
                        onEmailChange = viewModel::updateEmail,
                        onSearchCNPJ = viewModel::searchCNPJ,
                        isLoadingCNPJ = isLoadingCNPJ,
                        cnpjError = cnpjError
                    )
                }
                2 -> {
                    Step2Falecido(
                        nomeFalecido = nome_falecido,
                        onNomeFalecidoChange = viewModel::updateNomeFalecido,
                        idadeFalecido = idade_falecido,
                        onIdadeFalecidoChange = viewModel::updateIdadeFalecido,
                        sexoFalecido = sexo_falecido,
                        onSexoFalecidoChange = viewModel::updateSexoFalecido,
                        naturalidadeFalecido = naturalidade_falecido,
                        onNaturalidadeFalecidoChange = viewModel::updateNaturalidadeFalecido,
                        ufFalecido = uf_falecido,
                        onUfFalecidoChange = viewModel::updateUfFalecido,
                        rgFalecido = rg_falecido,
                        onRgFalecidoChange = viewModel::updateRgFalecido,
                        cpfFalecido = cpf_falecido,
                        onCpfFalecidoChange = viewModel::updateCpfFalecido,
                        paiFalecido = pai_falecido,
                        onPaiFalecidoChange = viewModel::updatePaiFalecido,
                        maeFalecido = mae_falecido,
                        onMaeFalecidoChange = viewModel::updateMaeFalecido
                    )
                }
                3 -> {
                    Step3Funeral(
                        tipoLocalSaida = tipo_local_saida,
                        onTipoLocalSaidaChange = viewModel::updateTipoLocalSaida,
                        ufLocalSaida = uf_local_saida,
                        onUfLocalSaidaChange = viewModel::updateUfLocalSaida,
                        cidadeLocalSaida = cidade_local_saida,
                        onCidadeLocalSaidaChange = viewModel::updateCidadeLocalSaida,
                        localFuneral = local_funeral,
                        onLocalFuneralChange = viewModel::updateLocalFuneral,
                        horaSaidaFuneral = hora_saida_funeral,
                        onHoraSaidaFuneralChange = viewModel::updateHoraSaidaFuneral,
                        diaFuneral = dia_funeral,
                        onDiaFuneralChange = viewModel::updateDiaFuneral,
                        tipoSepultamento = tipo_sepultamento,
                        onTipoSepultamentoChange = viewModel::updateTipoSepultamento,
                        ufSepultamento = uf_sepultamento,
                        onUfSepultamentoChange = viewModel::updateUfSepultamento,
                        cidadeSepultamento = cidade_sepultamento,
                        onCidadeSepultamentoChange = viewModel::updateCidadeSepultamento,
                        cemiterioSepultamento = cemiterio_sepultamento,
                        onCemiterioSepultamentoChange = viewModel::updateCemiterioSepultamento
                    )
                }
                4 -> {
                    Step4ServicoPagamento(
                        descricao = descricao,
                        onDescricaoChange = viewModel::updateDescricao,
                        onAdicionarTranslado = viewModel::adicionarTranslado,
                        valorTotal = valor,
                        onValorTotalChange = viewModel::updateValor,
                        totalPago = total_pago,
                        onTotalPagoChange = viewModel::updateTotalPago,
                        dataPagamento = data_pagamento,
                        onDataPagamentoChange = viewModel::updateDataPagamento,
                        pdfMessage = pdfMessage
                    )
                }
            }
        }
    }
    if (showExitDialog) {
        ReceiptExitBottomSheet(
            onDismiss = { showExitDialog = false },
            onContinueLater = {
                showExitDialog = false
                navController.popBackStack()
            },
            onDeleteDraft = {
                viewModel.deleteDraft()
                showExitDialog = false
                navController.navigate(Routes.MEUS_RECIBOS) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
    if (showMissingSheet) {
        MissingFieldsBottomSheet(
            blocking = when (currentStep) {
                1 -> ReciboEssentials.step1Blocking(
                    nomeContratante, cpf_cnpj, endereco_contratante
                )
                2 -> ReciboEssentials.step2Blocking(
                    nome_falecido, cpf_falecido, rg_falecido,
                    naturalidade_falecido, uf_falecido, pai_falecido, mae_falecido
                )
                3 -> ReciboEssentials.step3Blocking(
                    local_funeral, dia_funeral, cemiterio_sepultamento
                )
                4 -> ReciboEssentials.step4Blocking(
                    descricao, total_pago, data_pagamento
                )
                else -> emptyList()
            },
            recommended = when (currentStep) {
                1 -> ReciboEssentials.step1Recommended(tel, email)
                2 -> ReciboEssentials.step2Recommended(idade_falecido, sexo_falecido)
                3 -> ReciboEssentials.step3Recommended(hora_saida_funeral)
                4 -> ReciboEssentials.step4Recommended() // Empty for now
                else -> emptyList()
            },
            onDismiss = { showMissingSheet = false }
        )
    }
    if (showSaveConfirm) {
        SaveConfirmationBottomSheet(
            title = "Salvar recibo?",
            message = "Confirme para gerar o PDF e salvar o recibo.",
            confirmLabel = "Salvar recibo",
            onDismiss = { showSaveConfirm = false },
            onConfirm = {
                showSaveConfirm = false
                scope.launch { saveRecibo() }
            }
        )
    }
}

@Composable
fun StepIndicator(
    step: Int,
    currentStep: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Surface(
            color = if (step <= currentStep) {
                colorBrand
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Text(
                text = step.toString(),
                modifier = Modifier.padding(8.dp),
                color = if (step <= currentStep) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (step) {
                1 -> "Contratante"
                2 -> "Falecido"
                3 -> "Funeral"
                4 -> "Serviço"
                else -> ""
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}
