package com.w_via.features.koin

import com.w_via.features.authentication.splash.SplashViewModel
import com.w_via.features.datamycnpj.UploadFileViewModel
import com.w_via.features.receipt.ReciboSucessoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { UploadFileViewModel(get()) }
    viewModel { ReciboSucessoViewModel(get(), get()) }
    viewModel { SplashViewModel(get(), get()) }
}