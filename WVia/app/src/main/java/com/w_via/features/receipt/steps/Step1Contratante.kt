package com.w_via.features.receipt.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.w_via.designsystem.input.CnpjSearchField
import com.w_via.features.components.CepSearchField
import com.w_via.features.components.CpfSearchFieldWithFeedback
import com.w_via.features.components.UFAndCityFields
import com.w_via.features.components.rememberMunicipios
import com.w_via.features.declaration.DeclarationFieldErrors
import com.w_via.features.utils.PhoneFormatter

@Composable
fun Step1Contratante(
    nomeContratante: String,
    onNomeContratanteChange: (String) -> Unit,
    cpfCnpj: String,
    onCpfCnpjChange: (String) -> Unit,
    cpfCnpjMode: String,
    onCpfCnpjModeChange: (String) -> Unit,
    cepContratante: String,
    onCepContratanteChange: (String) -> Unit,
    buscandoCep: Boolean,
    cepError: String?,
    onSearchCep: () -> Unit,
    enderecoContratante: String,
    onEnderecoContratanteChange: (String) -> Unit,
    ufContratante: String,
    onUfContratanteChange: (String) -> Unit,
    cidadeContratante: String,
    onCidadeContratanteChange: (String) -> Unit,
    numeroContratante: String,
    onNumeroContratanteChange: (String) -> Unit,
    bairroContratante: String,
    onBairroContratanteChange: (String) -> Unit,
    tel: String,
    onTelChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    onSearchCNPJ: () -> Unit,
    isLoadingCNPJ: Boolean,
    cnpjError: String?,
    modifier: Modifier = Modifier
) {
    val isCnpjMode = cpfCnpjMode == "CNPJ"
    val municipios = rememberMunicipios()

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dados do Contratante", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(
                    onClick = {
                        onCpfCnpjModeChange(if (isCnpjMode) "CPF" else "CNPJ")
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(cpfCnpjMode, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isCnpjMode) {
                CnpjSearchField(
                    value = cpfCnpj,
                    onValueChange = onCpfCnpjChange,
                    onSearchClick = onSearchCNPJ,
                    isLoading = isLoadingCNPJ,
                    isError = cnpjError != null
                )

                cnpjError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            } else {
                CpfSearchFieldWithFeedback(
                    value = cpfCnpj,
                    onValueChange = onCpfCnpjChange,
                    errorMessage = DeclarationFieldErrors.cpf(cpfCnpj)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Demais campos em coluna
            OutlinedTextField(
                value = nomeContratante.uppercase(),
                onValueChange = { onNomeContratanteChange(it.uppercase()) },
                label = { Text("Nome do Contratante") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            CepSearchField(
                value = cepContratante,
                onValueChange = onCepContratanteChange,
                onSearchClick = onSearchCep,
                isLoading = buscandoCep,
                isError = cepError != null,
                supportingText = cepError
            )
            Spacer(modifier = Modifier.height(8.dp))

            var addressExpanded by remember {
                mutableStateOf(
                    enderecoContratante.isNotBlank() ||
                        ufContratante.isNotBlank() ||
                        cidadeContratante.isNotBlank() ||
                        numeroContratante.isNotBlank() ||
                        bairroContratante.isNotBlank()
                )
            }
            val addressArrowRotation by animateFloatAsState(
                targetValue = if (addressExpanded) 180f else 0f,
                label = "address_arrow"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { addressExpanded = !addressExpanded }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (addressExpanded) "Ocultar endereço" else "Ver mais",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (addressExpanded) "Recolher endereço" else "Expandir endereço",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .graphicsLayer { rotationZ = addressArrowRotation }
                        .size(20.dp)
                )
            }

            AnimatedVisibility(visible = addressExpanded) {
                Column {
                    UFAndCityFields(
                        uf = ufContratante.ifBlank { null },
                        onUfSelected = onUfContratanteChange,
                        city = cidadeContratante,
                        onCityChange = onCidadeContratanteChange,
                        municipios = municipios
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = enderecoContratante.uppercase(),
                        onValueChange = { onEnderecoContratanteChange(it.uppercase()) },
                        label = { Text("Endereço") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = numeroContratante,
                            onValueChange = onNumeroContratanteChange,
                            label = { Text("Número") },
                            modifier = Modifier.weight(0.35f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bairroContratante,
                            onValueChange = onBairroContratanteChange,
                            label = { Text("Bairro") },
                            modifier = Modifier.weight(0.65f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            var telState by remember { mutableStateOf(TextFieldValue(PhoneFormatter.format(tel))) }
            LaunchedEffect(tel) {
                val formatted = PhoneFormatter.format(tel)
                if (formatted != telState.text) telState = TextFieldValue(formatted)
            }
            OutlinedTextField(
                value = telState,
                onValueChange = { newValue ->
                    val (formatted, newSelection) = PhoneFormatter.formatWithCursor(
                        newValue.text,
                        newValue.selection.start
                    )
                    telState = TextFieldValue(formatted, TextRange(newSelection))
                    onTelChange(formatted)
                },
                label = { Text("Telefone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-mail") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
