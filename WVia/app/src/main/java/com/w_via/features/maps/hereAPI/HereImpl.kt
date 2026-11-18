package com.w_via.features.maps.hereAPI

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

data class HereSuggestion(
    val title: String,
    val address: String,
    val complementary: String,
    val primary: String,
    val latitude: Double,
    val longitude: Double
)

class HereAutocompleteService(private val apiKey: String) {

    suspend fun autocomplete(query: String, lat: Double, lng: Double): List<HereSuggestion> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = "https://autosuggest.search.hereapi.com/v1/autosuggest" +
                        "?q=$encodedQuery" +
                        "&at=$lat,$lng" +
                        "&lang=pt-BR" +
                        "&limit=4" +
                        "&apiKey=$apiKey"

                val response = URL(url).readText()
                val json = JSONObject(response)
                val items = json.getJSONArray("items")

                val suggestions = mutableListOf<HereSuggestion>()
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val position = item.optJSONObject("position") ?: continue
                    val address = item.optJSONObject("address")
                    val label = address?.optString("label") ?: item.optString("title")

                    suggestions.add(
                        HereSuggestion(
                            title = item.optString("title"),
                            address = label,
                            complementary = buildComplementary(address, label),
                            primary = buildPrimary(address, item.optString("title")),
                            latitude = position.getDouble("lat"),
                            longitude = position.getDouble("lng")
                        )
                    )
                }
                suggestions
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    // Campo maior do item: apenas rua + número (o "title" da HERE já vem com o
    // endereço completo, o que fazia os dois campos mostrarem tudo). Sem rua,
    // usa o title original (ex.: pontos de interesse).
    private fun buildPrimary(address: JSONObject?, fallbackTitle: String): String {
        if (address == null) return fallbackTitle

        val street = address.optString("street").ifBlank { null }
        val houseNumber = address.optString("houseNumber").ifBlank { null }

        return when {
            street != null && houseNumber != null -> "$street, $houseNumber"
            street != null -> street
            else -> fallbackTitle
        }
    }

    // Informações complementares (distrito, cidade, estado, CEP, país) separadas
    // do título principal, para o campo menor do item de sugestão.
    private fun buildComplementary(address: JSONObject?, fallbackLabel: String): String {
        if (address == null) return fallbackLabel

        val district = address.optString("district").ifBlank { null }
        val city = address.optString("city").ifBlank { address.optString("county").ifBlank { null } }
        val stateName = address.optString("state").ifBlank { null }
        val stateCode = address.optString("stateCode").ifBlank { null }
        val postalCode = address.optString("postalCode").ifBlank { null }
        val countryName = address.optString("countryName").ifBlank { null }

        val parts = mutableListOf<String>()
        district?.let { parts.add(it) }
        city?.let { parts.add(it) }
        when {
            stateName != null && stateCode != null -> parts.add("$stateName - $stateCode")
            stateName != null -> parts.add(stateName)
            stateCode != null -> parts.add(stateCode)
        }
        postalCode?.let { parts.add(it) }
        countryName?.let { parts.add(it) }

        return if (parts.isEmpty()) fallbackLabel else parts.joinToString(", ")
    }
}