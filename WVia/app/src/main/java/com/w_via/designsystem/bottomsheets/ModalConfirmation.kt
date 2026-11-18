package com.w_via.designsystem.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w_via.designsystem.appbuttons.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmacaoBottomSheet(
    onDismissRequest: () -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    textoPrincipal: String,
    textoSecundario: String,
    textoComplementar: String,
    textoBotaoConfirmar: String = "Sair do App"
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = textoPrincipal,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = textoSecundario,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = textoComplementar,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                onClick = {
                    onConfirmar()
                    onDismissRequest()
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                text = textoBotaoConfirmar,
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                onClick = {
                    onCancelar()
                    onDismissRequest()
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                containerColorEnable = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurface,
                text = "Cancelar",
            )
        }
    }
}