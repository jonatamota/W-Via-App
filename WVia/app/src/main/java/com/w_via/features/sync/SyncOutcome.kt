package com.w_via.features.sync

/**
 * Resultado de uma operação de sincronização.
 */
sealed interface SyncOutcome {
    val isSuccess: Boolean get() = this is Success

    data object Success : SyncOutcome
    data class Failure(val message: String) : SyncOutcome
}
