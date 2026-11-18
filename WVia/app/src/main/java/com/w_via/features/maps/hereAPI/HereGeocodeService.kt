package com.w_via.features.maps.hereAPI

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

class HereGeocodeService(private val apiKey: String) {

  suspend fun geocode(
      endereco: String,
      biasLat: Double? = null,
      biasLng: Double? = null
  ): Pair<Double, Double>? {
    if (endereco.isBlank()) return null
    if (apiKey.isBlank()) {
      Log.e(TAG, "HERE API key vazia")
      return null
    }
    return withContext(Dispatchers.IO) {
      try {
        val encoded = URLEncoder.encode(endereco, "UTF-8")
        // Raio ~15 km em torno do entregador — prioriza resultados na mesma região
        val bias = if (biasLat != null && biasLng != null) "&at=$biasLat,$biasLng,15000" else ""
        val url =
            "https://geocode.search.hereapi.com/v1/geocode" +
                "?q=$encoded" +
                "&in=countryCode:BRA" +
                "&lang=pt-BR" +
                "&limit=1" +
                bias +
                "&apiKey=$apiKey"
        val body = URL(url).readText()
        val json = JSONObject(body)
        if (json.has("error")) {
          Log.w(TAG, "HERE erro para '$endereco': ${json.optString("error")} ${json.optString("error_description")}")
          return@withContext null
        }
        val items = json.optJSONArray("items") ?: return@withContext null
        if (items.length() == 0) {
          Log.w(TAG, "HERE sem resultados para '$endereco'")
          return@withContext null
        }
        val position = items.getJSONObject(0).optJSONObject("position") ?: return@withContext null
        val lat = position.getDouble("lat")
        val lng = position.getDouble("lng")
        Log.d(TAG, "HERE ok '$endereco' -> $lat,$lng")
        Pair(lat, lng)
      } catch (e: Exception) {
        Log.w(TAG, "HERE exceção para '$endereco': ${e.message}")
        null
      }
    }
  }

  companion object {
    private const val TAG = "HereGeocodeService"
  }
}
