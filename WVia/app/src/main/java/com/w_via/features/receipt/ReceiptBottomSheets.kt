package com.w_via.features.receipt

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
 * Confirmação ao sair da criação do recibo. Exibida quando há rascunho
 * preenchido e o usuário volta pelo primeiro passo (ver [CreateReciboScreen]).
 */
@Composable
fun ReceiptExitBottomSheet(
    onDismiss: () -> Unit,
    onContinueLater: () -> Unit,
    onDeleteDraft: () -> Unit,
) {
    CustomBottomSheet(
        onDismissRequest = onDismiss,
        title = "Sair da criação do recibo?",
        showCloseButton = true
    ) {
        Text(
            text = "Você pode continuar de onde parou mais tarde, ou excluir o que já foi preenchido.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            onClick = onContinueLater,
            text = "Continuar depois",
            containerColorEnable = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            onClick = onDeleteDraft,
            text = "Excluir rascunho",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
