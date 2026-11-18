package com.w_via.features.home

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.w_via.features.maps.hereAPI.HereAutocompleteService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

private val Context.searchDataStore by preferencesDataStore(name = "search_history_prefs")

@OptIn(FlowPreview::class)
class SearchDestinationViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKey = application.packageManager
        .getApplicationInfo(application.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("HERE_API_KEY") ?: ""

    private val hereService = HereAutocompleteService(apiKey)

    private val _searchQuery = MutableStateFlow(TextFieldValue(""))
    val searchQuery: StateFlow<TextFieldValue> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val HISTORY_KEY = stringPreferencesKey("search_history")
    
    private val _searchHistory = MutableStateFlow<List<PlaceSuggestion>>(emptyList())
    val searchHistory: StateFlow<List<PlaceSuggestion>> = _searchHistory.asStateFlow()

    // localização do usuário para bias de busca
    var userLat: Double = -23.5505
    var userLng: Double = -46.6333

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.text.length >= 3 }
                .collect { query -> fetchSuggestions(query.text) }
        }

        viewModelScope.launch {
            try {
                val prefs = application.searchDataStore.data.first()
                val historyJson = prefs[HISTORY_KEY] ?: "[]"
                _searchHistory.value = parseHistory(historyJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onQueryChange(query: TextFieldValue) {
        _searchQuery.value = query
        if (query.text.length < 3) _uiState.value = SearchUiState.Idle
    }

    // Pré-preenche o campo de busca com o texto de origem/destino já selecionado
    // e posiciona o cursor no final do texto.
    fun prefillQuery(text: String) {
        _searchQuery.value = TextFieldValue(
            text = text,
            selection = TextRange(text.length)
        )
    }

    fun clearQuery() {
        _searchQuery.value = TextFieldValue("")
        _uiState.value = SearchUiState.Idle
    }

    private suspend fun fetchSuggestions(query: String) {
        _uiState.value = SearchUiState.Loading
        try {
            val results = hereService.autocomplete(query, userLat, userLng)
            _uiState.value = SearchUiState.Results(
                results.map {
                    PlaceSuggestion(
                        placeId = "${it.latitude},${it.longitude}",
                        primaryText = it.primary,
                        secondaryText = it.complementary,
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = SearchUiState.Error("Erro ao buscar endereços")
        }
    }

    fun saveToHistory(place: PlaceSuggestion) {
        viewModelScope.launch {
            val current = _searchHistory.value.toMutableList()
            current.removeAll { it.placeId == place.placeId || it.primaryText == place.primaryText }
            current.add(0, place)
            val updated = current.take(6)
            _searchHistory.value = updated

            try {
                getApplication<Application>().searchDataStore.edit { prefs ->
                    prefs[HISTORY_KEY] = serializeHistory(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseHistory(jsonStr: String): List<PlaceSuggestion> {
        val list = mutableListOf<PlaceSuggestion>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PlaceSuggestion(
                        placeId = obj.optString("placeId", ""),
                        primaryText = obj.optString("primaryText", ""),
                        secondaryText = obj.optString("secondaryText", ""),
                        latitude = obj.optDouble("latitude", 0.0),
                        longitude = obj.optDouble("longitude", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun serializeHistory(list: List<PlaceSuggestion>): String {
        val arr = JSONArray()
        list.forEach { p ->
            val obj = JSONObject()
            obj.put("placeId", p.placeId)
            obj.put("primaryText", p.primaryText)
            obj.put("secondaryText", p.secondaryText)
            obj.put("latitude", p.latitude)
            obj.put("longitude", p.longitude)
            arr.put(obj)
        }
        return arr.toString()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                SearchDestinationViewModel(application)
            }
        }
    }
}