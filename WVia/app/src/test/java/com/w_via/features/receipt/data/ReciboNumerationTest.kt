package com.w_via.features.receipt.data

import com.w_via.features.declaration.data.UserIdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Garante que a numeração progressiva dos recibos nunca reutiliza números,
 * mesmo quando recibos são excluídos (soft delete mantém a linha contabilizada).
 */
class ReciboNumerationTest {

    private class FakeReciboDao(
        var entities: MutableList<ReciboEntity> = mutableListOf()
    ) : ReciboDao {
        override suspend fun insert(recibo: ReciboEntity): Long {
            entities += recibo
            return recibo.id
        }

        override suspend fun update(recibo: ReciboEntity) {
            entities.replaceAll { if (it.id == recibo.id) recibo else it }
        }

        override fun observeByUser(userId: String): Flow<List<ReciboEntity>> =
            flowOf(entities.filter { it.userId == userId && !it.isDeleted })

        override suspend fun getById(id: Long): ReciboEntity? =
            entities.firstOrNull { it.id == id }

        override suspend fun getByUuid(uuid: String): ReciboEntity? =
            entities.firstOrNull { it.uuid == uuid }

        override suspend fun getUnsynced(userId: String): List<ReciboEntity> =
            entities.filter { it.userId == userId && !it.synced }

        override suspend fun markSynced(id: Long) {
            entities.replaceAll { if (it.id == id) it.copy(synced = true) else it }
        }

        override suspend fun markObsolete(id: Long, updatedAt: Long) {
            entities.replaceAll {
                if (it.id == id) it.copy(isObsolete = true, updatedAt = updatedAt) else it
            }
        }

        override suspend fun softDelete(id: Long, updatedAt: Long) {
            entities.replaceAll {
                if (it.id == id) it.copy(isDeleted = true, synced = false, updatedAt = updatedAt) else it
            }
        }

        override suspend fun softDeleteByIds(ids: List<Long>, updatedAt: Long) {
            entities.replaceAll {
                if (it.id in ids) it.copy(isDeleted = true, synced = false, updatedAt = updatedAt) else it
            }
        }

        override suspend fun deleteById(id: Long) {
            entities.removeIf { it.id == id }
        }

        override suspend fun countForUser(userId: String): Int =
            entities.count { it.userId == userId && !it.isDeleted }

        override suspend fun countAllForUser(userId: String): Int =
            entities.count { it.userId == userId }
    }

    private val userIdProvider = UserIdProvider { "user-1" }

    private fun reciboEntity(id: Long, isDeleted: Boolean = false): ReciboEntity =
        ReciboEntity(
            id = id,
            userId = "user-1",
            uuid = "uuid-$id",
            nomeContratante = "Funerária",
            dataPagamento = "01/01/2026",
            totalPago = "R$ 100,00",
            payloadJson = "{}",
            createdAt = id,
            updatedAt = id,
            isDeleted = isDeleted
        )

    @Test
    fun `nextNumero conta tambem os recibos excluidos`() = runBlocking {
        val dao = FakeReciboDao(
            mutableListOf(reciboEntity(1), reciboEntity(2, isDeleted = true))
        )
        val repository = ReciboRepository(dao, userIdProvider)

        assertEquals(3, repository.nextNumero())
    }

    @Test
    fun `nextNumero nao reutiliza numero de recibo excluido`() = runBlocking {
        val dao = FakeReciboDao(mutableListOf(reciboEntity(1)))
        val repository = ReciboRepository(dao, userIdProvider)

        // Antes da exclusão o próximo número emitido seria 2.
        assertEquals(2, repository.nextNumero())

        // Exclui o recibo 1: o soft delete mantém a linha, então o número já
        // emitido (0001) não é reutilizado e o próximo segue sendo 2.
        repository.delete(1)
        assertEquals(2, repository.nextNumero())

        // Cria um novo recibo: a contagem continua progressiva (não volta para 1).
        dao.entities += reciboEntity(2)
        assertEquals(3, repository.nextNumero())
    }

    @Test
    fun `nextNumero avanca a cada recibo salvo`() = runBlocking {
        val dao = FakeReciboDao(mutableListOf())
        val repository = ReciboRepository(dao, userIdProvider)

        assertEquals(1, repository.nextNumero())
        dao.entities += reciboEntity(1)
        assertEquals(2, repository.nextNumero())
        dao.entities += reciboEntity(2)
        assertEquals(3, repository.nextNumero())
    }
}
