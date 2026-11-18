package com.w_via.features.declaration.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO das declarações persistidas. Todas as operações são escopadas por [userId]
 * (dados de um usuário não vazam para outro).
 *
 * Suporta soft delete: [isDeleted] marca a linha como excluída sem removê-la,
 * permitindo que a exclusão seja propagada na sincronização.
 */
@Dao
interface DeclaracaoDao {

    /** Insere uma declaração e retorna o id gerado. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(declaracao: DeclaracaoEntity): Long

    /** Atualiza uma declaração existente (usada no merge do pull). */
    @Update
    suspend fun update(declaracao: DeclaracaoEntity)

    /** Observa as declarações do usuário (ativas), ordenadas pela mais recente. */
    @Query("SELECT * FROM declaracoes WHERE userId = :userId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeByUser(userId: String): Flow<List<DeclaracaoEntity>>

    /** Lista as declarações do usuário (chamada única). */
    @Query("SELECT * FROM declaracoes WHERE userId = :userId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getByUser(userId: String): List<DeclaracaoEntity>

    /** Busca uma declaração pelo id (sem escopo de usuário — id é único). */
    @Query("SELECT * FROM declaracoes WHERE id = :id")
    suspend fun getById(id: Long): DeclaracaoEntity?

    /** Busca uma declaração pelo uuid estável (para o merge da sincronização). */
    @Query("SELECT * FROM declaracoes WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): DeclaracaoEntity?

    /** Declarações ainda não sincronizadas com o servidor (inclui tombstone de soft delete). */
    @Query("SELECT * FROM declaracoes WHERE userId = :userId AND synced = 0 ORDER BY updatedAt ASC")
    suspend fun getUnsynced(userId: String): List<DeclaracaoEntity>

    /** Marca uma declaração como sincronizada. */
    @Query("UPDATE declaracoes SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    /** Soft delete: marca a declaração como excluída e pendente de sincronização. */
    @Query("UPDATE declaracoes SET isDeleted = 1, synced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: Long, updatedAt: Long)

    /** Soft delete em lote: marca várias declarações como excluídas e pendentes de sincronização. */
    @Query("UPDATE declaracoes SET isDeleted = 1, synced = 0, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun softDeleteByIds(ids: List<Long>, updatedAt: Long)

    /** Remove definitivamente uma declaração. */
    @Query("DELETE FROM declaracoes WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Remove todas as declarações de um usuário. */
    @Query("DELETE FROM declaracoes WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    /** Quantidade de declarações ativas do usuário. */
    @Query("SELECT COUNT(*) FROM declaracoes WHERE userId = :userId AND isDeleted = 0")
    suspend fun countForUser(userId: String): Int
}
