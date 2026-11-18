package com.w_via.features.declaration.createdeclaration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.features.appnavigation.TopBar
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.components.MissingFieldsBottomSheet

/**
 * Scaffold padrão para as telas de seção da Declaração de Óbito.
 * Estrutura semelhante à do recibo: TopBar, conteúdo rolável e botão
 * "Confirmar" fixo na parte inferior.
 */
@Composable
fun DeclarationSectionScaffold(
    title: String,
    navController: NavController,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    errorMessage: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            TopBar(
                title = title,
                showBackButton = true,
                onBackPress = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                PrimaryButton(
                    text = "Confirmar",
                    onClick = onConfirm,
                    isEnabled = confirmEnabled,
                    height = 52.dp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            content = content
        )
    }
}

/**
 * Host genérico que conecta uma seção ao [DeclarationDraftViewModel] compartilhado:
 * coleta o estado, gerencia a mensagem de erro e chama [DeclarationDraftViewModel.confirm]
 * no clique do botão "Confirmar", voltando para o grid em caso de sucesso.
 *
 * Para seções divididas em abas ([tabCount] > 1), o botão "Confirmar" avança para a
 * próxima aba; somente na última aba a seção é validada e confirmada, retornando ao grid.
 */
@Composable
fun DeclarationSectionHost(
    section: DeclarationSection,
    navController: NavController,
    viewModel: DeclarationDraftViewModel,
    tabCount: Int = 1,
    currentTab: Int = 0,
    onTabChange: (Int) -> Unit = {},
    content: @Composable (DeclaracaoObitoFormState) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMissingSheet by remember { mutableStateOf(false) }

    // Limpa a mensagem de erro assim que o usuário edita qualquer campo.
    LaunchedEffect(state) {
        if (errorMessage != null) errorMessage = null
    }

    DeclarationSectionScaffold(
        title = section.title,
        navController = navController,
        errorMessage = errorMessage,
        confirmEnabled = true, // Sempre habilitado
        onConfirm = {
            if (currentTab < tabCount - 1) {
                onTabChange(currentTab + 1)
            } else if (section.isComplete(state)) {
                if (viewModel.confirm(section)) {
                    navController.popBackStack()
                } else {
                    errorMessage = section.completionHint
                }
            } else {
                showMissingSheet = true
            }
        }
    ) {
        content(state)
    }

    val conflictMessage by viewModel.conflictMessage.collectAsState()
    conflictMessage?.let { message ->
        DeclarationConflictBottomSheet(
            message = message,
            onDismiss = viewModel::clearConflict
        )
    }

    if (showMissingSheet) {
        MissingFieldsBottomSheet(
            blocking = section.essentialsMissing(state),
            recommended = section.recommendedMissing(state),
            onDismiss = { showMissingSheet = false }
        )
    }
}
