package com.w_via.features.initscreens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import com.w_via.features.appnavigation.TopBar

@Composable
fun PrivacyPolicyScreen(
    navController: NavController,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val conteudoArquivo = remember {
        try {
            context.assets.open("politicaPrivacidadeOffiline.txt").bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            "Erro ao carregar o arquivo de política de privacidade."
        }
    }

    // Função para formatar o texto com títulos e negrito
    @Composable
    fun formatarTexto(texto: String): AnnotatedString {
        return buildAnnotatedString {
            // Definir padrões para títulos (#texto#) e negrito (**texto**)
            val titlePattern = "#(.*?)#"
            val boldPattern = "\\*\\*(.*?)\\*\\*"

            // Combinar os dois padrões em uma expressão regular
            val combinedPattern = Regex("($titlePattern)|($boldPattern)")

            var lastMatchEnd = 0

            // Encontrar todas as correspondências no texto
            val matches = combinedPattern.findAll(texto)

            for (match in matches) {
                // Adicionar texto normal antes da formatação
                append(texto.substring(lastMatchEnd, match.range.first))

                val matchValue = match.value

                // Verificar se é um padrão de título
                if (matchValue.startsWith("#") && matchValue.endsWith("#")) {
                    // Extrair o texto entre as hashtags
                    val titleText = matchValue.substring(1, matchValue.length - 1)

                    // Adicionar espaçamento antes do título
                    append("\n")

                    // Aplicar estilo de título
                    pushStyle(
                        SpanStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                    append(titleText)
                    pop()

                    // Adicionar espaçamento após o título
                    append("\n")
                }
                // Verificar se é um padrão de negrito
                else if (matchValue.startsWith("**") && matchValue.endsWith("**")) {
                    // Extrair o texto entre os asteriscos
                    val boldText = matchValue.substring(2, matchValue.length - 2)

                    // Aplicar estilo de negrito
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldText)
                    pop()
                }

                // Atualizar o índice para o próximo trecho de texto
                lastMatchEnd = match.range.last + 1
            }

            // Adicionar qualquer texto restante após a última formatação
            if (lastMatchEnd < texto.length) {
                append(texto.substring(lastMatchEnd))
            }
        }
    }

    DisposableEffect(Unit) {
        val window = (context as Activity).window
        val backgroundColor = colorScheme.background

        window.statusBarColor = backgroundColor.toArgb()
        window.navigationBarColor = backgroundColor.toArgb()

        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // Determina a cor dos ícones baseado na luminância do background
        val isLightBackground = backgroundColor.luminance() > 0.5
        controller.isAppearanceLightStatusBars = isLightBackground
        controller.isAppearanceLightNavigationBars = isLightBackground

        onDispose {
            // Sem Reset
        }
    }

    Surface(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    )
    {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(
                title = "Termos de privacidade",
                showBackButton = true,
                onBackPress = { navController.popBackStack() }
            )

            // Conteúdo principal
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = formatarTexto(conteudoArquivo),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}



