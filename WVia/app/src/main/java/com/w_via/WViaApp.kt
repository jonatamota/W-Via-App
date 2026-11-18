package com.w_via

import android.app.Application
import com.w_via.features.koin.databaseModule
import com.w_via.features.koin.supabaseModule
import com.w_via.features.koin.syncModule
import com.w_via.features.koin.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.maplibre.android.MapLibre

class WViaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        startKoin {
            androidLogger()
            androidContext(this@WViaApp)
            modules(viewModelModule, supabaseModule, databaseModule, syncModule)
        }
    }
}