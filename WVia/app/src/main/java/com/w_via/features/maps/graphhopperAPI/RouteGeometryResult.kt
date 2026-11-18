package com.w_via.features.maps.graphhopperAPI

/**
 * Resultado da rota GraphHopper: geometria (polyline) e métricas da API.
 */
data class RouteGeometryResult(
    val points: List<Pair<Double, Double>>,
    val distanceMeters: Double,
    val timeMs: Long,
    val roadClassDetails: List<Triple<Int, Int, String>> = emptyList()
) {
    companion object {
        val Empty = RouteGeometryResult(emptyList(), 0.0, 0L, emptyList())
    }
}
