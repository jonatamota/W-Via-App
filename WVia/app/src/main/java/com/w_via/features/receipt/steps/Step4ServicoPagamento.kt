package com.w_via.features.receipt.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w_via.designsystem.bottomsheets.DatePickerBottomSheet
import com.w_via.designsystem.input.BankNumberField
import com.w_via.designsystem.input.OutlinedFieldBox
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

@Composable
fun Step4ServicoPagamento(
    descricao: String,
    onDescricaoChange: (String) -> Unit,
    onAdicionarTranslado: () -> Unit,
    valorTotal: Double,
    onValorTotalChange: (Double) -> Unit,
    totalPago: Double,
    onTotalPagoChange: (Double) -> Unit,
    dataPagamento: String,
    onDataPagamentoChange: (String) -> Unit,
    pdfMessage: String?,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var totalPagoLong by remember(totalPago) { mutableStateOf((totalPago * 100).toLong()) }
    var valorTotalLong by remember(valorTotal) { mutableStateOf((valorTotal * 100).toLong()) }

    // Parse da data da String
    val localDate = remember(dataPagamento) {
        if (dataPagamento.isNotBlank()) {
            try {
                val parts = dataPagamento.split("/")
                if (parts.size == 3) {
                    val day = parts[0].toInt()
                    val month = parts[1].toInt()
                    val year = parts[2].toInt()
                    LocalDate(year, month, day)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    val displayDate = remember(localDate) {
        localDate?.let { date ->
            "%02d/%02d/%04d".format(date.dayOfMonth, date.monthNumber, date.year)
        } ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // DADOS DO SERVIÇO
            Text(text = "Dados do Serviço", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = descricao.uppercase(),
                onValueChange = { onDescricaoChange(it.uppercase()) },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )
            val showTransladoButton = descricao.isBlank() || descricao.trimEnd().endsWith(",")
            if (showTransladoButton) {
                OutlinedButton(
                    onClick = onAdicionarTranslado,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("Adicionar Translado")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // DADOS DE PAGAMENTO
            Text(text = "Dados de Pagamento", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Valor Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .width(1.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))
                BankNumberField(
                    value = valorTotalLong,
                    onValueChange = { newValue ->
                        valorTotalLong = newValue
                        onValorTotalChange(newValue / 100.0)
                    },
                    modifier = Modifier.weight(2f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Valor Pago",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .width(1.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))
                BankNumberField(
                    value = totalPagoLong,
                    onValueChange = { newValue ->
                        totalPagoLong = newValue
                        onTotalPagoChange(newValue / 100.0)
                    },
                    modifier = Modifier.weight(2f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedFieldBox(
                value = displayDate,
                label = "Data de Pagamento",
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
                },
                backgroundColor = MaterialTheme.colorScheme.surface
            )

            // Mensagem de resultado PDF
            pdfMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("sucesso")) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerBottomSheet(
            date = localDate,
            onDateChange = { date ->
                if (date != null) {
                    // Formata a data como String no formato dd/MM/yyyy
                    val dateString = "%02d/%02d/%04d".format(
                        date.dayOfMonth,
                        date.monthNumber,
                        date.year
                    )
                    onDataPagamentoChange(dateString)
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }
}