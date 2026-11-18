package com.w_via.features.declaration.createdeclaration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.w_via.features.declaration.DeclaracaoObitoThresholds
import com.w_via.features.declaration.ExcecaoAutoInfo
import com.w_via.features.declaration.LabeledTextField

/**
 * Seção opcional "Filhos". O nome do cônjuge agora é informado na seção
 * "Dados do Falecido" (aba Estado civil); esta seção permanece apenas com filhos.
 * A exibição depende da idade informada na seção "Dados do Falecido":
 *  - filhos: menor de 10 anos não aplicável (com indicador de caso excepcional).
 */
@Composable
fun ConjugeFilhosSectionScreen(
    navController: NavController,
    viewModel: DeclarationDraftViewModel,
) {
    DeclarationSectionHost(
        section = DeclarationSection.CONJUGE_FILHOS,
        navController = navController,
        viewModel = viewModel
    ) { s ->
        if (s.mostrarFilhos()) {
            Text("Filhos", modifier = Modifier.padding(top = 8.dp))
            s.filhos.forEachIndexed { index, nome ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabeledTextField(
                        label = "Filho(a) ${index + 1}",
                        value = nome,
                        onValueChange = { v -> viewModel.atualizarFilho(index, v) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = { viewModel.removerFilho(index) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remover filho")
                    }
                }
            }
            OutlinedButton(
                onClick = { viewModel.adicionarFilho() },
                enabled = s.filhos.size < DeclaracaoObitoThresholds.MAX_FILHOS
            ) {
                Text("Adicionar filho(a)")
            }
            if (s.filhos.size >= DeclaracaoObitoThresholds.MAX_FILHOS) {
                Text(
                    text = "Limite de ${DeclaracaoObitoThresholds.MAX_FILHOS} filhos atingido.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            ExcecaoAutoInfo(
                mensagem = "Filhos não aplicável para a idade informada.",
                acao = "Caso excepcional — informar filhos mesmo assim",
                onReveal = { viewModel.update { it.copy(excecaoFilhos = true) } }
            )
        }
    }
}
