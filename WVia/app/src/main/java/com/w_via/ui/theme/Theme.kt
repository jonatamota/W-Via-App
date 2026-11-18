package com.w_via.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Definindo as cores para o tema claro
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    background = BackgroundColor, //EFF3EFFF
    surface = SurfaceColor,
    surfaceVariant = SurfaceVariantColor,
    onPrimary = OnPrimaryColor,
    onSecondary = OnSecondaryColor,
    onTertiary = OnTertiaryColor,
    onBackground = OnBackgroundColorLight,
    onSurface = OnSurfaceColorLight,
    // Slots não definidos caíam nos padrões roxos/lilás do Material3; aqui ficam
    // neutros e alinhados à paleta do app (preto/brand/dourado, verdes e azuis).
    primaryContainer = Color(0xFFE9E9E9),
    onPrimaryContainer = Color(0xFF1C1C1C),
    secondaryContainer = Color(0xFFE3F1E4),
    onSecondaryContainer = Color(0xFF0B3B0E),
    tertiaryContainer = Color(0xFFE5EAFB),
    onTertiaryContainer = Color(0xFF0F1A5A),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFC9C9C9),
    onSurfaceVariant = Color(0xFF4D4D4D),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F6F4),
    surfaceContainer = Color(0xFFEFF3EF),
    surfaceContainerHigh = Color(0xFFE8ECE8),
    surfaceContainerHighest = Color(0xFFE0E5E0),
    surfaceDim = Color(0xFFDEE3DE),
    surfaceBright = Color(0xFFF7F8F6),
    surfaceTint = PrimaryColor,
    inverseSurface = Color(0xFF2C2C2C),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = colorBrand,
    scrim = Color(0xFF000000)
)

// Definindo as cores para o tema escuro
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColorDark,
    secondary = SecondaryColorDark,
    tertiary = TertiaryColorDark,
    background = BackgroundColorDark,
    surface = SurfaceColorDark,
    surfaceVariant = SurfaceVariantColorDark,
    onPrimary = OnPrimaryColorDark,
    onSecondary = OnSecondaryColorDark,
    onTertiary = OnTertiaryColorDark,
    onBackground = OnBackgroundColorDark,
    onSurface = OnSurfaceColorDark,
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color(0xFFF5F5F5),
    secondaryContainer = Color(0xFF1F3A22),
    onSecondaryContainer = Color(0xFFE3F1E4),
    tertiaryContainer = Color(0xFF232B4D),
    onTertiaryContainer = Color(0xFFE5EAFB),
    errorContainer = Color(0xFF4A1411),
    onErrorContainer = Color(0xFFF9DEDC),
    outline = Color(0xFF8F8F8F),
    outlineVariant = Color(0xFF414141),
    onSurfaceVariant = Color(0xFFC6C6C6),
    surfaceContainerLowest = Color(0xFF131616),
    surfaceContainerLow = Color(0xFF1A1E1D),
    surfaceContainer = Color(0xFF202423),
    surfaceContainerHigh = Color(0xFF262A29),
    surfaceContainerHighest = Color(0xFF2C3130),
    surfaceDim = Color(0xFF171B1A),
    surfaceBright = Color(0xFF2C3130),
    surfaceTint = PrimaryColorDark,
    inverseSurface = Color(0xFFE8E8E8),
    inverseOnSurface = Color(0xFF1C1C1C),
    inversePrimary = colorBrand,
    scrim = Color(0xFF000000)
)

@Composable
fun WViaTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    // val currentTheme by context.readTheme().collectAsState(initial = "sistema") // Comentado - readTheme não encontrado
    val view = LocalView.current
    val isDarkTheme = isSystemInDarkTheme() // Usando tema do sistema por padrão

    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    val typography = typography()

    if (!view.isInEditMode) {
        val activity = context as? Activity
        activity?.updateSystemBars(colorScheme)
    }

    val cornerRadios = 1.dp

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = 1f.coerceIn(0.8f, 1.4f)
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
            typography = typography()
        )
    }
}


// Função para atualizar as cores da barra de status e navegação
@Suppress("DEPRECATION")
fun Activity.updateSystemBars(colorScheme: ColorScheme) {
    window.statusBarColor = colorScheme.background.toArgb()
    window.navigationBarColor = colorScheme.background.toArgb()

    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = colorScheme.background.luminance() > 0.5
        isAppearanceLightNavigationBars = colorScheme.background.luminance() > 0.5
    }
}

