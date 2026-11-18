package com.w_via.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.w_via.features.appnavigation.TopBar
import com.w_via.features.company.data.EmpresaEntity
import com.w_via.features.company.data.EmpresaRepository
import com.w_via.features.sync.SyncManager
import com.w_via.opencnpj.CNPJFormatter
import com.w_via.features.utils.PhoneFormatter
import com.w_via.ui.components.DetailSection
import com.w_via.ui.theme.colorBrand
import org.koin.compose.koinInject
import java.io.File

/**
 * Tela "Informações profissionais": exibe os dados da funerária (logo, nome
 * fantasia, CNPJ, telefone e e-mail) guardados no cache local pelo
 * [com.w_via.features.sync.CompanyProfileSync] após a sincronização com o Supabase.
 */
@Composable
fun InformacoesProfissionaisScreen(
    navController: NavController,
) {
    val empresaRepository: EmpresaRepository = koinInject()
    val syncManager: SyncManager = koinInject()
    val empresa by empresaRepository.observe().collectAsState(initial = null)

    LaunchedEffect(Unit) {
        syncManager.syncCompanyProfile()
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            TopBar(
                title = "Informações profissionais",
                showBackButton = true,
                onBackPress = { navController.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val current = empresa
        if (current == null) {
            EmptyProfissionalState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 32.dp
                )
            ) {
                item { EmpresaHeader(empresa = current) }
                item {
                    DetailSection(
                        title = "Dados da empresa",
                        items = empresaItens(current)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmpresaHeader(empresa: EmpresaEntity) {
    val nomeFantasia = empresa.nomeFantasia
    val initials = nomeFantasia
        .split(" ")
        .filter { it.isNotBlank() }
        .mapNotNull { it.firstOrNull() }
        .take(2)
        .joinToString("")
        .uppercase()

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
            val logoPath = empresa.logoPath
            if (logoPath != null) {
                AsyncImage(
                    model = File(logoPath),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials.ifEmpty { "—" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorBrand
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = nomeFantasia.ifEmpty { "Empresa" },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private fun empresaItens(empresa: EmpresaEntity): List<Pair<String, String>> = buildList {
    if (empresa.cnpj.isNotBlank()) {
        add("CNPJ" to (CNPJFormatter.format(empresa.cnpj) ?: empresa.cnpj))
    }
    if (empresa.nomeFantasia.isNotBlank()) {
        add("Nome fantasia" to empresa.nomeFantasia)
    }
    if (empresa.telefone.isNotBlank()) {
        add("Telefone" to (PhoneFormatter.format(empresa.telefone).ifBlank { empresa.telefone }))
    }
    if (empresa.email.isNotBlank()) {
        add("E-mail" to empresa.email)
    }
}

@Composable
private fun EmptyProfissionalState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Work,
            contentDescription = null,
            tint = colorBrand,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Dados profissionais não sincronizados",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Entre novamente no app para sincronizar os dados da empresa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
