package com.w_via.features.declaration.createdeclaration

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.designsystem.bottomsheets.CustomBottomSheet

/**
 * Confirmação de descarte do rascunho ao sair da tela do grid.
 * Exibida apenas quando há conteúdo preenchido (ver [DeclarationDraftViewModel.hasContent]).
 */
@Composable
fun DeclarationDiscardBottomSheet(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
) {
    CustomBottomSheet(
        onDismissRequest = onDismiss,
        title = "Descartar declaração?",
        showCloseButton = true
    ) {
        Text(
            text = "Se você sair agora, o preenchimento atual será perdido. " +
                "Deseja descartar o rascunho e voltar?",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            onClick = onDismiss,
            text = "Continuar preenchendo",
            containerColorEnable = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            onClick = onDiscard,
            text = "Descartar e voltar",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Resultado da geração do PDF (sucesso ou erro), exibido como bottom sheet.
 */
@Composable
fun DeclarationResultBottomSheet(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    CustomBottomSheet(
        onDismissRequest = onDismiss,
        title = title,
        showCloseButton = true
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            onClick = onConfirm,
            text = "OK",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Aviso de conformidade quando uma nova informação conflita com dados já
 * preenchidos. Os campos conflitantes já foram atualizados — este modal apenas
 * informa o usuário, que confirma para prosseguir.
 */
@Composable
fun DeclarationConflictBottomSheet(
    message: String,
    onDismiss: () -> Unit,
) {
    CustomBottomSheet(
        onDismissRequest = onDismiss,
        title = "Dados atualizados",
        showCloseButton = true
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            onClick = onDismiss,
            text = "Entendi",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
