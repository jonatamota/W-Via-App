package com.w_via.designsystem.appbuttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w_via.R

import com.w_via.ui.theme.colorBrand

@Composable
fun CloseButton(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = colorBrand.copy(alpha = 0.1f),
    iconColor: Color = MaterialTheme.colorScheme.outline,
    size: Dp = 50.dp,
    iconSize: Dp = size * 0.5f,
) {

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                onClick = onClose
            )
            .background(
                color = backgroundColor,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_x),
            contentDescription = "Fechar",
            tint = iconColor.copy(alpha = 0.7f),
            modifier = Modifier.size(iconSize)
        )
    }
}