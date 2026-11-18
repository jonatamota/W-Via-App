package com.w_via.features.receipt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO dos recibos persistidos. Todas as operações são escopadas por [userId]
 * (dados de um usuário não vazam para outro).
 *
 * Suporta soft delete: [isDeleted] marca a linha como excluída sem removê-la,
 * permitindo que a exclusão seja propagada na sincronização.
 */
@Dao
interface ReciboDao {

    /** Insere um recibo e retorna o id gerado. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recibo: ReciboEntity): Long

    /** Atualiza um recibo existente (usada no merge do pull). */
    @Update
    suspend fun update(recibo: ReciboEntity)

    /** Observa os recibos do usuário (ativos), ordenados pelos mais recentes. */
    @Query("SELECT * FROM recibos WHERE userId = :userId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeByUser(userId: String): Flow<List<ReciboEntity>>

    /** Busca um recibo pelo id (id é único). */
    @Query("SELECT * FROM recibos WHERE id = :id")
    suspend fun getById(id: Long): ReciboEntity?

    /** Busca um recibo pelo uuid estável (para o merge da sincronização). */
    @Query("SELECT * FROM recibos WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): ReciboEntity?

    /** Recibos ainda não sincronizados com o servidor (inclui tombstone de soft delete). */
    @Query("SELECT * FROM recibos WHERE userId = :userId AND synced = 0 ORDER BY updatedAt ASC")
    suspend fun getUnsynced(userId: String): List<ReciboEntity>

    /** Marca um recibo como sincronizado. */
    @Query("UPDATE recibos SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    /** Soft delete: marca o recibo como excluído e pendente de sincronização. */
    @Query("UPDATE recibos SET isDeleted = 1, synced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: Long, updatedAt: Long)

    /** Soft delete em lote: marca vários recibos como excluídos e pendentes de sincronização. */
    @Query("UPDATE recibos SET isDeleted = 1, synced = 0, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun softDeleteByIds(ids: List<Long>, updatedAt: Long)

    /** Marca um recibo como desatualizado (refeito) e pendente de sincronização. */
    @Query("UPDATE recibos SET isObsolete = 1, synced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markObsolete(id: Long, updatedAt: Long)

    /** Remove um recibo definitivamente. */
    @Query("DELETE FROM recibos WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Quantidade de recibos ativos do usuário. */
    @Query("SELECT COUNT(*) FROM recibos WHERE userId = :userId AND isDeleted = 0")
    suspend fun countForUser(userId: String): Int

    /** Quantidade total de recibos já criados pelo usuário (inclui soft delete). */
    @Query("SELECT COUNT(*) FROM recibos WHERE userId = :userId")
    suspend fun countAllForUser(userId: String): Int
}
