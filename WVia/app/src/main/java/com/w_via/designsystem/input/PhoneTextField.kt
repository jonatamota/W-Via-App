package com.w_via.designsystem.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PhoneNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "(00) 00000-0000",
    countryCode: String = "+55",
    onCountryCodeClick: (() -> Unit)? = null,
    error: String? = null,
    isError: Boolean = error != null
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Box para o código do país
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(70.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        color = if (isError) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable(enabled = onCountryCodeClick != null) {
                        onCountryCodeClick?.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Ícone da bandeira (pode substituir por imagem)
                    Text(
                        text = "🇧🇷", // Ou use um Image/Icon
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    Text(
                        text = countryCode,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Campo para o número de telefone
            CustomTextField(
                value = value,
                onValueChange = { newValue ->
                    // Filtra apenas números
                    val filtered = newValue.filter { it.isDigit() }

                    // Limita a 11 dígitos (DDD + 9 dígitos)
                    if (filtered.length <= 11) {
                        // Formata o número
                        val formatted = formatPhoneNumber(filtered)
                        onValueChange(formatted)
                    }
                },
                modifier = Modifier.weight(0.7f),
                label = "Telefone",
                placeholder = placeholder,
                isError = isError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
        }

        // Mensagem de erro
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

// Transformation para formatação do telefone
private class PhoneNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text
        val formatted = formatPhoneNumber(input)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset // DDD sem formatação ainda
                    offset <= 6 -> offset + 1 // Adiciona parêntese fechando
                    offset <= 10 -> offset + 2 // Adiciona hífen
                    else -> offset + 3 // Se tiver mais dígitos
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 3 -> offset // Até o parêntese
                    offset <= 8 -> offset - 1 // Depois do parêntese até hífen
                    offset <= 13 -> offset - 2 // Depois do hífen
                    else -> offset - 3
                }
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}

// Função para formatar o número de telefone
private fun formatPhoneNumber(input: String): String {
    val digits = input.filter { it.isDigit() }

    return when {
        digits.isEmpty() -> ""
        digits.length <= 2 -> digits // Apenas DDD
        digits.length <= 7 -> "(${digits.substring(0, 2)}) ${digits.substring(2)}"
        else -> "(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${digits.substring(7, 11)}"
    }
}

// Versão alternativa com TextField do Material 3
@Composable
fun PhoneNumberTextFieldV2(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "(00) 00000-0000",
    countryCode: String = "+55",
    onCountryCodeClick: (() -> Unit)? = null,
    error: String? = null
) {
    val isError = error != null

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Código do país com aparência de TextField
            OutlinedTextField(
                value = countryCode,
                onValueChange = {},
                modifier = Modifier
                    .weight(0.25f)
                    .heightIn(min = 56.dp),
                readOnly = true,
                enabled = false,
                label = { Text("Cód.") },
                leadingIcon = {
                    Text("🇧🇷", fontSize = 16.sp)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Campo do número
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    if (filtered.length <= 11) {
                        val formatted = formatPhoneNumber(filtered)
                        onValueChange(formatted)
                    }
                },
                modifier = Modifier.weight(0.75f),
                label = { Text("Telefone") },
                placeholder = { Text(placeholder) },
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = PhoneNumberTransformation(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

// Como usar:
@Preview
@Composable
fun PhoneNumberTextFieldPreview() {
    MaterialTheme {
        var phoneNumber by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PhoneNumberTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth(),
                error = error,
                onCountryCodeClick = {
                    // Navegar para seleção de país
                }
            )
        }
    }
}