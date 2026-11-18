package com.w_via.features.authentication.finishlogin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w_via.features.authentication.AuthManager
import com.w_via.features.authentication.AuthSessionStore
import com.w_via.features.authentication.data.RegistrationRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserUpdateBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FinishCreateAuthViewModel : ViewModel(), KoinComponent {

    private val authSessionStore: AuthSessionStore by inject()
    private val registrationRepository: RegistrationRepository by inject()

    // Modelos de dados
    data class UserData(
        val name: String = "",
        val birthday: String = "",
        val phone: String = "",
        val email: String = "",
        val sex: String = "",
        val accountTime: String = "",
    )

    data class FormState(
        val name: String = "",
        val birthday: String = "",
        val phone: String = "",
        val email: String = "",
        val nameError: String? = null,
        val emailError: String? = null,
    ) {
        val isNameValid: Boolean
            get() = name.trim().split(" ").size >= 2 &&
                    name.trim().contains(" ") &&
                    name.trim().split(" ").all { it.isNotBlank() && it[0].isUpperCase() && it.drop(1).all { c -> c.isLowerCase() || c == 'ç' || c == 'ã' || c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú' || c == 'â' || c == 'ê' || c == 'ô' || c == 'õ' || c == 'ü' || c == 'à' || c == 'è' || c == 'ì' || c == 'ò' || c == 'ù' || c == 'ä' || c == 'ë' || c == 'ï' || c == 'ö' || c == 'ÿ' || c == 'ñ' || c == ' ' } }
        val isEmailValid: Boolean
            get() = email.isNotBlank() && FinishCreateAuthViewModel.isEmailAllowed(email)
        val isValid: Boolean
            get() = isNameValid && isEmailValid
    }

    companion object Companion {
        private val allowedDomains = listOf(
            "@gmail.com", "@outlook.com", "@hotmail.com", "@yahoo.com",
            "@icloud.com", "@aol.com", "@setdrive.com.br"
        )
        fun isEmailAllowed(email: String): Boolean {
            val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}")
            return regex.matches(email) && allowedDomains.any { email.endsWith(it, ignoreCase = true) }
        }
    }

    fun validateForm(form: FormState): FormState {
        val nameError = if (!form.isNameValid) "Digite seu nome completo (nome e sobrenome), cada um começando com maiúscula" else null
        val emailError = if (!form.isEmailValid) "E-mail inválido ou domínio não permitido" else null
        return form.copy(
            nameError = nameError,
            emailError = emailError
        )
    }

    // Formulário
    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    fun onFormChange(updated: FormState) {
        _formState.value = updated
    }

    // Dados do usuário
    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData.asStateFlow()

    fun atualizarUserData(novo: UserData) {
        Log.d("ViewModel", "Atualizando UserData: $novo")
        _userData.value = novo
    }

    // Estados
    private val _registroResultado = MutableStateFlow<String?>(null)
    val registroResultado: StateFlow<String?> = _registroResultado.asStateFlow()

    private val _contaCriadaComSucesso = MutableStateFlow(false)
    val contaCriadaComSucesso: StateFlow<Boolean> = _contaCriadaComSucesso.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    /**
     * Retorna o primeiro nome do usuário a partir dos metadados da sessão Supabase.
     */
    fun getPrimeiroNome(): String {
        val metadata = AuthManager.getCurrentUser()?.userMetadata ?: return ""
        val nomeCompleto = metadata["full_name"]?.jsonPrimitive?.contentOrNull?.trim()
            ?: metadata["name"]?.jsonPrimitive?.contentOrNull?.trim()
            ?: return ""
        return nomeCompleto.split(" ").firstOrNull() ?: ""
    }

    /**
     * Atualiza os metadados do usuário no Supabase com os dados pessoais e
     * marca register_complete = true.
     */
    fun atualizarRegisterComplete(
        phone: String,
        birthday: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        val uid = AuthManager.getCurrentUserId()
        if (uid == null) {
            onError?.invoke("Usuário não autenticado")
            return
        }

        _isSubmitting.value = true
        viewModelScope.launch {
            try {
                val client = AuthManager.getClient()
                client.auth.updateUser {
                    data = buildJsonObject {
                        put("phone", phone)
                        put("birthday", birthday)
                        put("register_complete", true)
                        put("first_steps", false)
                    }
                }
                Log.d("FinishCreateAuthVM", "Metadados atualizados com sucesso para UID: $uid")

                // Persiste birthday + phone em `personal_data` no Supabase (Postgrest + RLS).
                // Marca `personal_complete` em `status_registration` (upsert) no sucesso.
                registrationRepository.savePersonalData(
                    birthday = birthday,
                    phone = phone
                )

                _contaCriadaComSucesso.value = true
                // Campo pessoal completo localmente: agiliza a navegação na próxima abertura.
                // O registro ainda não está 100% ok porque falta a etapa profissional.
                authSessionStore.updateRegistration(
                    personalComplete = true,
                    professionalComplete = false
                )
                onSuccess?.invoke()
            } catch (e: Exception) {
                Log.e("FinishCreateAuthVM", "Erro ao atualizar metadados: ${e.message}")
                onError?.invoke(e.message ?: "Erro desconhecido")
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
