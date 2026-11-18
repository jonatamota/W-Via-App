package com.w_via.features.receipt.data

import com.w_via.features.declaration.data.UserIdProvider
import com.w_via.features.receipt.Recibo
import kotlinx.coroutines.flow.Flow

/**
 * Ponto de acesso da camada de dados dos recibos. Encapsula o DAO e o usuário
 * atual, expondo operações em termos do modelo de domínio (offline-first).
 */
class ReciboRepository(
    private val dao: ReciboDao,
    private val userIdProvider: UserIdProvider
) {

    private fun currentUserId(): String = userIdProvider.currentUserId()

    /** Salva um recibo e retorna o id gerado. */
    suspend fun save(recibo: Recibo): Long =
        dao.insert(ReciboMapper.toEntity(currentUserId(), recibo))

    /** Observa os recibos salvos do usuário (para a lista). */
    fun observeSaved(): Flow<List<ReciboEntity>> = dao.observeByUser(currentUserId())

    /** Busca um recibo pelo id, já como modelo de domínio. */
    suspend fun getById(id: Long): Recibo? = dao.getById(id)?.let(ReciboMapper::fromEntity)

    /** Soft delete: marca o recibo como excluído e pendente de sincronização. */
    suspend fun delete(id: Long) = dao.softDelete(id, System.currentTimeMillis())

    /** Soft delete em lote: marca vários recibos como excluídos e pendentes de sincronização. */
    suspend fun deleteByIds(ids: List<Long>) {
        if (ids.isEmpty()) return
        dao.softDeleteByIds(ids, System.currentTimeMillis())
    }

    /** Marca um recibo como desatualizado (refeito a partir dele). */
    suspend fun markObsolete(id: Long) = dao.markObsolete(id, System.currentTimeMillis())

    /** Quantidade de recibos ativos do usuário. */
    suspend fun count(): Int = dao.countForUser(currentUserId())

    /**
     * Próximo número de recibo: sequência crescente por usuário, contando também
     * os recibos excluídos para não reutilizar numeração já emitida.
     */
    suspend fun nextNumero(): Int = dao.countAllForUser(currentUserId()) + 1

    /** Recibos ainda não sincronizados, como entidades (para o SyncManager). */
    suspend fun getUnsyncedEntities(): List<ReciboEntity> =
        dao.getUnsynced(currentUserId())

    /** Marca um recibo como sincronizado. */
    suspend fun markSynced(id: Long) = dao.markSynced(id)

    /**
     * Aplica no banco local as linhas vindas do servidor (merge LWW por
     * [ReciboEntity.updatedAt]). Linhas novas são inseridas; as mais antigas que
     * a local são ignoradas; tombstones remotos viram soft delete local.
     */
    suspend fun mergeRemote(remote: List<ReciboEntity>) {
        remote.forEach { remoteEntity ->
            val local = dao.getByUuid(remoteEntity.uuid) ?: run {
                if (!remoteEntity.isDeleted) {
                    dao.insert(
                        remoteEntity.copy(
                            id = 0,
                            userId = currentUserId(),
                            synced = true,
                            isDeleted = false
                        )
                    )
                }
                return@forEach
            }
            if (remoteEntity.updatedAt > local.updatedAt) {
                dao.update(
                    local.copy(
                        nomeContratante = remoteEntity.nomeContratante,
                        dataPagamento = remoteEntity.dataPagamento,
                        totalPago = remoteEntity.totalPago,
                        payloadJson = remoteEntity.payloadJson,
                        updatedAt = remoteEntity.updatedAt,
                        synced = true,
                        isDeleted = remoteEntity.isDeleted
                    )
                )
            }
        }
    }
}
