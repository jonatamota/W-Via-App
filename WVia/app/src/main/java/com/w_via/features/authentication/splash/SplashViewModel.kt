package com.w_via.features.authentication.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w_via.features.authentication.AuthManager
import com.w_via.features.authentication.AuthSessionStore
import com.w_via.features.routes.Routes
import com.w_via.features.sync.SyncRunner
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object Loading : SplashDestination()
    data class Navigate(val route: String) : SplashDestination()
}

class SplashViewModel(
    private val authSessionStore: AuthSessionStore,
    private val syncRunner: SyncRunner
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        decideDestination()
    }

    /** Decisão inicial de navegação baseada no estado local + sessão do Supabase. */
    private fun decideDestination() {
        viewModelScope.launch {
            // Tempo mínimo de exibição da splash para evitar "piscar"
            delay(900)

            // Carrega a sessão persistida em disco (não depende de rede).
            try {
                AuthManager.getClient().auth.awaitInitialization()
            } catch (e: Exception) {
                Log.w("SplashViewModel", "Erro ao aguardar inicialização do auth: ${e.message}")
            }

            val localState = authSessionStore.currentState()
            val hasValidSession = AuthManager.isUserLoggedIn()

            // Grava o token (uuid) localmente quando há sessão válida, liberando
            // o uso offline nas próximas aberturas.
            if (hasValidSession) {
                authSessionStore.setUserId(AuthManager.getCurrentUserId() ?: "")
            }

            // Offline-first: se o estado local indicar login, prossegue mesmo sem
            // sessão remota disponível (a splash não depende da rede).
            if (!localState.loggedIn && !hasValidSession) {
                val route = Routes.TELA_START
                navigate(route)
                return@launch
            }

            // Sincronização única pós-login. Falhas de rede não bloqueiam a navegação.
            runCatching { syncRunner.runOnceAfterLogin() }

            when {
                !localState.personalComplete -> navigate(Routes.FINALIZAR_CONTA)
                !localState.professionalComplete -> navigate(Routes.UPLOAD_FILE)
                else -> navigate(Routes.HOME)
            }
        }
    }

    private fun navigate(route: String) {
        Log.d("SplashViewModel", "Navegando para: $route")
        _destination.value = SplashDestination.Navigate(route)
    }
}