package com.w_via.features.declaration.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.w_via.features.company.data.EmpresaDao
import com.w_via.features.company.data.EmpresaEntity
import com.w_via.features.receipt.data.ReciboDao
import com.w_via.features.receipt.data.ReciboEntity

/**
 * Banco local da aplicação. Contém a Declaração de Óbito, o Recibo e os dados
 * da empresa (cache do pull).
 *
 * Versão 1: primeira versão publicada na Play Store. Como não há versões
 * anteriores instaladas, nenhuma migração é necessária.
 *
 * Versão 2: adiciona a coluna `isObsolete` nos recibos (recibo refeito/desatualizado).
 */
@Database(
    entities = [DeclaracaoEntity::class, ReciboEntity::class, EmpresaEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun declaracaoDao(): DeclaracaoDao
    abstract fun reciboDao(): ReciboDao
    abstract fun empresaDao(): EmpresaDao
}

/** Migração 1→2: novos recibos nascem não obsoletos (faixa normal). */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE recibos ADD COLUMN isObsolete INTEGER NOT NULL DEFAULT 0"
        )
    }
}
