package com.w_via.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w_via.designsystem.CustomSwitch
import com.w_via.designsystem.input.BankNumberField
import com.w_via.designsystem.appbuttons.PrimaryButton

private const val MaxDigitosValor = 8

@Composable
fun CalculatorBoxBottom(
    onCalcularClick: () -> Unit,
    onIdaVoltaChange: (Boolean) -> Unit,
    idaVolta: Boolean,
    valorKm: Long,
    onValorKmChange: (Long) -> Unit,
    valorCombustivel: Long,
    onValorCombustivelChange: (Long) -> Unit,
    autonomia: String,
    onAutonomiaChange: (String) -> Unit,
    valorAdicional: Long,
    onValorAdicionalChange: (Long) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ValorComLabel(
                label = "Valor/KM (R$)",
                value = valorKm,
                onValueChange = onValorKmChange,
                surfaceVariant = surfaceVariant,
                textColor = onSurface,
                modifier = Modifier.weight(1f)
            )
            ValorComLabel(
                label = "Preço/L (R$)",
                value = valorCombustivel,
                onValueChange = onValorCombustivelChange,
                surfaceVariant = surfaceVariant,
                textColor = onSurface,
                modifier = Modifier.weight(1f)
            )
            AutonomiaIntField(
                value = autonomia,
                onValueChange = onAutonomiaChange,
                label = "Autonomia",
                modifier = Modifier.weight(1f)
            )
            ValorComLabel(
                label = "+ Custos (R$)",
                value = valorAdicional,
                onValueChange = onValorAdicionalChange,
                surfaceVariant = surfaceVariant,
                textColor = onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calcular ida e volta",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
                    .padding(start = 8.dp),
            )
            CustomSwitch(
                checked = idaVolta,
                onCheckedChange = onIdaVoltaChange
            )
        }

        val focusManager = LocalFocusManager.current

        PrimaryButton(
            onClick = {
                focusManager.clearFocus() // fecha o teclado
                onCalcularClick()
            },
            text = "Calcular rota",
            isLoading = isLoading,
            isEnabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun ValorComLabel(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit,
    surfaceVariant: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 2,
            maxLines = 2,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        BankNumberField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            textColor = textColor,
            backgroundColor = surfaceVariant,
            maxDigits = MaxDigitosValor,
            showSymbol = false
        )
    }
}

@Composable
private fun AutonomiaIntField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 2,
            maxLines = 2,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        BasicTextField(
            value = androidx.compose.ui.text.input.TextFieldValue(
                text = value,
                selection = androidx.compose.ui.text.TextRange(value.length)
            ),
            onValueChange = { newValue ->
                val digits = newValue.text.filter { it.isDigit() }.take(2)
                onValueChange(digits)
            },
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
            ),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "0",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorBoxBottomPreview() {
    MaterialTheme {
        CalculatorBoxBottom(
            onCalcularClick = {},
            onIdaVoltaChange = {},
            idaVolta = false,
            valorKm = 50L,
            onValorKmChange = {},
            valorCombustivel = 575L,
            onValorCombustivelChange = {},
            autonomia = "12",
            onAutonomiaChange = {},
            valorAdicional = 1000L,
            onValorAdicionalChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
