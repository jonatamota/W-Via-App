package com.w_via.features.authentication.loginAuth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.w_via.BuildConfig
import com.w_via.R
import com.w_via.designsystem.TextTerms
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.features.routes.Routes
import com.w_via.features.utils.ClickDebounce
import com.w_via.ui.theme.colorBrand


@Composable
fun LoginScreen(
    navController: NavController? = null,
    googleViewModel: LoginGoogleViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val googleLoginState by googleViewModel.loginState.collectAsState()
    val googleNavigationState by googleViewModel.navigationState.collectAsState()

    val context = LocalContext.current
    val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID


    LaunchedEffect(googleLoginState) {
        when (googleLoginState) {
            is LoginGoogleViewModel.GoogleLoginState.Error -> {
                val message =
                    (googleLoginState as LoginGoogleViewModel.GoogleLoginState.Error).message
                Log.e("LoginScreen", "Google login error: $message")
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }

            else -> {}
        }
    }

    // Navegação para login Google
    LaunchedEffect(googleNavigationState) {
        when (googleNavigationState) {
            is LoginGoogleViewModel.NavigationState.NavigateToFinalizeAccount -> {
                navController?.navigate(Routes.FINALIZAR_CONTA) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                googleViewModel.resetNavigationState()
            }

            is LoginGoogleViewModel.NavigationState.NavigateToHome -> {
                navController?.navigate(Routes.HOME) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                googleViewModel.resetNavigationState()
            }

            is LoginGoogleViewModel.NavigationState.NavigateToErrorAccount -> {
                navController?.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                googleViewModel.resetNavigationState()
            }

            is LoginGoogleViewModel.NavigationState.NavigateToUploadFile -> {
                navController?.navigate(Routes.UPLOAD_FILE) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                googleViewModel.resetNavigationState()
            }

            else -> {}
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

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        bottomBar = {
            TextTerms(
                text = "Ao entrar, você aceita os Termos e Condições de uso." +
                        " Para ler os os termos clique aqui, eles também poderão ser consultados dentro do app.",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 30.dp
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (ClickDebounce.canClick()) {
                            navController?.navigate(Routes.POLITICA_PRIVACIDADE)
                        }
                    }
            )
        },
        modifier = Modifier
            .safeDrawingPadding()
            .background(
                color = MaterialTheme.colorScheme.background
            )
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Divisão na tela: metade de cima colorBrand, metade de baixo tema
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(colorBrand)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Entre ou crie sua conta",
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                PrimaryButton(
                    onClick = { googleViewModel.signInWithGoogle(context, clientId) },
                    text = "Continuar com o Google",
                    isLoading = googleLoginState is LoginGoogleViewModel.GoogleLoginState.Loading,
                    isEnabled = googleLoginState !is LoginGoogleViewModel.GoogleLoginState.Loading,
                    containerColorEnable = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo_google_1),
                            contentDescription = "Login com o Google",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = { openWhatsApp(context) },
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_whatsapp),
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF25D366)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Está com dificuldade para entrar?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fale com o suporte",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// Abre o WhatsApp no número de suporte, preferindo o app e caindo para o
// navegador caso ele não esteja instalado.
private fun openWhatsApp(context: Context) {
    val uri = Uri.parse("https://wa.me/5511933391081")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage("com.whatsapp"))
    } catch (e: Exception) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (ignored: Exception) {
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}