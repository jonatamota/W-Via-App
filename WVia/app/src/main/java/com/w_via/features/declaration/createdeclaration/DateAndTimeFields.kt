package com.w_via.features.declaration.createdeclaration

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.w_via.designsystem.bottomsheets.DatePickerBottomSheet
import com.w_via.designsystem.bottomsheets.TimePickerBottomSheet
import com.w_via.designsystem.input.OutlinedFieldBox
import com.w_via.features.utils.AgeCalculator

/**
 * Campo de data usando o componente de calendário do design system. O valor é
 * armazenado como texto no padrão `dd/MM/yyyy`, exibido de forma somente leitura.
 */
@Composable
fun DeclarationDateField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Toque para selecionar a data"
) {
    var showPicker by remember { mutableStateOf(false) }
    val date = remember(value) { AgeCalculator.parse(value) }

    OutlinedFieldBox(
        value = value,
        label = label,
        onClick = { showPicker = true },
        modifier = modifier,
        placeholder = placeholder,
        trailingIcon = {
            Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
        },
        backgroundColor = MaterialTheme.colorScheme.surface
    )

    if (showPicker) {
        DatePickerBottomSheet(
            date = date,
            onDateChange = { d ->
                onValueChange(d?.let { AgeCalculator.format(it) } ?: "")
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

/**
 * Campo de hora usando o seletor de horário do design system. O valor é
 * armazenado como texto no padrão `HH:mm`, exibido de forma somente leitura.
 */
@Composable
fun DeclarationTimeField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Toque para selecionar a hora"
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedFieldBox(
        value = value,
        label = label,
        onClick = { showPicker = true },
        modifier = modifier,
        placeholder = placeholder,
        trailingIcon = {
            Icon(Icons.Default.AccessTime, contentDescription = "Selecionar hora")
        },
        backgroundColor = MaterialTheme.colorScheme.surface
    )

    if (showPicker) {
        TimePickerBottomSheet(
            date = java.time.LocalDate.now(),
            currentValue = parseTimeToFloat(value),
            onDismiss = { showPicker = false },
            onConfirm = { hour, minute, _ ->
                onValueChange("%02d:%02d".format(hour, minute))
                showPicker = false
            }
        )
    }
}

private fun parseTimeToFloat(value: String): Float? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour + minute / 60f
}
