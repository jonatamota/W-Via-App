package com.w_via.features.sync

import com.w_via.features.receipt.data.ReciboEntity

/**
 * Contrato da sincronização remota dos Recibos com o Supabase.
 *
 * O push envia as entidades locais (incluindo tombstones de soft delete) e o
 * pull devolve as linhas do servidor para o merge local (LWW por updatedAt).
 */
interface ReciboRemoteSync {

    /** Envia os recibos locais (novos/editados/excluídos) para o servidor. */
    suspend fun pushRecibos(recibos: List<ReciboEntity>): SyncOutcome

    /** Busca do servidor os recibos do usuário (para merge local). */
    suspend fun pullRecibos(): List<ReciboEntity>
}
