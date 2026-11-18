package com.w_via.features.sync

/**
 * Sincronização dos dados profissionais da empresa (logo, nome, CNPJ etc.).
 *
 * Baixa do Supabase e guarda no cache local (Room + LogoStorage). Separada em
 * interface para permitir implementações alternativas e testes.
 */
interface CompanyProfileSync {
    suspend fun sync(): SyncOutcome
}
