package com.w_via.designsystem.bottomsheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.w_via.ui.theme.colorBrand

@Composable
fun BottomSheetContador(
    onNumberSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    total: Int,
    title: String
) {
    CustomBottomSheet(
        onDismissRequest = onDismissRequest,
        title,
        showCloseButton = true
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            items(List(total) { it + 1 }) { number ->
                NumberItem(
                    number = number,
                    onNumberSelected = {
                        onNumberSelected(it)
                        onDismissRequest()
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp))
                .height(46.dp)
                .background(colorBrand)
                .clickable { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cancelar",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun NumberItem(
    number: Int,
    onNumberSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onNumberSelected(number) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.background
        )
    }
}