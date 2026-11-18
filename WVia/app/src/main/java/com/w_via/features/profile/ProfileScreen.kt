package com.w_via.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.w_via.designsystem.bottomsheets.ConfirmacaoBottomSheet
import com.w_via.features.appnavigation.TopBar
import com.w_via.features.authentication.AuthManager
import com.w_via.features.authentication.AuthSessionStore
import com.w_via.features.routes.Routes
import com.w_via.ui.theme.colorBrand
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    navController: NavController,
) {
    val currentUser = AuthManager.getCurrentUser()
    val fullName = remember(currentUser) {
        val metadata = currentUser?.userMetadata
        metadata?.get("full_name")?.jsonPrimitive?.contentOrNull?.trim()
            ?: metadata?.get("name")?.jsonPrimitive?.contentOrNull?.trim()
            ?: ""
    }
    val avatarUrl = remember(currentUser) {
        val metadata = currentUser?.userMetadata
        metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("picture")?.jsonPrimitive?.contentOrNull
    }
    val email = remember(currentUser) { currentUser?.email.orEmpty() }

    var showLogoutSheet by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }
    val authSessionStore: AuthSessionStore = koinInject()
    val scope = rememberCoroutineScope()

    fun fazerLogout() {
        if (isLoggingOut) return
        isLoggingOut = true
        scope.launch {
            try {
                AuthManager.signOut()
            } catch (e: Exception) {
                // Falha na rede não deve impedir o logout local
                android.util.Log.e("ProfileScreen", "Erro ao encerrar sessão remota: ${e.message}")
            }
            authSessionStore.signOutLocal()
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = { TopBar(title = "Perfil") },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ProfileHeader(
                    fullName = fullName,
                    email = email,
                    avatarUrl = avatarUrl
                )
            }

            item {
                SectionTitle(text = "Conta")
            }
//            item {
//                ProfileMenuItem(
//                    title = "Dados pessoais",
//                    icon = Icons.Outlined.Person,
//                    onClick = { navController.navigate(Routes.DADOS_PESSOAIS) }
//                )
//            }
            item {
                ProfileMenuItem(
                    title = "Informações profissionais",
                    icon = Icons.Outlined.Work,
                    onClick = { navController.navigate(Routes.INFORMACOES_PROFISSIONAIS) }
                )
            }

            item {
                SectionTitle(text = "Sobre o app")
            }
            item {
                ProfileMenuItem(
                    title = "Termos de uso",
                    icon = Icons.Outlined.Description,
                    onClick = { navController.navigate(Routes.TERMOS_USO) }
                )
            }
            item {
                ProfileMenuItem(
                    title = "Termos de privacidade",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = { navController.navigate(Routes.POLITICA_PRIVACIDADE) }
                )
            }
            item {
                ProfileMenuItem(
                    title = "Licenças do aplicativo",
                    icon = Icons.Outlined.VerifiedUser,
                    onClick = { navController.navigate(Routes.LICENCAS) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                ProfileMenuItem(
                    title = "Sair da conta",
                    icon = Icons.Outlined.Logout,
                    tint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = { showLogoutSheet = true }
                )
            }
        }
    }

    if (showLogoutSheet) {
        ConfirmacaoBottomSheet(
            onDismissRequest = { showLogoutSheet = false },
            onConfirmar = { fazerLogout() },
            onCancelar = {},
            textoPrincipal = "Sair da conta",
            textoSecundario = "Você deseja sair da sua conta?",
            textoComplementar = "Suas informações continuarão salvas nesta conta.",
            textoBotaoConfirmar = "Sair da conta"
        )
    }
}

@Composable
private fun ProfileHeader(
    fullName: String,
    email: String,
    avatarUrl: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .border(3.dp, colorBrand, CircleShape)
                .background(
                    color = colorBrand.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = fullName.ifEmpty { "Agente" },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (email.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    editable: Boolean = false,
    tint: Color = colorBrand,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.12f), RoundedCornerShape(0.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (editable) {
                Text(
                    text = "Editar",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorBrand,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}
