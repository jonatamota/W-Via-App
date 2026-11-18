package com.w_via.features.maps.graphhopperAPI

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GraphHopperService(private val apiKey: String) {

    suspend fun calcularRota(
        origemLat: Double, origemLng: Double,
        destinoLat: Double, destinoLng: Double,
        vehicle: String = "car",
        incluirDetalhes: Boolean = true
    ): RouteGeometryResult {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                Log.e(TAG, "GraphHopper API key vazia")
                return@withContext RouteGeometryResult.Empty
            }
            try {
                val details = if (incluirDetalhes) "&details=road_class" else ""
                val url = "https://graphhopper.com/api/1/route" +
                        "?point=$origemLat,$origemLng" +
                        "&point=$destinoLat,$destinoLng" +
                        "&vehicle=$vehicle" +
                        "&locale=pt-BR" +
                        "&points_encoded=true" +
                        details +
                        "&key=$apiKey"

                val (status, response) = httpGet(url)
                if (status !in 200..299) {
                    Log.w(TAG, "GraphHopper HTTP $status (vehicle=$vehicle): ${response.take(300)}")
                    return@withContext RouteGeometryResult.Empty
                }
                parsearResposta(response)
            } catch (e: Exception) {
                Log.w(TAG, "GraphHopper exceção (vehicle=$vehicle): ${e.javaClass.simpleName} ${e.message}")
                RouteGeometryResult.Empty
            }
        }
    }

    private fun httpGet(urlString: String): Pair<Int, String> {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("User-Agent", "W-Via/Android")
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val status = conn.responseCode
            val body =
                (if (status in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()
            status to body
        } finally {
            conn.disconnect()
        }
    }

    private fun parsearResposta(response: String): RouteGeometryResult {
        if (response.isBlank()) return RouteGeometryResult.Empty
        val json = JSONObject(response)
        if (json.has("message")) {
            Log.w(TAG, "GraphHopper erro: ${json.optString("message")}")
            return RouteGeometryResult.Empty
        }
        val paths = json.optJSONArray("paths") ?: return RouteGeometryResult.Empty
        if (paths.length() == 0) return RouteGeometryResult.Empty

        val path = paths.getJSONObject(0)
        val distanceMeters = path.optDouble("distance", 0.0)
        val timeMs = path.optLong("time", 0L)
        val encodedPoints = path.optString("points", "")
        val points = if (encodedPoints.isNotEmpty()) decodificarPolyline(encodedPoints) else emptyList()

        val roadClassDetails = mutableListOf<Triple<Int, Int, String>>()
        val detailsObj = path.optJSONObject("details")
        if (detailsObj != null) {
            val roadClassArray = detailsObj.optJSONArray("road_class")
            if (roadClassArray != null) {
                for (i in 0 until roadClassArray.length()) {
                    val detail = roadClassArray.optJSONArray(i)
                    if (detail != null && detail.length() >= 3) {
                        val start = detail.optInt(0, 0)
                        val end = detail.optInt(1, 0)
                        val rClass = detail.optString(2, "unknown")
                        roadClassDetails.add(Triple(start, end, rClass))
                    }
                }
            }
        }

        return RouteGeometryResult(
            points = points,
            distanceMeters = distanceMeters,
            timeMs = timeMs,
            roadClassDetails = roadClassDetails
        )
    }

    companion object {
        private const val TAG = "GraphHopperService"
    }

    private fun decodificarPolyline(encoded: String): List<Pair<Double, Double>> {
        val points = mutableListOf<Pair<Double, Double>>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0 
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dLng

            points.add(Pair(lat / 1e5, lng / 1e5))
        }
        return points
    }
}