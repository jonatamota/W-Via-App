package com.w_via.features.sync

import com.w_via.features.authentication.AuthManager
import com.w_via.features.authentication.data.RegistrationRepository
import com.w_via.features.receipt.data.ReciboEntity
import com.w_via.features.receipt.data.ReciboMapper
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Sincronização dos recibos com o Supabase via Postgrest + RLS.
 *
 * - push: upsert com `onConflict = uuid` (cria/atualiza; tombstone de soft
 *   delete é propagado como `is_deleted = true`).
 * - pull: select das linhas do usuário e conversão para entidades locais,
 *   prontas para o merge do [SyncManager].
 */
class SupabaseReciboRemoteSync(
    private val registrationRepository: RegistrationRepository
) : ReciboRemoteSync {

    private val client get() = AuthManager.getClient()

    override suspend fun pushRecibos(recibos: List<ReciboEntity>): SyncOutcome {
        if (recibos.isEmpty()) return SyncOutcome.Success
        return try {
            val userId = registrationRepository.resolveUserId()
            withContext(Dispatchers.IO) {
                client.postgrest["recibos"].upsert(
                    buildJsonArray {
                        recibos.forEach { r ->
                            add(
                                buildJsonObject {
                                    put("uuid", r.uuid)
                                    put("user_id", userId)
                                    put("nome_contratante", r.nomeContratante)
                                    put("data_pagamento", r.dataPagamento)
                                    put("total_pago", r.totalPago)
                                    put("payload_json", r.payloadJson)
                                    put("created_at", r.createdAt)
                                    put("updated_at", r.updatedAt)
                                    put("is_deleted", r.isDeleted)
                                }
                            )
                        }
                    }
                ) {
                    onConflict = "uuid"
                }
            }
            SyncOutcome.Success
        } catch (e: Exception) {
            SyncOutcome.Failure(e.message ?: "Erro ao enviar recibos")
        }
    }

    override suspend fun pullRecibos(): List<ReciboEntity> {
        val userId = registrationRepository.resolveUserId()
        val authUserId = AuthManager.getCurrentUserId() ?: return emptyList()
        val rows = withContext(Dispatchers.IO) {
            client.postgrest["recibos"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<ReciboRemoteRow>()
        }
        return rows.map { row ->
            val recibo = ReciboMapper.fromJson(row.payload_json)
            ReciboMapper.toEntity(
                userId = authUserId,
                recibo = recibo,
                now = row.updated_at,
                id = 0,
                synced = true,
                isDeleted = row.is_deleted
            )
        }
    }
}
