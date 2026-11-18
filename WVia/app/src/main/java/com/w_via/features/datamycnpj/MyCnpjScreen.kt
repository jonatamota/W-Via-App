package com.w_via.features.datamycnpj

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.designsystem.input.CnpjSearchField
import com.w_via.features.routes.Routes
import com.w_via.features.utils.PhoneFormatter
import kotlinx.coroutines.launch

@Composable
fun MyCnpjScreen(
    navController: NavController,
    viewModel: UploadFileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.professionalSaved) {
        if (uiState.professionalSaved) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Long)
            viewModel.clearSnackbar()
        }
    }

    val cpfCnpj = uiState.cnpjNumber
    val nomeFantasia = uiState.nomeFantasia
    val telefone = uiState.telefone
    val email = uiState.email
    val endereco = uiState.endereco
    val isLoadingCNPJ = uiState.isLoading

    val cnpjValido = viewModel.isValidCnpj(cpfCnpj)
    val nomeValido = nomeFantasia.isNotBlank()
    val telefoneValido = viewModel.isValidPhone(telefone)
    val emailValido = viewModel.isValidEmail(email)
    val canContinue = cnpjValido && nomeValido && telefoneValido && emailValido

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                PrimaryButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    onClick = {
                        if (canContinue) {
                            viewModel.saveAndComplete()
                        } else {
                            val mensagem = when {
                                !cnpjValido -> "Informe um CNPJ válido (numérico ou alfanumérico)."
                                !nomeValido -> "Informe o nome fantasia."
                                !telefoneValido -> "Informe um telefone válido com DDD."
                                else -> "Informe um e-mail válido."
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = mensagem,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    disabledContainerColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.outline,
                    text = "Continuar",
                    isEnabled = canContinue && !uiState.isSavingProfData,
                    isLoading = uiState.isSavingProfData
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier
                    .padding(bottom = 2.dp),
                text = "Informe o seu CNPJ ",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

                CnpjSearchField(
                    value = cpfCnpj,
                    onValueChange = { formatted ->
                        viewModel.updateCnpj(formatted)
                    },
                    onSearchClick = { viewModel.searchCnpj() },
                    isLoading = isLoadingCNPJ,
                    isError = cpfCnpj.isNotEmpty() && !cnpjValido
                )

                OutlinedTextField(
                    value = nomeFantasia.uppercase(),
                    onValueChange = { viewModel.updateNomeFantasia(it.uppercase()) },
                    label = { Text("Nome Fantasia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isSavingProfData
                )

                var telState by remember { mutableStateOf(TextFieldValue(PhoneFormatter.format(telefone))) }
                LaunchedEffect(telefone) {
                    val formatted = PhoneFormatter.format(telefone)
                    if (formatted != telState.text) telState = TextFieldValue(formatted)
                }
                OutlinedTextField(
                    value = telState,
                    onValueChange = { novo ->
                        val (formatted, newSelection) = PhoneFormatter.formatWithCursor(
                            novo.text,
                            novo.selection.start
                        )
                        if (PhoneFormatter.digitCount(formatted) <= 11) {
                            telState = TextFieldValue(formatted, TextRange(newSelection))
                            viewModel.updateTelefone(formatted)
                        }
                    },
                    label = { Text("Telefone") },
                    placeholder = { Text("(11) 99999-9999") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSavingProfData,
                    isError = telefone.isNotEmpty() && !telefoneValido,
                    supportingText = if (telefone.isNotEmpty() && !telefoneValido) {
                        { Text("Telefone inválido com DDD") }
                    } else null
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("E-mail") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isSavingProfData,
                    isError = email.isNotEmpty() && !emailValido,
                    supportingText = if (email.isNotEmpty() && !emailValido) {
                        { Text("E-mail inválido") }
                    } else null
                )

                OutlinedTextField(
                    value = endereco.uppercase(),
                    onValueChange = { viewModel.updateEndereco(it.uppercase()) },
                    label = { Text("Endereço profissional") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isSavingProfData,
                    supportingText = {
                        Text("Preenchido automaticamente ao buscar o CNPJ, se disponível")
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyCnpjScreenPreview() {
    MyCnpjScreen(navController = rememberNavController())
}
