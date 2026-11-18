package com.w_via.features.koin

import com.w_via.BuildConfig.SUPABASE_ANON_KEY
import com.w_via.BuildConfig.SUPABASE_URL
import com.w_via.features.authentication.AuthSessionStore
import com.w_via.features.authentication.data.RegistrationRepository

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.functions.Functions
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val supabaseModule = module {
    single { AuthSessionStore(androidContext()) }
    single { RegistrationRepository() }

    single {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Storage)
            install(Auth)
            install(Functions)
        }
    }
}