package com.w_via.features.koin

import androidx.room.Room
import com.w_via.features.company.data.EmpresaDao
import com.w_via.features.company.data.EmpresaRepository
import com.w_via.features.declaration.data.AppDatabase
import com.w_via.features.declaration.data.DeclaracaoDao
import com.w_via.features.declaration.data.DeclaracaoRepository
import com.w_via.features.declaration.data.MIGRATION_1_2
import com.w_via.features.declaration.data.SupabaseUserIdProvider
import com.w_via.features.receipt.data.ReciboDao
import com.w_via.features.receipt.data.ReciboRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "wvia.db"
        )
            // Builds de desenvolvimento podem deixar o banco em versões mais
            // recentes que a atual (ex.: v3 de um schema de teste). Como a v1 é
            // a primeira versão publicada, um downgrade só pode ser resolvido
            // recriando o banco — os dados são offline-first e reintegrados no sync.
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    single<DeclaracaoDao> { get<AppDatabase>().declaracaoDao() }

    single { DeclaracaoRepository(get(), SupabaseUserIdProvider) }

    single<ReciboDao> { get<AppDatabase>().reciboDao() }

    single { ReciboRepository(get(), SupabaseUserIdProvider) }

    single<EmpresaDao> { get<AppDatabase>().empresaDao() }

    single { EmpresaRepository(get(), SupabaseUserIdProvider) }
}
