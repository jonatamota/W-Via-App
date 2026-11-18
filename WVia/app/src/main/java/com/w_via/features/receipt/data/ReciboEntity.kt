package com.w_via.features.receipt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Recibo persistido localmente (Room), offline-first, no mesmo padrão da
 * Declaração de Óbito: o modelo completo vai em [payloadJson] e as demais
 * colunas são metadados para listagem e futura sincronização.
 */
@Entity(tableName = "recibos")
data class ReciboEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val uuid: String,
    val nomeContratante: String,
    val dataPagamento: String,
    val totalPago: String,
    val payloadJson: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val synced: Boolean = false,
    val isDeleted: Boolean = false,
    val isObsolete: Boolean = false
)
