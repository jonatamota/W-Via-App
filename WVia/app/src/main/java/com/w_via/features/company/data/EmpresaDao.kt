package com.w_via.features.company.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO dos dados profissionais da empresa. Uma única linha por usuário,
 * gravada via upsert durante a sincronização.
 */
@Dao
interface EmpresaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(empresa: EmpresaEntity)

    @Query("SELECT * FROM empresa WHERE userId = :userId")
    fun observeByUser(userId: String): Flow<EmpresaEntity?>

    @Query("SELECT * FROM empresa WHERE userId = :userId")
    suspend fun getByUser(userId: String): EmpresaEntity?
}
