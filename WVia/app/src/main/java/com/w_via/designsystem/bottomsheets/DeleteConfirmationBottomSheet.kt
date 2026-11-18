package com.w_via.designsystem.bottomsheets

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

/**
 * Confirmação de exclusão (individual ou em lote). Segue o mesmo padrão do
 * [com.w_via.features.receipt.ReceiptExitBottomSheet]: `CustomBottomSheet` com
 * um `PrimaryButton` secundário para cancelar e um destrutivo para confirmar.
 *
 * @param title Texto exibido como título, ex.: "Excluir recibo?".
 * @param message Descrição do que será excluído, ex.: "3 recibos serão excluídos.".
 * @param confirmText Texto do botão de confirmação, ex.: "Excluir".
 */
@Composable
fun DeleteConfirmationBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Excluir",
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
            onClick = onDismiss,
            text = "Cancelar",
            containerColorEnable = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            onClick = onConfirm,
            text = confirmText,
            containerColorEnable = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
