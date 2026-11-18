package com.w_via.features.receipt

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
import com.w_via.features.datamycnpj.LogoStorage
import com.w_via.features.datamycnpj.logoDisponivel
import com.w_via.features.receipt.composeToPdf.PdfDocumentGenerator
import com.w_via.features.receipt.composeToPdf.PdfPageSize
import com.w_via.features.receipt.composeToPdf.pdfSizeKb
import com.w_via.features.receipt.data.ReciboRepository
import com.w_via.features.receipt.layouts.ReciboPDFLayout
import com.w_via.features.routes.Routes
import com.w_via.features.sync.CompanyProfileSync
import com.w_via.features.utils.valorPorExtenso
import com.w_via.ui.components.DetailSection
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detalhes de um recibo salvo. Carrega o modelo do Room e, no fim, gera o PDF
 * seguindo o fluxo da tela de sucesso.
 */
@Composable
fun ReciboDetalheScreen(
    navController: NavController? = null,
    reciboId: Long
) {
    val repository: ReciboRepository = koinInject()
    val companyProfileSync: CompanyProfileSync = koinInject()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfGenerator = remember { PdfDocumentGenerator(context) }

    var isGenerating by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var isDeleting by rememberSaveable { mutableStateOf(false) }

    val recibo by produceState<Recibo?>(initialValue = null, key1 = reciboId) {
        value = repository.getById(reciboId)
    }
    val data = recibo

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            TopBar(
                title = "Detalhes do recibo",
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
                        text = "Recibo não encontrado",
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
                    item { DetailSection("Contratante", contratanteItens(data)) }
                    item { DetailSection("Falecido", falecidoItens(data)) }
                    item { DetailSection("Funeral", funeralItens(data)) }
                    item { DetailSection("Serviço e pagamento", servicoItens(data)) }
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
                                // Garante a logo da funerária antes de emitir o recibo.
                                var logo = data.logoUri
                                if (!logoDisponivel(context, logo)) {
                                    runCatching { companyProfileSync.sync() }
                                    logo = LogoStorage.load(context) ?: data.logoUri
                                }
                                val reciboParaPdf = data.copy(logoUri = logo)
                                val fileName = "recibo_${reciboId}.pdf"
                                val uri = pdfGenerator.generate(
                                    fileName = fileName,
                                    pageSize = PdfPageSize.A4(72),
                                    pages = listOf { ReciboPDFLayout(recibo = reciboParaPdf) }
                                )
                                if (uri != null) {
                                    val pdfSizeKb = pdfSizeKb(context, uri, fileName)
                                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                                    navController?.currentBackStackEntry?.savedStateHandle?.apply {
                                        set("pdfUri", uri.toString())
                                        set("pdfName", uri.lastPathSegment ?: "recibo.pdf")
                                        set("pdfSize", pdfSizeKb)
                                        set("generatedAt", sdf.format(Date()))
                                    }
                                    navController?.navigate(Routes.RECIBO_SUCESSO) {
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
                                ReciboDraftStorage.saveFromRecibo(context, data, editingReciboId = reciboId)
                                navController?.navigate(Routes.CRIAR_RECIBO)
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
                    repository.delete(reciboId)
                    isDeleting = false
                    navController?.popBackStack()
                }
            },
            title = "Excluir recibo?",
            message = "O recibo será excluído."
        )
    }
}

private fun contratanteItens(r: Recibo): List<Pair<String, String>> = buildList {
    add("Nome do contratante" to r.nomeContratante)
    if (r.rg_ie.isNotBlank()) add("RG/IE" to r.rg_ie)
    if (r.cpf_cnpj.isNotBlank()) add("CPF/CNPJ" to r.cpf_cnpj)
    add("Endereço" to r.endereco_contratante)
    if (r.tel.isNotBlank()) add("Telefone" to r.tel)
    add("E-mail" to r.email)
}.filterNot { it.second.isBlank() }

private fun falecidoItens(r: Recibo): List<Pair<String, String>> = buildList {
    add("Nome do falecido" to r.nome_falecido)
    add("Estado civil" to r.estado_civil_falecido)
    add("Idade" to r.idade_falecido)
    add("Sexo" to r.sexo_falecido)
    add("Naturalidade" to r.naturalidade_falecido)
    add("UF" to r.uf_falecido)
    if (r.rg_falecido.isNotBlank()) add("RG" to r.rg_falecido)
    if (r.cpf_falecido.isNotBlank()) add("CPF" to r.cpf_falecido)
    add("Filiação" to r.filiacao_falecido)
}.filterNot { it.second.isBlank() }

private fun funeralItens(r: Recibo): List<Pair<String, String>> = buildList {
    add("Local do funeral" to r.local_funeral)
    add("Hora de saída" to r.hora_saida_funeral)
    add("Dia do funeral" to r.dia_funeral)
    add("Cemitério" to r.cemiterio_sepultamento)
}.filterNot { it.second.isBlank() }

private fun servicoItens(r: Recibo): List<Pair<String, String>> = buildList {
    add("Identificador único" to r.uuid)
    if (r.quantidade.isNotBlank()) add("Quantidade" to r.quantidade)
    add("Descrição" to r.descricao)
    if (r.unidade.isNotBlank()) add("Unidade" to r.unidade)
    r.valor.toDoubleOrNull()?.let { add("Valor" to "R$ %.2f".format(it)) }
    r.total_pago.toDoubleOrNull()?.let { add("Total pago" to "R$ %.2f".format(it)) }
    add("Valor por extenso" to r.valor_por_extenso.ifBlank {
        r.total_pago.toDoubleOrNull()?.let { valorPorExtenso(it) } ?: ""
    })
    add("Data de pagamento" to r.data_pagamento)
}.filterNot { it.second.isBlank() }
