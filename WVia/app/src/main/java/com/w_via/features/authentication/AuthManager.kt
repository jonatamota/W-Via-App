package com.w_via.features.authentication

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Gerenciador de autenticação baseado em Supabase.
 * Substitui o AuthManager anterior que usava Firebase Auth + Firestore.
 */
object AuthManager : KoinComponent {

    private val supabase: SupabaseClient by inject()

    /** Retorna as informações do usuário autenticado, ou null se não houver sessão. */
    fun getCurrentUser(): UserInfo? = supabase.auth.currentUserOrNull()

    /** Retorna o ID (UUID) do usuário autenticado, ou null se não houver sessão. */
    fun getCurrentUserId(): String? = getCurrentUser()?.id

    /** Retorna o e-mail do usuário autenticado, ou null. */
    fun getCurrentUserEmail(): String? = getCurrentUser()?.email

    /** Retorna true se há um usuário autenticado com sessão ativa. */
    fun isUserLoggedIn(): Boolean = getCurrentUser() != null

    /** Encerra a sessão do usuário. */
    suspend fun signOut() {
        supabase.auth.signOut()
    }

    /** Retorna o cliente Supabase (para uso nos módulos de autenticação). */
    fun getClient(): SupabaseClient = supabase
}
