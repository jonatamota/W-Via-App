package com.w_via.features.authentication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Estado de autenticação guardado localmente no DataStore.
 *
 * Permite que a SplashScreen decida rapidamente a navegação inicial sem
 * consultar o servidor a cada abertura do app. Os valores de registro
 * espelham o retorno da edge function `registration-gate`.
 *
 * O [userId] (uuid do Supabase) é gravado localmente para permitir o uso do
 * app offline (splash e draft não dependem de sessão remota).
 */
data class AuthState(
    val loggedIn: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val personalComplete: Boolean = false,
    val professionalComplete: Boolean = false,
    val userId: String = ""
) {
    /** Registro totalmente concluído quando acaba o onboarding e ambos os campos estão completos. */
    val registrationComplete: Boolean
        get() = personalComplete && professionalComplete
}

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_session_prefs"
)

private object AuthPrefsKeys {
    val loggedIn = booleanPreferencesKey("logged_in")
    val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    val personalComplete = booleanPreferencesKey("personal_complete")
    val professionalComplete = booleanPreferencesKey("professional_complete")
    val userId = stringPreferencesKey("user_id")
}

/** Repositório local da sessão de autenticação baseado em Preferences DataStore. */
class AuthSessionStore(private val context: Context) {

    /** Fluxo observável do estado de autenticação local. */
    val state: Flow<AuthState> = context.authDataStore.data.map { prefs ->
        AuthState(
            loggedIn = prefs[AuthPrefsKeys.loggedIn] ?: false,
            onboardingCompleted = prefs[AuthPrefsKeys.onboardingCompleted] ?: false,
            personalComplete = prefs[AuthPrefsKeys.personalComplete] ?: false,
            professionalComplete = prefs[AuthPrefsKeys.professionalComplete] ?: false,
            userId = prefs[AuthPrefsKeys.userId] ?: ""
        )
    }

    /** Valor atual (bloqueante) do estado local. */
    suspend fun currentState(): AuthState = state.first()

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.authDataStore.edit {
            it[AuthPrefsKeys.onboardingCompleted] = completed
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.authDataStore.edit {
            it[AuthPrefsKeys.loggedIn] = loggedIn
        }
    }

    /** Grava o uuid do usuário localmente (offline sessions). */
    suspend fun setUserId(userId: String) {
        context.authDataStore.edit {
            if (userId.isBlank()) {
                it.remove(AuthPrefsKeys.userId)
            } else {
                it[AuthPrefsKeys.userId] = userId
            }
        }
    }

    /** Atualiza o estado de registro conforme retorno de `registration-gate`. */
    suspend fun updateRegistration(
        personalComplete: Boolean,
        professionalComplete: Boolean
    ) {
        context.authDataStore.edit {
            it[AuthPrefsKeys.personalComplete] = personalComplete
            it[AuthPrefsKeys.professionalComplete] = professionalComplete
        }
    }

    suspend fun signOutLocal() {
        context.authDataStore.edit {
            it.remove(AuthPrefsKeys.loggedIn)
            it.remove(AuthPrefsKeys.userId)
            it.remove(AuthPrefsKeys.personalComplete)
            it.remove(AuthPrefsKeys.professionalComplete)
        }
    }
}