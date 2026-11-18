package com.w_via.designsystem.appbuttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w_via.R

@Composable
fun ActionButton(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    size: Dp = 100.dp,
    text: String = "Botão",
    elevation: Dp = 8.dp,
    icon: @Composable (modifier: Modifier, tint: Color) -> Unit = { _, _ -> },
) {
    val iconSize = size * 0.30f
    val textSize = size * 0.18f
    val paddingSize = size * 0.1f

    Card(
        modifier = modifier
            .width(size * 0.6f)
            .height(size)
            .clickable(onClick = onClose),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
            pressedElevation = elevation * 2.5f
        ),
        shape = RoundedCornerShape(size * 0.01f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                icon(
                    Modifier
                        .size(iconSize)
                        .align(Alignment.Center),
                    iconColor
                )
            }

            Text(
                text = text,
                textAlign = TextAlign.Center,
                color = iconColor,
                fontSize = textSize.value.sp,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(bottom = paddingSize)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}


@Preview
@Composable
fun ActionButtonPreview() {
    MaterialTheme {
        Column {
            ActionButton(
                onClose = {},
                backgroundColor = MaterialTheme.colorScheme.primary,
                iconColor = MaterialTheme.colorScheme.onPrimary,
                text = "Botão",
                icon = { modifier, tint ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_doc_svgrepo_com),
                        contentDescription = "Voltar",
                        tint = tint,
                        modifier = modifier
                    )
                }
            )
        }
    }
}
