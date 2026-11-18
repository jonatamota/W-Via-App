package com.w_via.features.declaration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Indicador para "casos excepcionais": um campo oculto por regra de exibição
 * (menor de idade, sexo fora da faixa etc.) pode ser revelado para preenchimento
 * excepcional. Ao tocar, dispara [onReveal], que normalmente ativa a flag
 * `excecao*` do estado.
 *
 * Componente legado mantido para compatibilidade — prefira [ExcecaoAutoInfo].
 */
@Composable
fun ExcecaoIndicator(
    text: String,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onReveal, modifier = modifier) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Bloco harmonioso para casos de exceção automática: exibe a [mensagem]
 * informativa e a ação para informar o campo mesmo assim ([acao]), unificadas
 * em um único cartão no padrão do Design System do app.
 *
 * Usado nos casos de estado civil automático, reservista e filhos.
 */
@Composable
fun ExcecaoAutoInfo(
    mensagem: String,
    acao: String,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mensagem,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            TextButton(
                onClick = onReveal,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    text = acao,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }
        }
    }
}
