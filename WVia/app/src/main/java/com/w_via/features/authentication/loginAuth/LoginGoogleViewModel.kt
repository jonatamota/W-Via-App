package com.w_via.features.authentication.loginAuth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.w_via.features.authentication.AuthManager
import com.w_via.features.authentication.AuthSessionStore
import com.w_via.features.sync.SyncRunner
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginGoogleViewModel : ViewModel(), KoinComponent {

    private val authSessionStore: AuthSessionStore by inject()
    private val syncRunner: SyncRunner by inject()

    private val _loginState = MutableStateFlow<GoogleLoginState>(GoogleLoginState.Idle)
    val loginState: StateFlow<GoogleLoginState> = _loginState.asStateFlow()

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    sealed class GoogleLoginState {
        object Idle : GoogleLoginState()
        object Loading : GoogleLoginState()
        object Success : GoogleLoginState()
        data class Error(val message: String) : GoogleLoginState()
    }

    sealed class NavigationState {
        object Idle : NavigationState()
        object NavigateToFinalizeAccount : NavigationState()
        object NavigateToUploadFile : NavigationState()
        object NavigateToHome : NavigationState()
        object NavigateToErrorAccount : NavigationState()
    }

    fun signInWithGoogle(context: Context, clientId: String) {
        if (clientId.isBlank() || clientId == "missing-web-client-id") {
            _loginState.value = GoogleLoginState.Error(
                "Google Sign-In não configurado. Verifique o client ID do Google."
            )
            return
        }

        _loginState.value = GoogleLoginState.Loading

        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val result = requestGoogleCredential(credentialManager, context, clientId)
                processCredentialResult(result)
            } catch (e: NoCredentialException) {
                Log.e("GoogleLoginVM", "Nenhuma credencial encontrada: ${e.message}")
                _loginState.value = GoogleLoginState.Error(
                    "Nenhuma conta Google disponível. Adicione uma conta Google no dispositivo e tente novamente."
                )
            } catch (e: GetCredentialException) {
                Log.e("GoogleLoginVM", "Erro ao obter credencial: ${e.message}")
                _loginState.value =
                    GoogleLoginState.Error("Erro ao acessar conta Google: ${e.message}")
            } catch (e: Exception) {
                Log.e("GoogleLoginVM", "Erro inesperado: ${e.message}")
                _loginState.value = GoogleLoginState.Error("Erro inesperado: ${e.message}")
            }
        }
    }

    private suspend fun requestGoogleCredential(
        credentialManager: CredentialManager,
        context: Context,
        clientId: String
    ): GetCredentialResponse {
        return try {
            Log.d("GoogleLoginVM", "Iniciando fluxo do botão Google (GetSignInWithGoogleOption)")
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(clientId).build()
            val signInRequest = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            credentialManager.getCredential(context, signInRequest)
        } catch (e: NoCredentialException) {
            Log.w(
                "GoogleLoginVM",
                "Fluxo do botão indisponível, tentando credencial salva: ${e.message}"
            )
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val googleIdRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            credentialManager.getCredential(context, googleIdRequest)
        }
    }

    private fun processCredentialResult(result: GetCredentialResponse) {
        try {
            val credential = result.credential
            Log.d(
                "GoogleLoginVM",
                "Tipo de credencial recebida: ${credential.javaClass.simpleName}"
            )

            when (credential) {
                is GoogleIdTokenCredential -> {
                    Log.d("GoogleLoginVM", "Google ID Token obtido com sucesso")
                    signInWithSupabase(credential.idToken)
                }

                else -> {
                    try {
                        when (credential) {
                            is CustomCredential -> {
                                Log.w(
                                    "GoogleLoginVM",
                                    "CustomCredential detectada: ${credential.type}"
                                )

                                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    try {
                                        val googleIdTokenCredential =
                                            GoogleIdTokenCredential.createFrom(credential.data)
                                        signInWithSupabase(googleIdTokenCredential.idToken)
                                    } catch (e: Exception) {
                                        Log.e(
                                            "GoogleLoginVM",
                                            "Erro ao converter CustomCredential: ${e.message}"
                                        )
                                        _loginState.value =
                                            GoogleLoginState.Error("Erro ao processar credencial do Google: ${e.message}")
                                    }
                                } else {
                                    _loginState.value =
                                        GoogleLoginState.Error("Tipo de credencial customizada não suportada: ${credential.type}")
                                }
                            }

                            else -> {
                                val credentialType = credential::class.java.simpleName
                                _loginState.value =
                                    GoogleLoginState.Error("Tipo de credencial não suportado: $credentialType")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "GoogleLoginVM",
                            "Erro ao processar credencial alternativa: ${e.message}"
                        )
                        _loginState.value =
                            GoogleLoginState.Error("Erro ao processar credencial: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleLoginVM", "Erro ao processar resultado da credencial: ${e.message}")
            _loginState.value = GoogleLoginState.Error("Erro ao processar credencial: ${e.message}")
        }
    }

    /**
     * Autentica no Supabase usando o ID token do Google.
     * O Supabase Auth gerencia a criação/atualização do usuário automaticamente.
     */

    @Serializable
    data class RegistrationGateResponse(
        val personal_complete: Boolean = false,
        val professional_complete: Boolean = false
    )

    private fun signInWithSupabase(idToken: String) {
        viewModelScope.launch {
            try {
                val client = AuthManager.getClient()

                client.auth.signInWith(IDToken) {
                    provider = Google
                    this.idToken = idToken
                }

                val user = AuthManager.getCurrentUser()
                Log.d("GoogleLoginVM", "Login Supabase bem-sucedido: ${user?.email}")

                if (user == null) {
                    _loginState.value = GoogleLoginState.Error("Erro ao autenticar com Google")
                    _navigationState.value = NavigationState.NavigateToErrorAccount
                    return@launch
                }

                _loginState.value = GoogleLoginState.Success

                // Consulta o estado real de cadastro via edge function (fonte da verdade no servidor)
                val gate = try {
                    client.functions.invoke("registration-gate")
                        .body<RegistrationGateResponse>()
                } catch (e: Exception) {
                    Log.e("GoogleLoginVM", "Erro ao consultar registration-gate: ${e.message}")
                    _loginState.value =
                        GoogleLoginState.Error("Erro ao verificar cadastro: ${e.message}")
                    _navigationState.value = NavigationState.NavigateToErrorAccount
                    return@launch
                }

                Log.d(
                    "GoogleLoginVM",
                    "Gate - personal_complete: ${gate.personal_complete}, professional_complete: ${gate.professional_complete}"
                )

                // Persiste localmente para agilizar a navegação na próxima abertura
                authSessionStore.setLoggedIn(true)
                authSessionStore.setUserId(AuthManager.getCurrentUserId() ?: "")
                authSessionStore.updateRegistration(
                    personalComplete = gate.personal_complete,
                    professionalComplete = gate.professional_complete
                )

                // Sincronização única pós-login: empresa, declarações e recibos
                syncRunner.runOnceAfterLogin()

                handleUserNavigation(gate)

            } catch (e: Exception) {
                Log.e("GoogleLoginVM", "Erro ao autenticar no Supabase: ${e.message}")
                _loginState.value = GoogleLoginState.Error("Erro ao autenticar: ${e.message}")
                _navigationState.value = NavigationState.NavigateToErrorAccount
            }
        }
    }

    private fun handleUserNavigation(gate: RegistrationGateResponse) {
        // O usuário não pode continuar com campos de registro incompletos:
        // os campos pessoais/profissionais determinam a navegação.
        when {
            !gate.personal_complete -> {
                Log.d("GoogleLoginVM", "Cadastro pessoal incompleto -> tela de finalização")
                _navigationState.value = NavigationState.NavigateToFinalizeAccount
            }

            !gate.professional_complete -> {
                Log.d("GoogleLoginVM", "Cadastro profissional pendente -> upload de arquivo")
                _navigationState.value = NavigationState.NavigateToUploadFile
            }

            else -> {
                Log.d("GoogleLoginVM", "Onboarding completo -> home")
                _navigationState.value = NavigationState.NavigateToHome
            }
        }
    }

    fun resetNavigationState() {
        _navigationState.value = NavigationState.Idle
    }

    fun clearError() {
        _loginState.value = GoogleLoginState.Idle
    }
}
