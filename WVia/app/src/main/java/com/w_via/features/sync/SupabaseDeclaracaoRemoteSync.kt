package com.w_via.features.sync

import com.w_via.features.authentication.AuthManager
import com.w_via.features.authentication.data.RegistrationRepository
import com.w_via.features.declaration.data.DeclaracaoEntity
import com.w_via.features.declaration.data.DeclaracaoMapper
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Sincronização das declarações com o Supabase via Postgrest + RLS.
 *
 * - push: upsert com `onConflict = uuid` (cria/atualiza; tombstone de soft
 *   delete é propagado como `is_deleted = true`).
 * - pull: select das linhas do usuário e conversão para entidades locais,
 *   prontas para o merge do [SyncManager].
 */
class SupabaseDeclaracaoRemoteSync(
    private val registrationRepository: RegistrationRepository
) : DeclaracaoRemoteSync {

    private val client get() = AuthManager.getClient()

    override suspend fun pushDeclaracoes(declaracoes: List<DeclaracaoEntity>): SyncOutcome {
        if (declaracoes.isEmpty()) return SyncOutcome.Success
        return try {
            val userId = registrationRepository.resolveUserId()
            withContext(Dispatchers.IO) {
                client.postgrest["declaracoes"].upsert(
                    buildJsonArray {
                        declaracoes.forEach { d ->
                            add(
                                buildJsonObject {
                                    put("uuid", d.uuid)
                                    put("user_id", userId)
                                    put("nome", d.nome)
                                    put("data_obito", d.dataObito)
                                    put("local_obito", d.localObito)
                                    put("payload_json", d.payloadJson)
                                    put("created_at", d.createdAt)
                                    put("updated_at", d.updatedAt)
                                    put("is_deleted", d.isDeleted)
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
            SyncOutcome.Failure(e.message ?: "Erro ao enviar declarações")
        }
    }

    override suspend fun pullDeclaracoes(): List<DeclaracaoEntity> {
        val userId = registrationRepository.resolveUserId()
        val authUserId = AuthManager.getCurrentUserId() ?: return emptyList()
        val rows = withContext(Dispatchers.IO) {
            client.postgrest["declaracoes"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<DeclaracaoRemoteRow>()
        }
        return rows.map { row ->
            val declaracao = DeclaracaoMapper.fromJson(row.payload_json)
            DeclaracaoMapper.toEntity(
                userId = authUserId,
                declaracao = declaracao,
                now = row.updated_at,
                id = 0,
                synced = true,
                isDeleted = row.is_deleted
            )
        }
    }
}
