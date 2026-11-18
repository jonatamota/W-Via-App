package com.w_via.features.profile

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.w_via.BuildConfig
import com.w_via.features.appnavigation.TopBar
import com.w_via.ui.theme.colorBrand

/**
 * Exibe as licenças open-source das dependências do app a partir do
 * relatório gerado pelo plugin `com.jaredsburrows.license`
 * (`open_source_licenses.json` em `assets/`), no mesmo estilo de detalhes
 * usado no recibo e na declaração de óbito.
 */
fun Context.openUrlInBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

@Composable
fun LicenseScreen(
    navController: NavController,
    viewModel: LicenseViewModel = viewModel(),
) {
    val context = LocalContext.current
    val licenses by viewModel.licenses.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLicenses(context)
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            TopBar(
                title = "Licenças do aplicativo",
                showBackButton = true,
                onBackPress = { navController.popBackStack() },
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (licenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 32.dp
                )
            ) {
                item {
                    AppVersionHeader()
                }
                items(licenses) { license ->
                    LicenseItem(license = license, context = context)
                }
            }
        }
    }
}

@Composable
private fun AppVersionHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Versão do aplicativo",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun LicenseItem(
    license: LicenseClass,
    context: Context,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = license.project,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorBrand,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "Recolher" else "Expandir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val rows = licenseRows(license)
                        rows.forEachIndexed { index, row ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                            when (row) {
                                is LicenseRowItem.Value -> LicenseValueRow(label = row.label, value = row.value)
                                is LicenseRowItem.Link -> LicenseLinkRow(
                                    label = row.label,
                                    url = row.url,
                                    context = context
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed interface LicenseRowItem {
    data class Value(val label: String, val value: String) : LicenseRowItem
    data class Link(val label: String, val url: String) : LicenseRowItem
}

private fun licenseRows(license: LicenseClass): List<LicenseRowItem> = buildList {
    add(LicenseRowItem.Value("Dependência", license.dependency ?: "—"))
    add(LicenseRowItem.Value("Versão", license.version))
    license.description
        ?.takeIf { it.isNotBlank() }
        ?.let { add(LicenseRowItem.Value("Descrição", it)) }
    license.developers
        ?.takeIf { it.isNotEmpty() }
        ?.let { add(LicenseRowItem.Value("Desenvolvedores", it.joinToString(", "))) }
    license.year
        ?.takeIf { it.isNotBlank() }
        ?.let { add(LicenseRowItem.Value("Ano", it)) }
    license.licenses.forEach { lic ->
        add(LicenseRowItem.Value("Licença", lic.license))
        lic.license_url
            ?.takeIf { it.isNotBlank() }
            ?.let { add(LicenseRowItem.Link("URL da licença", it)) }
    }
    license.url
        ?.takeIf { it.isNotBlank() }
        ?.let { add(LicenseRowItem.Link("Site", it)) }
}

@Composable
private fun LicenseValueRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LicenseLinkRow(label: String, url: String, context: Context) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = url,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { context.openUrlInBrowser(url) }
        )
    }
}
