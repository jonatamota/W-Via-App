package com.w_via.features.sync

import com.w_via.features.declaration.data.DeclaracaoEntity

/**
 * Contrato da sincronização remota das Declarações de Óbito com o Supabase.
 *
 * O push envia as entidades locais (incluindo tombstones de soft delete) e o
 * pull devolve as linhas do servidor para o merge local (LWW por updatedAt).
 */
interface DeclaracaoRemoteSync {

    /** Envia as declarações locais (novas/editadas/excluídas) para o servidor. */
    suspend fun pushDeclaracoes(declaracoes: List<DeclaracaoEntity>): SyncOutcome

    /** Busca do servidor as declarações do usuário (para merge local). */
    suspend fun pullDeclaracoes(): List<DeclaracaoEntity>
}
