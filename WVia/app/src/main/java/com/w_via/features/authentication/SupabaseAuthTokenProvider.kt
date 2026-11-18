package com.w_via.features.authentication

import io.github.jan.supabase.auth.auth

/**
 * Implementação de AuthTokenProvider usando Supabase Auth.
 * Retorna o access token JWT da sessão ativa do Supabase.
 */
class SupabaseAuthTokenProvider : AuthTokenProvider {
    override suspend fun getAccessToken(): String? {
        return AuthManager.getClient().auth.currentSessionOrNull()?.accessToken
    }
}