package com.w_via.features.sync

import com.w_via.features.declaration.DeclaracaoObito
import com.w_via.features.declaration.data.DeclaracaoDao
import com.w_via.features.declaration.data.DeclaracaoEntity
import com.w_via.features.declaration.data.DeclaracaoMapper
import com.w_via.features.declaration.data.DeclaracaoRepository
import com.w_via.features.declaration.data.UserIdProvider
import com.w_via.features.declaration.state.DeclaracaoObitoFormState
import com.w_via.features.declaration.toDeclaracaoObito
import com.w_via.features.receipt.Recibo
import com.w_via.features.receipt.data.ReciboDao
import com.w_via.features.receipt.data.ReciboEntity
import com.w_via.features.receipt.data.ReciboMapper
import com.w_via.features.receipt.data.ReciboRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncManagerTest {

    private class FakeDeclaracaoDao(
        var entities: MutableList<DeclaracaoEntity> = mutableListOf()
    ) : DeclaracaoDao {
        override suspend fun insert(declaracao: DeclaracaoEntity): Long {
            entities += declaracao
            return declaracao.id
        }

        override suspend fun update(declaracao: DeclaracaoEntity) {
            entities.replaceAll { if (it.id == declaracao.id) declaracao else it }
        }

        override fun observeByUser(userId: String): Flow<List<DeclaracaoEntity>> =
            flowOf(entities.filter { it.userId == userId && !it.isDeleted })

        override suspend fun getByUser(userId: String): List<DeclaracaoEntity> =
            entities.filter { it.userId == userId && !it.isDeleted }

        override suspend fun getById(id: Long): DeclaracaoEntity? =
            entities.firstOrNull { it.id == id }

        override suspend fun getByUuid(uuid: String): DeclaracaoEntity? =
            entities.firstOrNull { it.uuid == uuid }

        override suspend fun getUnsynced(userId: String): List<DeclaracaoEntity> =
            entities.filter { it.userId == userId && !it.synced }

        override suspend fun markSynced(id: Long) {
            entities.replaceAll { if (it.id == id) it.copy(synced = true) else it }
        }

        override suspend fun softDelete(id: Long, updatedAt: Long) {
            entities.replaceAll {
                if (it.id == id) it.copy(isDeleted = true, synced = false, updatedAt = updatedAt) else it
            }
        }

        override suspend fun deleteById(id: Long) {
            entities.removeIf { it.id == id }
        }

        override suspend fun softDeleteByIds(ids: List<Long>, updatedAt: Long) {
            entities.replaceAll {
                if (it.id in ids) it.copy(isDeleted = true, synced = false, updatedAt = updatedAt) else it
            }
        }

        override suspend fun deleteAllForUser(userId: String) {
            entities.removeIf { it.userId == userId }
        }

        override suspend fun countForUser(userId: String): Int =
            entities.count { it.userId == userId && !it.isDeleted }
    }

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

    private class FakeDeclaracaoRemote(
        var outcome: SyncOutcome = SyncOutcome.Success,
        var pulled: List<DeclaracaoEntity> = emptyList()
    ) : DeclaracaoRemoteSync {
        var pushedCount = 0

        override suspend fun pushDeclaracoes(declaracoes: List<DeclaracaoEntity>): SyncOutcome {
            pushedCount = declaracoes.size
            return outcome
        }

        override suspend fun pullDeclaracoes(): List<DeclaracaoEntity> = pulled
    }

    private class FakeReciboRemote(
        var outcome: SyncOutcome = SyncOutcome.Success,
        var pulled: List<ReciboEntity> = emptyList()
    ) : ReciboRemoteSync {
        var pushedCount = 0

        override suspend fun pushRecibos(recibos: List<ReciboEntity>): SyncOutcome {
            pushedCount = recibos.size
            return outcome
        }

        override suspend fun pullRecibos(): List<ReciboEntity> = pulled
    }

    private class FakeCompanyProfileSync(
        var outcome: SyncOutcome = SyncOutcome.Success
    ) : CompanyProfileSync {
        override suspend fun sync(): SyncOutcome = outcome
    }

    private val userIdProvider = UserIdProvider { "user-1" }

    private fun declEntity(id: Long, synced: Boolean = false): DeclaracaoEntity =
        DeclaracaoMapper.toEntity(
            userId = "user-1",
            declaracao = DeclaracaoObitoFormState().copy(nome = "Fulano de Tal").toDeclaracaoObito(),
            now = id,
            id = id,
            synced = synced
        )

    private fun reciboEntity(id: Long, synced: Boolean = false): ReciboEntity =
        ReciboMapper.toEntity(
            userId = "user-1",
            recibo = Recibo(nomeContratante = "Funerária", total_pago = "100.0"),
            now = id,
            id = id,
            synced = synced
        )

    private fun manager(
        declDao: FakeDeclaracaoDao,
        reciboDao: FakeReciboDao,
        declRemote: DeclaracaoRemoteSync,
        reciboRemote: ReciboRemoteSync,
        company: CompanyProfileSync = FakeCompanyProfileSync()
    ): SyncManager = SyncManager(
        DeclaracaoRepository(declDao, userIdProvider),
        ReciboRepository(reciboDao, userIdProvider),
        declRemote,
        reciboRemote,
        company
    )

    @Test
    fun `sem dados nao sincronizados retorna sucesso sem chamar o remoto`() = runBlocking {
        val declDao = FakeDeclaracaoDao(mutableListOf(declEntity(1, synced = true)))
        val reciboDao = FakeReciboDao(mutableListOf(reciboEntity(1, synced = true)))
        val declRemote = FakeDeclaracaoRemote()
        val reciboRemote = FakeReciboRemote()

        val result = manager(declDao, reciboDao, declRemote, reciboRemote).syncAll()

        assertTrue(result.isSuccess)
        assertEquals(0, declRemote.pushedCount)
        assertEquals(0, reciboRemote.pushedCount)
    }

    @Test
    fun `push com sucesso marca os dados como sincronizados`() = runBlocking {
        val declDao = FakeDeclaracaoDao(mutableListOf(declEntity(1), declEntity(2)))
        val reciboDao = FakeReciboDao(mutableListOf(reciboEntity(1)))

        val result = manager(
            declDao,
            reciboDao,
            FakeDeclaracaoRemote(),
            FakeReciboRemote()
        ).syncAll()

        assertTrue(result.isSuccess)
        assertTrue(declDao.entities.all { it.synced })
        assertTrue(reciboDao.entities.all { it.synced })
    }

    @Test
    fun `falha no push nao marca como sincronizado`() = runBlocking {
        val declDao = FakeDeclaracaoDao(mutableListOf(declEntity(1)))
        val reciboDao = FakeReciboDao(mutableListOf())

        val result = manager(
            declDao,
            reciboDao,
            FakeDeclaracaoRemote(SyncOutcome.Failure("erro de rede")),
            FakeReciboRemote()
        ).syncAll()

        assertTrue(result is SyncOutcome.Failure)
        assertFalse(declDao.entities.first().synced)
    }

    @Test
    fun `sync combina o resultado das duas fontes`() = runBlocking {
        val declDao = FakeDeclaracaoDao(mutableListOf(declEntity(1)))
        val reciboDao = FakeReciboDao(mutableListOf(reciboEntity(1)))

        val result = manager(
            declDao,
            reciboDao,
            FakeDeclaracaoRemote(SyncOutcome.Failure("falha declaração")),
            FakeReciboRemote()
        ).syncAll()

        assertTrue(result is SyncOutcome.Failure)
        assertTrue(reciboDao.entities.first().synced)
        assertFalse(declDao.entities.first().synced)
    }
}
