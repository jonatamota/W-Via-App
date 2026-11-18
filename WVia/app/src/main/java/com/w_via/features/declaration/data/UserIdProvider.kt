package com.w_via.features.declaration.data

import com.w_via.features.authentication.AuthManager

/**
 * Fonte do id do usuário atual, abstraída para o repositório ser testável
 * sem depender de Supabase/Android.
 */
fun interface UserIdProvider {
    fun currentUserId(): String
}

/** Implementação real: id do usuário autenticado no Supabase (fallback local). */
object SupabaseUserIdProvider : UserIdProvider {
    override fun currentUserId(): String = AuthManager.getCurrentUserId() ?: "local"
}
