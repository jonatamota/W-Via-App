package com.w_via.features.authentication.finishlogin

import ProgressStepper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.designsystem.bottomsheets.DatePickerBottomSheet
import com.w_via.designsystem.input.OutlinedFieldBox
import com.w_via.features.routes.Routes
import com.w_via.features.utils.AgeCalculator
import com.w_via.features.utils.PhoneFormatter
import com.w_via.ui.theme.colorBrand
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishCreateAuthScreen(
    navController: NavController,
) {
    val finishCreateAuthViewModel: FinishCreateAuthViewModel = viewModel()
    var telefone by remember { mutableStateOf(TextFieldValue()) }
    var dataNascimento by remember { mutableStateOf<LocalDate?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val primeiroNome = remember { finishCreateAuthViewModel.getPrimeiroNome() }

    // Validação dos campos
    val isTelefoneValid = PhoneFormatter.isValid(telefone.text)
    val idade = dataNascimento?.let { AgeCalculator.ageOn(it, today()).years }
    val isDataValid = dataNascimento != null && (idade ?: 0) >= 18
    val isFormValid = isTelefoneValid && isDataValid

    fun finalizarCadastro() {
        Log.d("AuthFlow", "[FINISH][START] Iniciando finalizarCadastro")
        isSubmitting = true

        finishCreateAuthViewModel.atualizarRegisterComplete(
            phone = telefone.text,
            birthday = dataNascimento?.toString() ?: "",
            onSuccess = {
                navController.navigate(Routes.UPLOAD_FILE) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onError = { error ->
                Log.e("AuthFlow", "[FINISH][ERROR] Erro ao atualizar metadados: $error")
                isSubmitting = false
            }
        )
    }

    // UI - Scaffold
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            ProgressStepper(
                currentStep = 3,
                modifier = Modifier.fillMaxWidth(),
                circleSize = 40.dp,
                strokeWidth = 2.dp,
                activeColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.primary
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = if (primeiroNome.isNotBlank()) "Olá! $primeiroNome\nTudo pronto para começar?" else "Tudo pronto para começar?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // Card de Progresso
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorBrand.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Preencha os campos a seguir ",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Preencha corretamente, esses dados ficarão vinculados a sua conta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Formulário
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Campo Telefone
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { novoValor ->
                        val resultado = PhoneFormatter.formatWithCursor(
                            novoValor.text,
                            novoValor.selection.start
                        )
                        if (PhoneFormatter.digitCount(resultado.first) <= 11) {
                            telefone = TextFieldValue(
                                text = resultado.first,
                                selection = PhoneFormatter.selectionFor(resultado.second)
                            )
                        }
                    },
                    label = { Text("Telefone") },
                    placeholder = { Text("(11) 99999-9999") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isTelefoneValid && telefone.text.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    supportingText = if (!isTelefoneValid && telefone.text.isNotEmpty()) {
                        { Text("Digite um telefone válido com DDD") }
                    } else null
                )

                // Campo Data de Nascimento — usa o mesmo campo clicável (área
                // inteira) do calendário do design system, como na "data do óbito".
                BirthDateField(
                    value = dataNascimento,
                    isError = !isDataValid && dataNascimento != null,
                    onDateChange = { data ->
                        dataNascimento = data
                        data?.let { dob ->
                            val anos = AgeCalculator.ageOn(dob, today()).years
                            if (anos < 18) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "É necessário ter 18 anos ou mais",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(30.dp))

            // Botão Finalizar
            PrimaryButton(
                onClick = { finalizarCadastro() },
                text = "Concluir e iniciar",
                modifier = Modifier
                    .fillMaxWidth(),
                isEnabled = isFormValid && !isSubmitting,
                isLoading = isSubmitting
            )

        }
    }
}

/**
 * Campo de data clicável por inteiro, usando o calendário do design system
 * (mesmo componente da "data do óbito"). Valor exibido em dd/MM/yyyy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateField(
    value: LocalDate?,
    isError: Boolean,
    onDateChange: (LocalDate?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedFieldBox(
        value = value?.let { AgeCalculator.format(it) } ?: "",
        label = "Data de Nascimento",
        onClick = { showPicker = true },
        trailingIcon = {
            Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
        },
        borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
        backgroundColor = MaterialTheme.colorScheme.background
    )

    if (showPicker) {
        DatePickerBottomSheet(
            date = value,
            onDateChange = { d ->
                onDateChange(d)
                showPicker = false
            },
            onDismiss = { showPicker = false },
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )
    }
}

private fun today(): LocalDate {
    val now = java.time.LocalDate.now()
    return LocalDate(now.year, now.monthValue, now.dayOfMonth)
}
