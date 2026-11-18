package com.w_via.features.declaration.data

import com.w_via.features.declaration.DeclaracaoObito
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Ponto de acesso da camada de dados das declarações. Encapsula o DAO e o usuário
 * atual, expondo operações em termos do modelo de domínio.
 *
 * Offline-first: salva localmente e mantém o flag [DeclaracaoEntity.synced] para
 * a sincronização com o Supabase.
 */
class DeclaracaoRepository(
    private val dao: DeclaracaoDao,
    private val userIdProvider: UserIdProvider
) {

    private fun currentUserId(): String = userIdProvider.currentUserId()

    /** Salva uma declaração e retorna o id gerado. */
    suspend fun save(declaracao: DeclaracaoObito): Long =
        dao.insert(DeclaracaoMapper.toEntity(currentUserId(), declaracao))

    /** Observa as declarações salvas do usuário (para a lista). */
    fun observeSaved(): Flow<List<DeclaracaoEntity>> = dao.observeByUser(currentUserId())

    /** Observa as declarações salvas já como modelo de domínio. */
    fun observeDeclaracoes(): Flow<List<DeclaracaoObito>> =
        observeSaved().map { entities -> entities.map(DeclaracaoMapper::fromEntity) }

    /** Busca uma declaração pelo id. */
    suspend fun getById(id: Long): DeclaracaoObito? =
        dao.getById(id)?.let(DeclaracaoMapper::fromEntity)

    /** Soft delete: marca a declaração como excluída e pendente de sincronização. */
    suspend fun delete(id: Long) = dao.softDelete(id, System.currentTimeMillis())

    /** Soft delete em lote: marca várias declarações como excluídas e pendentes de sincronização. */
    suspend fun deleteByIds(ids: List<Long>) {
        if (ids.isEmpty()) return
        dao.softDeleteByIds(ids, System.currentTimeMillis())
    }

    /** Quantidade de declarações ativas do usuário. */
    suspend fun count(): Int = dao.countForUser(currentUserId())

    /** Declarações ainda não sincronizadas, como entidades (para o SyncManager). */
    suspend fun getUnsyncedEntities(): List<DeclaracaoEntity> =
        dao.getUnsynced(currentUserId())

    /** Marca uma declaração como sincronizada. */
    suspend fun markSynced(id: Long) = dao.markSynced(id)

    /**
     * Aplica no banco local as linhas vindas do servidor (merge LWW por
     * [DeclaracaoEntity.updatedAt]). Linhas novas são inseridas; as mais antigas
     * que a local são ignoradas; tombstones remotos viram soft delete local.
     */
    suspend fun mergeRemote(remote: List<DeclaracaoEntity>) {
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
                        nome = remoteEntity.nome,
                        dataObito = remoteEntity.dataObito,
                        localObito = remoteEntity.localObito,
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
