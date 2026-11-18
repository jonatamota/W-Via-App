package com.w_via.features.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.designsystem.bottomsheets.CustomBottomSheet

/**
 * Modal sheet que lista os campos essenciais ainda não preenchidos antes de
 * finalizar um recibo ou declaração. Mostra duas seções: "Bloqueantes" (impedem
 * o envio) e "Recomendados" (não bloqueiam, mas sugeridos).
 *
 * @param blocking      Lista de campos bloqueantes faltantes.
 * @param recommended   Lista de campos recomendados faltantes (opcional).
 * @param onDismiss     Fecha o sheet.
 */
@Composable
fun MissingFieldsBottomSheet(
    blocking: List<String>,
    recommended: List<String> = emptyList(),
    onDismiss: () -> Unit,
) {
    CustomBottomSheet(
        onDismissRequest = onDismiss,
        title = "Campos pendentes",
        showCloseButton = true
    ) {
        if (blocking.isNotEmpty()) {
            Text(
                text = "Obrigatórios",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            blocking.forEach { field ->
                Text(
                    text = "• $field",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }

        if (recommended.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Recomendados",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
            recommended.forEach { field ->
                Text(
                    text = "• $field",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            onClick = onDismiss,
            text = "Continuar preenchendo",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
