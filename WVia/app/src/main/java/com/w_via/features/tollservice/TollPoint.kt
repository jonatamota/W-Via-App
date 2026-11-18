package com.w_via.features.tollservice

data class TollPoint(
    val lat: Double,
    val lng: Double,
    val nome: String,
    val valor: Double,
    val concessionaria: String
)