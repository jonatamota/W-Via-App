package com.w_via.features.maps.mapLibreAPI

import com.w_via.features.tollservice.TollPoint

data class MapState(
    val latitude: Double = -23.5505,
    val longitude: Double = -46.6333,
    val zoom: Double = 15.0,
    val isMyLocationEnabled: Boolean = false,
    val routePoints: List<Pair<Double, Double>> = emptyList(),
    val tollPoints: List<TollPoint> = emptyList(),
    val showTollNames: Boolean = false,
    val cameraTarget: MapCameraTarget? = null
)

/**
 * Intenção explícita de movimento de câmera. O mapa só anima quando esse valor
 * muda — nunca por auto-snap de dados (rota/pedágio/localização).
 */
sealed interface MapCameraTarget {

    /** Centraliza em uma coordenada com um zoom (ex.: localização do usuário). */
    data class Center(
        val latitude: Double,
        val longitude: Double,
        val zoom: Double
    ) : MapCameraTarget

    /** Ajusta a câmera para caber a rota atual (rota origem → destino). */
    data class FitRoute(
        val topPaddingPx: Int = 0,
        val bottomPaddingPx: Int = 0,
        val horizontalPaddingPx: Int = 0
    ) : MapCameraTarget
}