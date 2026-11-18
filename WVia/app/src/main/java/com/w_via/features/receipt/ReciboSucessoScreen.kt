package com.w_via.features.receipt

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.features.routes.Routes
import com.w_via.ui.theme.colorBrand
import org.koin.androidx.compose.koinViewModel

/**
 * Tela genérica de resultado de PDF.
 *
 * Uso 1 - fluxo de criação: defina pdfUri/pdfName/pdfSize/generatedAt no
 *   savedStateHandle da entrada anterior e navegue com reciboId = null.
 * Uso 2 - recibo existente: passe reciboId, a tela busca os dados e regenera o PDF.
 */
@Composable
fun ReciboSucessoScreen(
    navController: NavHostController? = null,
    reciboId: String? = null,
    documentType: String = "Recibo",
    backRoute: String = Routes.MEUS_RECIBOS,
    shareTitle: String = "Compartilhar recibo",
    viewModel: ReciboSucessoViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var isAnimating by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        navController?.navigate(backRoute) {
            popUpTo(0) { inclusive = true }
        }
    }

    LaunchedEffect(reciboId) {
        isAnimating = true
        if (reciboId != null) {
            viewModel.loadExistingRecibo(context, reciboId)
        } else {
            val savedStateHandle = navController?.previousBackStackEntry?.savedStateHandle
            val pdfUriString = savedStateHandle?.get<String>("pdfUri")
            if (pdfUriString != null) {
                viewModel.setGeneratedPdf(
                    uri = Uri.parse(pdfUriString),
                    name = savedStateHandle.get<String>("pdfName") ?: "",
                    size = savedStateHandle.get<String>("pdfSize") ?: "",
                    generatedAt = savedStateHandle.get<String>("generatedAt") ?: ""
                )
            } else {
                viewModel.setError("Nenhum PDF para exibir.")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = {
                navController?.navigate(backRoute) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Fechar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (val state = uiState) {
            is ReciboSucessoViewModel.UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ReciboSucessoViewModel.UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is ReciboSucessoViewModel.UiState.Ready -> {
                ReciboSucessoContent(
                    pdfUri = state.pdfUri,
                    pdfName = state.pdfName,
                    pdfSize = state.pdfSize,
                    generatedAt = state.generatedAt,
                    documentType = documentType,
                    shareTitle = shareTitle,
                    isAnimating = isAnimating,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun ReciboSucessoContent(
    pdfUri: Uri,
    pdfName: String,
    pdfSize: String,
    generatedAt: String,
    documentType: String,
    shareTitle: String,
    isAnimating: Boolean,
    context: android.content.Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            targetState = isAnimating,
            transitionSpec = {
                scaleIn(initialScale = 0.5f) + fadeIn() togetherWith
                        scaleOut(targetScale = 0.5f) + fadeOut()
            },
            label = "success_icon"
        ) { animated ->
            if (animated) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "PDF gerado",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Documento pronto",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "O arquivo está pronto para ser visualizado ou compartilhado",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Informações",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colorBrand.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = documentType,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = colorBrand,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(icon = Icons.Default.Description, label = "Nome do arquivo", value = pdfName)
                if (pdfSize.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    InfoRow(icon = Icons.Default.Storage, label = "Tamanho", value = pdfSize)
                }
                if (generatedAt.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    InfoRow(icon = Icons.Default.CalendarMonth, label = "Gerado em", value = generatedAt)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, pdfUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, shareTitle))
                },
                text = "Compartilhar"
            )

            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(pdfUri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(openIntent)
                    } catch (e: android.content.ActivityNotFoundException) {
                        // sem app compatível instalado
                    }
                },
                text = "Abrir PDF"
            )
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
        Text(text = value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Normal, textAlign = TextAlign.End)
    }
}