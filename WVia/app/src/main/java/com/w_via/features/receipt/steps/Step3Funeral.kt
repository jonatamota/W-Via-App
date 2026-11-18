package com.w_via.features.receipt.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.w_via.designsystem.bottomsheets.TimePickerBottomSheet
import com.w_via.designsystem.bottomsheets.DatePickerBottomSheet
import com.w_via.designsystem.input.OutlinedFieldBox
import com.w_via.features.components.SuggestionAutocompleteField
import com.w_via.features.components.UFAndCityFields
import com.w_via.features.components.rememberLocaisBrasil
import com.w_via.features.components.rememberMunicipios
import com.w_via.features.declaration.SingleChoiceSelector
import com.w_via.features.receipt.TipoLocalSaida
import com.w_via.features.receipt.TipoSepultamento
import com.w_via.features.utils.LocaisBrasilData
import kotlinx.datetime.LocalDate
import java.time.LocalDate as JavaLocalDate

@Composable
fun Step3Funeral(
    tipoLocalSaida: TipoLocalSaida?,
    onTipoLocalSaidaChange: (TipoLocalSaida) -> Unit,
    ufLocalSaida: String,
    onUfLocalSaidaChange: (String) -> Unit,
    cidadeLocalSaida: String,
    onCidadeLocalSaidaChange: (String) -> Unit,
    localFuneral: String,
    onLocalFuneralChange: (String) -> Unit,
    horaSaidaFuneral: String,
    onHoraSaidaFuneralChange: (String) -> Unit,
    diaFuneral: LocalDate?,
    onDiaFuneralChange: (LocalDate?) -> Unit,
    tipoSepultamento: TipoSepultamento?,
    onTipoSepultamentoChange: (TipoSepultamento) -> Unit,
    ufSepultamento: String,
    onUfSepultamentoChange: (String) -> Unit,
    cidadeSepultamento: String,
    onCidadeSepultamentoChange: (String) -> Unit,
    cemiterioSepultamento: String,
    onCemiterioSepultamentoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val locais = rememberLocaisBrasil()
    val municipios = rememberMunicipios()

    val hospitais = if (tipoLocalSaida == TipoLocalSaida.HOSPITAL) {
        LocaisBrasilData.hospitaisPara(locais, ufLocalSaida, cidadeLocalSaida)
    } else {
        emptyList()
    }
    val cemiterios = if (tipoSepultamento == TipoSepultamento.CEMITERIO) {
        LocaisBrasilData.cemiteriosPara(locais, ufSepultamento, cidadeSepultamento)
    } else {
        emptyList()
    }

    val displayDate = remember(diaFuneral) {
        diaFuneral?.let { date ->
            "%02d/%02d/%04d".format(date.dayOfMonth, date.monthNumber, date.year)
        } ?: ""
    }

    LaunchedEffect(tipoLocalSaida) {
        if (tipoLocalSaida == TipoLocalSaida.CASA) {
            onLocalFuneralChange("RESIDÊNCIA")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "Dados do Funeral", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSelector(
                label = "Local de saída",
                options = TipoLocalSaida.entries,
                selected = tipoLocalSaida,
                onSelect = onTipoLocalSaidaChange,
                optionLabel = { it.label },
                labelAbove = true
            )
            UFAndCityFields(
                uf = ufLocalSaida.ifBlank { null },
                onUfSelected = onUfLocalSaidaChange,
                city = cidadeLocalSaida,
                onCityChange = onCidadeLocalSaidaChange,
                municipios = municipios
            )

            if (tipoLocalSaida == TipoLocalSaida.HOSPITAL) {
                SuggestionAutocompleteField(
                    value = localFuneral,
                    onValueChange = onLocalFuneralChange,
                    suggestions = hospitais,
                    placeholder = "Local de saída"
                )
            } else {
                OutlinedTextField(
                    value = localFuneral.uppercase(),
                    onValueChange = { onLocalFuneralChange(it.uppercase()) },
                    label = { Text("Local de saída") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = tipoLocalSaida != TipoLocalSaida.CASA
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedFieldBox(
                    value = horaSaidaFuneral,
                    label = "Hora de Saída",
                    onClick = { showTimePicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    trailingIcon = {
                        Icon(Icons.Default.AccessTime, contentDescription = "Selecionar hora")
                    },
                    backgroundColor = MaterialTheme.colorScheme.surface
                )

                OutlinedFieldBox(
                    value = displayDate,
                    label = "Dia",
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    trailingIcon = {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
                    },
                    backgroundColor = MaterialTheme.colorScheme.surface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSelector(
                label = "Local alvo",
                options = TipoSepultamento.entries,
                selected = tipoSepultamento,
                onSelect = onTipoSepultamentoChange,
                optionLabel = { it.label },
                labelAbove = true
            )
            UFAndCityFields(
                uf = ufSepultamento.ifBlank { null },
                onUfSelected = onUfSepultamentoChange,
                city = cidadeSepultamento,
                onCityChange = onCidadeSepultamentoChange,
                municipios = municipios
            )

            if (tipoSepultamento == TipoSepultamento.CEMITERIO) {
                SuggestionAutocompleteField(
                    value = cemiterioSepultamento,
                    onValueChange = onCemiterioSepultamentoChange,
                    suggestions = cemiterios,
                    placeholder = "Cemitério de Sepultamento"
                )
            } else {
                OutlinedTextField(
                    value = cemiterioSepultamento.uppercase(),
                    onValueChange = { onCemiterioSepultamentoChange(it.uppercase()) },
                    label = { Text("Funerária") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showTimePicker) {
        TimePickerBottomSheet(
            date = JavaLocalDate.now(),
            currentValue = null,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute, isNextDay ->
                onHoraSaidaFuneralChange("%02d:%02d".format(hour, minute))
                showTimePicker = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerBottomSheet(
            date = diaFuneral,
            onDateChange = { date ->
                onDiaFuneralChange(date)
            },
            onDismiss = { showDatePicker = false }
        )
    }
}