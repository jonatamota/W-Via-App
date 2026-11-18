package com.w_via.features.sync

import android.util.Log
import com.w_via.features.declaration.data.DeclaracaoRepository
import com.w_via.features.receipt.data.ReciboRepository

/**
 * Orquestra a sincronização local ↔ Supabase.
 *
 * - push: envia apenas o que ainda não foi sincronizado (getUnsynced, que inclui
 *   tombstones de soft delete) e marca como sincronizado após confirmação remota.
 * - pull: busca as linhas do servidor e aplica o merge local (LWW por updatedAt),
 *   incluindo tombstone remoto -> soft delete local.
 */
class SyncManager(
    private val declaracaoRepository: DeclaracaoRepository,
    private val reciboRepository: ReciboRepository,
    private val declaracaoRemote: DeclaracaoRemoteSync,
    private val reciboRemote: ReciboRemoteSync,
    private val companyProfileSync: CompanyProfileSync
) {

    /** Sincronização completa usada após o login. */
    suspend fun syncAll(): SyncOutcome {
        Log.d("SyncManager", "syncAll iniciado")
        val company = syncCompanyProfile()
        val declaracoes = syncDeclaracoes()
        val recibos = syncRecibos()
        val failures = listOf(company, declaracoes, recibos)
            .mapNotNull { (it as? SyncOutcome.Failure)?.message }
        Log.d("SyncManager", "syncAll resultado: company=$company, declaracoes=$declaracoes, recibos=$recibos")
        return if (failures.isEmpty()) SyncOutcome.Success
        else SyncOutcome.Failure(failures.joinToString("; "))
    }

    /** Sincroniza os dados da empresa (logo, nome, CNPJ etc.). */
    suspend fun syncCompanyProfile(): SyncOutcome = companyProfileSync.sync()

    /** Push + pull das declarações; sucesso apenas se ambas as etapas passarem. */
    suspend fun syncDeclaracoes(): SyncOutcome {
        Log.d("SyncManager", "syncDeclaracoes iniciado")
        val unsynced = declaracaoRepository.getUnsyncedEntities()
        Log.d("SyncManager", "declaracoes unsynced local: ${unsynced.size}")
        if (unsynced.isNotEmpty()) {
            val push = declaracaoRemote.pushDeclaracoes(unsynced)
            if (push is SyncOutcome.Failure) {
                Log.w("SyncManager", "push declaracoes falhou: ${push.message}")
                return push
            }
            unsynced.forEach { declaracaoRepository.markSynced(it.id) }
        }
        return pullAndMergeDeclaracoes()
    }

    /** Push + pull dos recibos; sucesso apenas se ambas as etapas passarem. */
    suspend fun syncRecibos(): SyncOutcome {
        Log.d("SyncManager", "syncRecibos iniciado")
        val unsynced = reciboRepository.getUnsyncedEntities()
        Log.d("SyncManager", "recibos unsynced local: ${unsynced.size}")
        if (unsynced.isNotEmpty()) {
            val push = reciboRemote.pushRecibos(unsynced)
            if (push is SyncOutcome.Failure) {
                Log.w("SyncManager", "push recibos falhou: ${push.message}")
                return push
            }
            unsynced.forEach { reciboRepository.markSynced(it.id) }
        }
        return pullAndMergeRecibos()
    }

    private suspend fun pullAndMergeDeclaracoes(): SyncOutcome = try {
        val remote = declaracaoRemote.pullDeclaracoes()
        Log.d("SyncManager", "pull declaracoes retornou ${remote.size} linhas")
        declaracaoRepository.mergeRemote(remote)
        SyncOutcome.Success
    } catch (e: Exception) {
        Log.e("SyncManager", "pull declaracoes falhou", e)
        SyncOutcome.Failure(e.message ?: "Erro ao baixar declarações")
    }

    private suspend fun pullAndMergeRecibos(): SyncOutcome = try {
        val remote = reciboRemote.pullRecibos()
        Log.d("SyncManager", "pull recibos retornou ${remote.size} linhas")
        reciboRepository.mergeRemote(remote)
        SyncOutcome.Success
    } catch (e: Exception) {
        Log.e("SyncManager", "pull recibos falhou", e)
        SyncOutcome.Failure(e.message ?: "Erro ao baixar recibos")
    }
}
