package com.w_via.features.sync

import kotlinx.serialization.Serializable

/**
 * Linha da tabela remota `declaracoes` (Supabase). Espelha o
 * [com.w_via.features.declaration.data.DeclaracaoEntity] local para o push/pull.
 */
@Serializable
data class DeclaracaoRemoteRow(
    val uuid: String = "",
    val user_id: Long = 0,
    val nome: String = "",
    val data_obito: String = "",
    val local_obito: String = "",
    val payload_json: String = "",
    val created_at: Long = 0,
    val updated_at: Long = 0,
    val is_deleted: Boolean = false
)

/**
 * Linha da tabela remota `recibos` (Supabase). Espelha o
 * [com.w_via.features.receipt.data.ReciboEntity] local para o push/pull.
 */
@Serializable
data class ReciboRemoteRow(
    val uuid: String = "",
    val user_id: Long = 0,
    val nome_contratante: String = "",
    val data_pagamento: String = "",
    val total_pago: String = "",
    val payload_json: String = "",
    val created_at: Long = 0,
    val updated_at: Long = 0,
    val is_deleted: Boolean = false
)
