package com.w_via.features.company.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Dados profissionais da funerária (logo, nome, CNPJ etc.), cacheados
 * localmente após o pull do Supabase.
 *
 * Uma única linha por usuário ([userId]) — sempre gravada via upsert.
 */
@Entity(tableName = "empresa")
data class EmpresaEntity(
    @PrimaryKey
    val userId: String,
    val cnpj: String = "",
    val nomeFantasia: String = "",
    val telefone: String = "",
    val email: String = "",
    val endereco: String = "",
    val cidade: String = "",
    val uf: String = "",
    val logoPath: String? = null,
    val updatedAt: Long = 0L
)
