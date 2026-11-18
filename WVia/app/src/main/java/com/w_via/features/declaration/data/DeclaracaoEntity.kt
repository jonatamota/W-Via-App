package com.w_via.features.declaration.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Declaração de Óbito persistida localmente (Room), offline-first.
 *
 * O modelo completo é armazenado como JSON em [payloadJson] (via [DeclaracaoMapper]);
 * as demais colunas são metadados usados para listagem e sincronização futura.
 *
 * [userId] particiona os dados por usuário: todas as consultas do DAO filtram por
 * esse campo, dando o efeito de "um banco por usuário" sem criar arquivos dinâmicos.
 */
@Entity(tableName = "declaracoes")
data class DeclaracaoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val uuid: String = "",
    val nome: String,
    val dataObito: String,
    val localObito: String,
    val payloadJson: String,
    val createdAt: Long,
    val updatedAt: Long,
    val synced: Boolean = false,
    val isDeleted: Boolean = false
)
