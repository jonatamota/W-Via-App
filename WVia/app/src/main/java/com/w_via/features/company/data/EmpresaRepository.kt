package com.w_via.features.company.data

import com.w_via.features.declaration.data.UserIdProvider
import kotlinx.coroutines.flow.Flow

/**
 * Acesso aos dados profissionais da empresa (cache local do pull).
 */
class EmpresaRepository(
    private val dao: EmpresaDao,
    private val userIdProvider: UserIdProvider
) {

    private fun currentUserId(): String = userIdProvider.currentUserId()

    /** Grava (upsert) os dados da empresa para o usuário atual. */
    suspend fun save(empresa: EmpresaEntity) = dao.upsert(empresa)

    /** Observa os dados da empresa do usuário atual. */
    fun observe(): Flow<EmpresaEntity?> = dao.observeByUser(currentUserId())

    /** Busca os dados da empresa do usuário atual (chamada única). */
    suspend fun get(): EmpresaEntity? = dao.getByUser(currentUserId())
}
