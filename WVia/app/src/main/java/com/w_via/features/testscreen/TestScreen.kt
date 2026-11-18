package com.w_via.features.testscreen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.w_via.BuildConfig
import com.w_via.features.receipt.composeToPdf.PdfDocumentGenerator
import com.w_via.features.testscreen.layouts.ReceituarioScreen
import kotlinx.coroutines.launch

@Composable
fun TestScreen(
    navController: NavHostController,
    bottomBarPadding: Dp = 0.dp,
) {
    if (!BuildConfig.DEBUG) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var gerando by remember { mutableStateOf(false) }
    var mensagem by remember { mutableStateOf("Gera o PDF do testLayout (receituário) e abre direto.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottomBarPadding)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tela de Testes",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !gerando,
            onClick = {
                gerando = true
                mensagem = "Gerando PDF..."
                scope.launch {
                    val uri = PdfDocumentGenerator(context).generate(
                        fileName = "teste_layout.pdf",
                        pageSize = null,
                        pages = listOf { ReceituarioScreen() }
                    )
                    gerando = false
                    if (uri == null) {
                        mensagem = "Falha ao gerar o PDF"
                    } else {
                        mensagem = "PDF gerado: teste_layout.pdf"
                        abrirPdf(context, uri)
                    }
                }
            }
        ) {
            Text(if (gerando) "Gerando..." else "Gerar PDF")
        }
    }
}

private fun abrirPdf(context: Context, uri: Uri) {
    val openIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(openIntent)
    } catch (_: ActivityNotFoundException) {
    }
}
