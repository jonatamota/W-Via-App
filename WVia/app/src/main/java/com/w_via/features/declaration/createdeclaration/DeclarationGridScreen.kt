package com.w_via.features.declaration.createdeclaration

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.designsystem.bottomsheets.SaveConfirmationBottomSheet
import com.w_via.features.appnavigation.TopBar
import com.w_via.features.company.data.EmpresaRepository
import com.w_via.features.declaration.data.DeclaracaoRepository
import com.w_via.features.declaration.layouts.DeclaracaoObitoPDFLayout
import com.w_via.features.declaration.layouts.cidadeEstadoDe
import com.w_via.features.declaration.layouts.dataAtualPorExtenso
import com.w_via.features.declaration.layouts.numeroExibido
import com.w_via.features.components.MissingFieldsBottomSheet
import com.w_via.features.receipt.composeToPdf.PdfDocumentGenerator
import com.w_via.features.receipt.composeToPdf.PdfPageSize
import com.w_via.features.receipt.composeToPdf.pdfSizeKb
import com.w_via.ui.theme.colorBrand
import com.w_via.features.routes.Routes
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeclarationGridScreen(
    navController: NavController? = null,
    bottomBarPadding: Dp = 0.dp,
    viewModel: DeclarationDraftViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val confirmedSections by viewModel.confirmedSections.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val pdfResult by viewModel.pdfResult.collectAsState()

    LaunchedEffect(Unit) {
        val seed = DeclarationDraftViewModel.editSeed
        if (seed != null) {
            viewModel.loadFromSaved(seed)
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfGenerator = remember { PdfDocumentGenerator(context) }
    val repository: DeclaracaoRepository = koinInject()
    val empresaRepository: EmpresaRepository = koinInject()

    var showDiscardSheet by remember { mutableStateOf(false) }
    var showMissingSheet by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }

    suspend fun salvarDeclaracao() {
        viewModel.setGenerating(true)
        val declaracaoBase = viewModel.toDeclaracaoObito()
        // Cabeçalho/rodapé com os dados profissionais da funerária (cache local).
        val empresa = runCatching { empresaRepository.get() }.getOrNull()
        val declaracao = declaracaoBase.copy(
            cidadeEstado = cidadeEstadoDe(empresa),
            dataEmissaoPorExtenso = dataAtualPorExtenso(),
            enderecoFuneraria = empresa?.endereco.orEmpty(),
            telefoneFuneraria = buildString {
                val tel = empresa?.telefone.orEmpty().trim()
                if (tel.isNotBlank()) {
                    append(tel)
                    append(" - ")
                }
                append("Atendimento 24h")
            }
        )
        // Salvar é best-effort (banco local + sincronização posterior). A falha
        // de persistência/sincronização não impede a geração e entrega do PDF.
        val savedId = runCatching { repository.save(declaracao) }.getOrNull()
        val fileName = "declaracao_obito_${System.currentTimeMillis()}.pdf"
        val uri = runCatching {
            pdfGenerator.generate(
                fileName = fileName,
                pageSize = PdfPageSize.A4(72),
                pages = listOf {
                    DeclaracaoObitoPDFLayout(declaracao = declaracao, numero = numeroExibido(savedId ?: 0L))
                }
            )
        }.getOrNull()
        viewModel.setGenerating(false)
        if (uri != null) {
            val pdfSizeKb = pdfSizeKb(context, uri, fileName)
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            val generatedAt = sdf.format(Date())
            navController?.currentBackStackEntry?.savedStateHandle?.apply {
                set("pdfUri", uri.toString())
                set("pdfName", uri.lastPathSegment ?: "declaracao_obito.pdf")
                set("pdfSize", pdfSizeKb)
                set("generatedAt", generatedAt)
            }
            viewModel.reset()
            navController?.navigate(Routes.DECLARACAO_SUCESSO) {
                launchSingleTop = true
            }
        } else {
            viewModel.setPdfResult(
                DeclarationDraftViewModel.PdfResult(
                    isSuccess = false,
                    message = "Não foi possível gerar o PDF. Tente novamente."
                )
            )
        }
    }
    val handleBack = {
        if (viewModel.hasContent()) {
            showDiscardSheet = true
        } else {
            navController?.popBackStack()
        }
    }

    BackHandler(enabled = true) { handleBack() }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            TopBar(
                title = "Campos da declaração",
                showBackButton = true,
                onBackPress = { handleBack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 30.dp, top = 6.dp, end = 30.dp, bottom = 30.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        declarationSectionDescriptors,
                        key = { it.section }
                    ) { descriptor ->
                        ActionGridButton(
                            text = descriptor.section.title,
                            iconRes = descriptor.iconRes,
                            isConfirmed = descriptor.section in confirmedSections &&
                                descriptor.section.isComplete(state),
                            isOptional = !descriptor.section.isRequiredFor(state),
                            onClick = {
                                navController?.navigate(descriptor.route)
                            }
                        )
                    }
                }

                // Sempre mostrar o botão de salvar; só prossegue quando as seções
                // obrigatórias estiverem confirmadas e completas.
                PrimaryButton(
                    text = "Salvar declaração",
                    onClick = {
                        if (viewModel.canSave()) {
                            showSaveConfirm = true
                        } else {
                            showMissingSheet = true
                        }
                    },
                    height = 52.dp,
                    isLoading = isGenerating,
                    isEnabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp)
                )
            }

            if (showMissingSheet) {
                MissingFieldsBottomSheet(
                    // Campos bloqueantes faltantes nas seções obrigatórias.
                    blocking = DeclarationSection.entries
                        .filter { it.isRequiredFor(state) }
                        .flatMap { it.essentialsMissing(state) },
                    // Campos recomendados faltantes nas seções obrigatórias.
                    recommended = DeclarationSection.entries
                        .filter { it.isRequiredFor(state) }
                        .flatMap { it.recommendedMissing(state) },
                    onDismiss = { showMissingSheet = false }
                )
            }
        }
    }

    if (showDiscardSheet) {
        DeclarationDiscardBottomSheet(
            onDismiss = { showDiscardSheet = false },
            onDiscard = {
                showDiscardSheet = false
                viewModel.reset()
                navController?.popBackStack()
            }
        )
    }

    if (showSaveConfirm) {
        SaveConfirmationBottomSheet(
            title = "Salvar declaração?",
            message = "Confirme para gerar o PDF e salvar a declaração.",
            confirmLabel = "Salvar declaração",
            onDismiss = { showSaveConfirm = false },
            onConfirm = {
                showSaveConfirm = false
                scope.launch { salvarDeclaracao() }
            }
        )
    }

    pdfResult?.let { result ->
        DeclarationResultBottomSheet(
            title = if (result.isSuccess) "Sucesso" else "Erro",
            message = result.message,
            onDismiss = { viewModel.setPdfResult(null) },
            onConfirm = {
                viewModel.setPdfResult(null)
                if (result.isSuccess) {
                    viewModel.reset()
                    navController?.popBackStack()
                }
            }
        )
    }
}

@Composable
fun ActionGridButton(
    text: String,
    iconRes: Int,
    isConfirmed: Boolean,
    isOptional: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isConfirmed) colorBrand else MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(onClick = onClick)
            .semantics {
                stateDescription = when {
                    isConfirmed -> "Confirmada"
                    isOptional -> "Opcional, não confirmada"
                    else -> "Não confirmada"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
                if (isConfirmed) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(0.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Confirmado",
                            tint = colorBrand,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else if (isOptional) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(colorBrand, RoundedCornerShape(0.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Opcional",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                textAlign = TextAlign.Center,
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeclarationGridScreenPreview() {
    DeclarationGridScreen()
}
