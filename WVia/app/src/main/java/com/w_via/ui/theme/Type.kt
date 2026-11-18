package com.w_via.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Display — pode limitar mais, são números grandes em tela
private const val MIN_SCALE_LARGE = 0.85f
private const val MAX_SCALE_LARGE = 1.10f

// Body/Title — respeita mais a preferência do usuário
private const val MIN_SCALE_MEDIUM = 0.85f
private const val MAX_SCALE_MEDIUM = 1.30f

// Labels/Small — limita um pouco para não quebrar botões e chips
private const val MIN_SCALE_SMALL = 0.85f
private const val MAX_SCALE_SMALL = 1.15f

@Composable
fun getScaledFontSize(baseSize: Float, minScale: Float, maxScale: Float): TextUnit {
    val fontScale = LocalDensity.current.fontScale
    return (baseSize * fontScale.coerceIn(minScale, maxScale)).sp
}

@Composable
fun typography(): Typography {
    return Typography(
        // Display — usados em telas hero/splash, nunca em UI comum
        displayLarge = TextStyle(
            fontWeight = FontWeight.Black,
            fontSize = getScaledFontSize(57f, MIN_SCALE_LARGE, MAX_SCALE_LARGE),
            lineHeight = getScaledFontSize(64f, MIN_SCALE_LARGE, MAX_SCALE_LARGE),
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = getScaledFontSize(45f, MIN_SCALE_MEDIUM, MAX_SCALE_MEDIUM),
            lineHeight = getScaledFontSize(52f, MIN_SCALE_MEDIUM, MAX_SCALE_MEDIUM),
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = getScaledFontSize(36f, MIN_SCALE_SMALL, MAX_SCALE_SMALL),
            lineHeight = getScaledFontSize(44f, MIN_SCALE_SMALL, MAX_SCALE_SMALL),
            letterSpacing = 0.sp
        ),

        // Title — cabeçalhos de tela, seções, bottom sheets
        titleLarge = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = getScaledFontSize(22f, MIN_SCALE_LARGE, MAX_SCALE_LARGE),
            lineHeight = getScaledFontSize(26f, MIN_SCALE_LARGE, MAX_SCALE_LARGE),
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = getScaledFontSize(16f, MIN_SCALE_MEDIUM, MAX_SCALE_MEDIUM),
            lineHeight = getScaledFontSize(19f, MIN_SCALE_MEDIUM, MAX_SCALE_MEDIUM),
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = getScaledFontSize(14f, MIN_SCALE_SMALL, MAX_SCALE_SMALL),
            lineHeight = getScaledFontSize(17f, MIN_SCALE_SMALL, MAX_SCALE_SMALL),
            letterSpacing = 0.1.sp
        ),

        // Body — texto de conteúdo, descrições, campos
        bodyLarge = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = getScaledFontSize(16f, MIN_SCALE_LARGE, MAX_SCALE_LARGE),
            lineHeight = getScaledFontSize(19f, MIN_SCALE_LARGE, MAX_SCALE_LARGE),
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = getScaledFontSize(14f, MIN_SCALE_MEDIUM, MAX_SCALE_MEDIUM),
            lineHeight = getScaledFontSize(17f, MIN_SCALE_MEDIUM, MAX_SCALE_MEDIUM),
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = getScaledFontSize(12f, MIN_SCALE_SMALL, MAX_SCALE_SMALL),
            lineHeight = getScaledFontSize(14f, MIN_SCALE_SMALL, MAX_SCALE_SMALL),
            letterSpacing = 0.4.sp
        ),

        // Label — botões, chips, tabs, campos pequenos
        labelLarge = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = getScaledFontSize(14f, MIN_SCALE_LARGE, MAX_SCALE_LARGE),
            lineHeight = getScaledFontSize(17f, MIN_SCALE_LARGE, MAX_SCALE_LARGE),
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = getScaledFontSize(12f, MIN_SCALE_MEDIUM, MAX_SCALE_MEDIUM),
            lineHeight = getScaledFontSize(14f, MIN_SCALE_MEDIUM, MAX_SCALE_MEDIUM),
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = getScaledFontSize(11f, MIN_SCALE_SMALL, MAX_SCALE_SMALL),
            lineHeight = getScaledFontSize(13f, MIN_SCALE_SMALL, MAX_SCALE_SMALL),
            letterSpacing = 0.5.sp
        )
    )
}