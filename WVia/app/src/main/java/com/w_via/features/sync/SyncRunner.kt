package com.w_via.features.sync

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Executa a sincronização completa uma única vez após o login.
 *
 * Guarda por [syncLock] para não rodar duas sincronizações simultâneas e
 * notifica os ViewModels via [SyncOutcome.flow] quando o processo termina.
 */
class SyncRunner(
    private val syncManager: SyncManager,
    private val scope: CoroutineScope
) {

    private val syncLock = Mutex()
    private var alreadySynced = false
    private val _outcome = MutableSharedFlow<SyncOutcome>(extraBufferCapacity = 1)
    val outcome: SharedFlow<SyncOutcome> = _outcome

    /** Dispara o sync pós-login se ainda não tiver rodado nesta sessão. */
    fun runOnceAfterLogin() {
        if (alreadySynced) return
        alreadySynced = true
        scope.launch {
            val result = syncLock.withLock { syncManager.syncAll() }
            Log.w("SyncRunner", "Sync result: $result")
            _outcome.emit(result)
        }
    }

    /** Força uma nova sincronização (ex.: após logout + login). */
    fun reset() {
        alreadySynced = false
    }
}
