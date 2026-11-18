package com.w_via.features.datamycnpj

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.w_via.designsystem.appbuttons.PrimaryButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun UploadFileScreen(
    navController: NavController,
    viewModel: UploadFileViewModel = koinViewModel(),
    onNavigateToCNPJ: (Uri?) -> Unit = {}
) {
    val cornerRadius = 2.dp
    val uiState by viewModel.uiState.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.handleFileSelection(it)
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Long
            )
            viewModel.clearSnackbar()
        }
    }

    // Navigate to CNPJ screen when file is saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateToCNPJ(uiState.savedUri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permissão concedida
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Spacer(modifier = Modifier.fillMaxHeight(0.3f))

                Text(
                    text = "Envie uma imagem com a logo ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Box de upload
                val borderColor = if (uiState.selectedFileUri != null)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4.2f / 2.4f)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val dashLength = 15.dp.toPx()
                            val gapLength = 10.dp.toPx()

                            drawRoundRect(
                                color = borderColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(
                                        intervals = floatArrayOf(dashLength, gapLength),
                                        phase = 0f
                                    )
                                ),
                                cornerRadius = CornerRadius(cornerRadius.toPx())
                            )
                        }
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    imageLauncher.launch("image/*")
                                } else {
                                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                }
                            } else {
                                if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    imageLauncher.launch("image/*")
                                } else {
                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.selectedFileUri != null)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                            ) {
                                val iconTint = when {
                                    uiState.isSaved -> MaterialTheme.colorScheme.primary
                                    uiState.selectedFileUri != null -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline
                                }

                                Icon(
                                    imageVector = when {
                                        uiState.isSaved -> Icons.Default.CheckCircle
                                        uiState.selectedFileUri != null -> Icons.Default.FilePresent
                                        else -> Icons.Default.CloudUpload
                                    },
                                    contentDescription = "Upload",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .align(Alignment.Center),
                                    tint = iconTint
                                )
                            }

                            uiState.fileName?.let { fileName ->
                                Text(
                                    text = fileName,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            uiState.fileSize?.let { size ->
                                Text(
                                    text = viewModel.formatFileSize(size),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }

                            if (uiState.selectedFileUri == null) {
                                Text(
                                    text = "Formatos suportados: JPG, PNG (máx. 1MB)",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Mensagem de erro
                            uiState.error?.let { error ->
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.clickable { viewModel.clearError() }
                                )
                            }
                        }
                    }

                    // Progresso horizontal dentro do box durante o envio ao Supabase
                    if (uiState.isSaving) {
                        LinearProgressIndicator(
                            progress = uploadProgress,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                        )
                    }
                }

                // Botão Limpar (canto inferior direito)
                if (!uiState.isSaving) {
                    Spacer(modifier = Modifier.width(12.dp))
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.clearSelection() }
                    ) {
                        Text(
                            text = "Limpar",
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            // Botões fixos no bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp)
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botão Proseguir sem imagem (aparece quando não há arquivo selecionado)
                if (uiState.selectedFileUri == null) {
                    PrimaryButton(
                        onClick = {
                            onNavigateToCNPJ(null)
                        },
                        contentColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        containerColorEnable = MaterialTheme.colorScheme.outline.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth(),
                        text = "Proseguir sem imagem",
                        showOutline = true
                    )
                }

                // Botão Salvar (aparece quando há arquivo selecionado)
                if (uiState.selectedFileUri != null && !uiState.isSaved) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrimaryButton(
                            onClick = {
                                uiState.selectedFileUri?.let {
                                    viewModel.salvarImagem(it)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            text = if (uiState.isSaving) "Enviando..." else "Salvar imagem",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Salvar"
                                )
                            },
                            isEnabled = !uiState.isSaving,
                            isLoading = uiState.isSaving,
                        )
                    }
                }
            }
        }
    }
}

// Preview
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewUploadFileScreen() {
    // Preview sem NavController - apenas visual
    UploadFileScreen(
        navController = rememberNavController(),
        onNavigateToCNPJ = {}
    )
}