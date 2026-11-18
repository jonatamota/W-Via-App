package com.w_via.features.koin

import com.w_via.features.authentication.data.RegistrationRepository
import com.w_via.features.company.data.EmpresaRepository
import com.w_via.features.declaration.data.DeclaracaoRepository
import com.w_via.features.receipt.data.ReciboRepository
import com.w_via.features.sync.CompanyProfileSync
import com.w_via.features.sync.DeclaracaoRemoteSync
import com.w_via.features.sync.ReciboRemoteSync
import com.w_via.features.sync.SupabaseCompanyProfileSync
import com.w_via.features.sync.SupabaseDeclaracaoRemoteSync
import com.w_via.features.sync.SupabaseReciboRemoteSync
import com.w_via.features.sync.SyncManager
import com.w_via.features.sync.SyncRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val syncModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    single<DeclaracaoRemoteSync> { SupabaseDeclaracaoRemoteSync(get()) }
    single<ReciboRemoteSync> { SupabaseReciboRemoteSync(get()) }

    single<CompanyProfileSync> {
        SupabaseCompanyProfileSync(
            androidContext(),
            get<RegistrationRepository>(),
            get<EmpresaRepository>()
        )
    }

    single {
        SyncManager(
            get<DeclaracaoRepository>(),
            get<ReciboRepository>(),
            get<DeclaracaoRemoteSync>(),
            get<ReciboRemoteSync>(),
            get()
        )
    }

    single { SyncRunner(get(), get()) }
}
