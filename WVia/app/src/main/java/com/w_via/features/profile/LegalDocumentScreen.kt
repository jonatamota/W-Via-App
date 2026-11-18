package com.w_via.features.profile

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.w_via.features.appnavigation.TopBar

/**
 * Tela genérica para exibir documentos textuais disponíveis em `assets/`.
 * Formata títulos envolvidos por `#...#` e trechos em negrito com `**...**`,
 * no mesmo padrão já adotado por [PrivacyPolicyScreen].
 */
@Composable
fun LegalDocumentScreen(
    title: String,
    assetFileName: String,
    navController: NavController,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val conteudo = remember(assetFileName) {
        runCatching {
            context.assets.open(assetFileName).bufferedReader().use { it.readText() }
        }.getOrElse { "Erro ao carregar o documento solicitado." }
    }

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val backgroundColor = colorScheme.background
            window.statusBarColor = backgroundColor.toArgb()
            window.navigationBarColor = backgroundColor.toArgb()
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            val isLight = backgroundColor.luminance() > 0.5
            controller.isAppearanceLightStatusBars = isLight
            controller.isAppearanceLightNavigationBars = isLight
        }
        onDispose { }
    }

    Surface(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = title,
                showBackButton = true,
                onBackPress = { navController.popBackStack() }
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                item { Spacer(modifier = Modifier.padding(8.dp)) }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = formatLegalText(conteudo),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp
                        )
                    }
                }
                item { Spacer(modifier = Modifier.padding(16.dp)) }
            }
        }
    }
}

@Composable
private fun formatLegalText(texto: String): AnnotatedString {
    val outlineColor = MaterialTheme.colorScheme.outline
    return formatLegalText(texto, outlineColor)
}

private fun formatLegalText(texto: String, outlineColor: androidx.compose.ui.graphics.Color): AnnotatedString =
    buildAnnotatedString {
    val titlePattern = "#(.*?)#"
    val boldPattern = "\\*\\*(.*?)\\*\\*"
    val combined = Regex("($titlePattern)|($boldPattern)")
    var last = 0
    for (match in combined.findAll(texto)) {
        append(texto.substring(last, match.range.first))
        val v = match.value
        when {
            v.startsWith("#") && v.endsWith("#") -> {
                val t = v.substring(1, v.length - 1)
                append("\n")
                pushStyle(
                    SpanStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = outlineColor
                    )
                )
                append(t)
                pop()
                append("\n")
            }

            v.startsWith("**") && v.endsWith("**") -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(v.substring(2, v.length - 2))
                pop()
            }
        }
        last = match.range.last + 1
    }
    if (last < texto.length) append(texto.substring(last))
}
