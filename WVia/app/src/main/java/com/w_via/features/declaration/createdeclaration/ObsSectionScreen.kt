package com.w_via.features.declaration.createdeclaration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Seção opcional de observações. Não bloqueia a geração do PDF.
 */
@Composable
fun ObsSectionScreen(
    navController: NavController,
    viewModel: DeclarationDraftViewModel,
) {
    DeclarationSectionHost(
        section = DeclarationSection.OBS,
        navController = navController,
        viewModel = viewModel
    ) { s ->
        OutlinedTextField(
            value = s.observacoes.uppercase(),
            onValueChange = { v -> viewModel.update { it.copy(observacoes = v.uppercase()) } },
            label = { Text("Observações") },
            minLines = 6,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        )
    }
}
