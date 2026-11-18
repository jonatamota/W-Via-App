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
 * Confirmação antes de salvar um documento (recibo ou declaração).
 * Exibida ao tocar no botão de salvar, garantindo que o usuário confirme
 * antes de gerar o PDF e persistir.
 */
@Composable
fun SaveConfirmationBottomSheet(
    title: String,
    message: String,
    confirmLabel: String = "Salvar",
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
            onClick = onDismiss,
            text = "Voltar",
            containerColorEnable = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            onClick = onConfirm,
            text = confirmLabel,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}