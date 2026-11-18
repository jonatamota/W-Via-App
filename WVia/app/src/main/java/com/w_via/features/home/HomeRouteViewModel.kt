package com.w_via.features.home

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.w_via.features.maps.graphhopperAPI.GraphHopperService
import com.w_via.features.maps.graphhopperAPI.RouteGeometryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.w_via.features.tollservice.ResultadoPedagio
import com.w_via.features.tollservice.TipoVeiculo
import com.w_via.features.tollservice.TollService
import kotlinx.coroutines.flow.first
import kotlin.math.abs

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calculator_route_prefs")

@OptIn(FlowPreview::class)
class MapRouteViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKey = application.packageManager
        .getApplicationInfo(application.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("GRAPHHOPPER_API_KEY") ?: ""

    private val graphHopperService = GraphHopperService(apiKey)

    private val currencyFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    private val _origemCoords = MutableStateFlow<Pair<Double, Double>?>(null)
    private val _destinoCoords = MutableStateFlow<Pair<Double, Double>?>(null)

    private val _origemText = MutableStateFlow("De onde?")
    val origemText: StateFlow<String> = _origemText.asStateFlow()

    private val _destinoText = MutableStateFlow("Para onde?")
    val destinoText: StateFlow<String> = _destinoText.asStateFlow()

    private val _routePoints = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val routePoints: StateFlow<List<Pair<Double, Double>>> = _routePoints.asStateFlow()

    /** Pontos da rota de volta (destino → origem), usados apenas para cálculo de pedágio. */
    private val _routePointsVolta = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())

    /** Distância do trecho origem → destino (polyline), em metros. */
    private val _routeDistanceMeters = MutableStateFlow(0.0)
    val routeDistanceMeters: StateFlow<Double> = _routeDistanceMeters.asStateFlow()

    /** Duração do trecho origem → destino, em ms. */
    private val _routeTimeMs = MutableStateFlow(0L)
    val routeTimeMs: StateFlow<Long> = _routeTimeMs.asStateFlow()

    private val _isLoadingRoute = MutableStateFlow(false)
    val isLoadingRoute: StateFlow<Boolean> = _isLoadingRoute.asStateFlow()

    private val _routeError = MutableStateFlow<String?>(null)
    val routeError: StateFlow<String?> = _routeError.asStateFlow()

    /** Valores monetários em centavos (estilo campo bancário). */
    private val _valorKm = MutableStateFlow(0L)
    val valorKm: StateFlow<Long> = _valorKm.asStateFlow()

    private val _valorCombustivel = MutableStateFlow(0L)
    val valorCombustivel: StateFlow<Long> = _valorCombustivel.asStateFlow()

    /** Apenas dígitos, no máximo 2 (km/L). */
    private val _autonomia = MutableStateFlow("")
    val autonomia: StateFlow<String> = _autonomia.asStateFlow()

    private val _valorAdicional = MutableStateFlow(0L)
    val valorAdicional: StateFlow<Long> = _valorAdicional.asStateFlow()

    private val _idaVolta = MutableStateFlow(false)
    val idaVolta: StateFlow<Boolean> = _idaVolta.asStateFlow()

    /** Após "Calcular rota", exibe [ResultCard] no lugar da [RoutesBar]. */
    private val _mostrarResultadoCalculo = MutableStateFlow(false)
    val mostrarResultadoCalculo: StateFlow<Boolean> = _mostrarResultadoCalculo.asStateFlow()

    private val _resultadoCalculo = MutableStateFlow<RouteCalculationUi?>(null)
    val resultadoCalculo: StateFlow<RouteCalculationUi?> = _resultadoCalculo.asStateFlow()

    private val _mensagemCalculo = MutableStateFlow<String?>(null)
    val mensagemCalculo: StateFlow<String?> = _mensagemCalculo.asStateFlow()

    /** Incrementado a cada troca origem ↔ destino (animação na [RoutesBar]). */
    private val _swapAnimationTrigger = MutableStateFlow(0)
    val swapAnimationTrigger: StateFlow<Int> = _swapAnimationTrigger.asStateFlow()

    private val tollService = TollService(application)

    private val _resultadoPedagio = MutableStateFlow<ResultadoPedagio?>(null)
    val resultadoPedagio: StateFlow<ResultadoPedagio?> = _resultadoPedagio.asStateFlow()

    private val _resultadoPedagioVolta = MutableStateFlow<ResultadoPedagio?>(null)

    private val _tipoVeiculo = MutableStateFlow(TipoVeiculo.CARRO)
    val tipoVeiculo: StateFlow<TipoVeiculo> = _tipoVeiculo.asStateFlow()

    private val dataStore = application.dataStore

    private val VALOR_KM_KEY = longPreferencesKey("valor_km")
    private val VALOR_COMB_KEY = longPreferencesKey("valor_combustivel")
    private val AUTONOMIA_KEY = stringPreferencesKey("autonomia")
    private val VALOR_ADC_KEY = longPreferencesKey("valor_adicional")
    private val IDA_VOLTA_KEY = booleanPreferencesKey("ida_volta")
    private val MODAL_EXPANDED_KEY = booleanPreferencesKey("modal_expanded")

    private val _modalExpanded = MutableStateFlow(true)
    val modalExpanded: StateFlow<Boolean> = _modalExpanded.asStateFlow()

    private val _isPrefsLoaded = MutableStateFlow(false)
    val isPrefsLoaded: StateFlow<Boolean> = _isPrefsLoaded.asStateFlow()

    private val roadTimeFactors = mapOf(
        "motorway"     to 1.25,   // via expressa
        "trunk"        to 1.20,   // rodovia principal
        "primary"      to 1.30,   // avenida principal
        "secondary"    to 1.30,   // avenida secundária
        "tertiary"     to 1.35,   // rua movimentada
        "residential"  to 1.20,   // rua residencial
        "service"      to 1.25    // via de serviço
    )

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            _valorKm.value = prefs[VALOR_KM_KEY] ?: 0L
            _valorCombustivel.value = prefs[VALOR_COMB_KEY] ?: 0L
            _autonomia.value = prefs[AUTONOMIA_KEY] ?: ""
            _valorAdicional.value = prefs[VALOR_ADC_KEY] ?: 0L
            _idaVolta.value = prefs[IDA_VOLTA_KEY] ?: false
            _modalExpanded.value = prefs[MODAL_EXPANDED_KEY] ?: true
            _isPrefsLoaded.value = true
        }

        viewModelScope.launch {
            combine(_origemCoords, _destinoCoords) { origem, destino ->
                Pair(origem, destino)
            }.collect { (origem, destino) ->
                if (origem != null && destino != null) {
                    calcularRota(origem, destino)
                }
            }
        }
    }

    fun setTipoVeiculo(tipo: TipoVeiculo) {
        _tipoVeiculo.value = tipo
        val points = _routePoints.value
        val pointsVolta = _routePointsVolta.value
        viewModelScope.launch {
            if (points.isNotEmpty()) {
                _resultadoPedagio.value = withContext(Dispatchers.Default) {
                    tollService.calcularPedagios(points, tipo)
                }
            }
            if (pointsVolta.isNotEmpty()) {
                _resultadoPedagioVolta.value = withContext(Dispatchers.Default) {
                    tollService.calcularPedagios(pointsVolta, tipo)
                }
            }
        }
    }

    fun setValorKm(centavos: Long) {
        _valorKm.value = centavos
        viewModelScope.launch {
            dataStore.edit { it[VALOR_KM_KEY] = centavos }
        }
    }

    fun setValorCombustivel(centavos: Long) {
        _valorCombustivel.value = centavos
        viewModelScope.launch {
            dataStore.edit { it[VALOR_COMB_KEY] = centavos }
        }
    }

    fun setAutonomia(value: String) {
        val filtered = value.filter { it.isDigit() }.take(2)
        _autonomia.value = filtered
        viewModelScope.launch {
            dataStore.edit { it[AUTONOMIA_KEY] = filtered }
        }
    }

    fun setValorAdicional(centavos: Long) {
        _valorAdicional.value = centavos
        viewModelScope.launch {
            dataStore.edit { it[VALOR_ADC_KEY] = centavos }
        }
    }

    fun setIdaVolta(value: Boolean) {
        _idaVolta.value = value
        viewModelScope.launch {
            dataStore.edit { it[IDA_VOLTA_KEY] = value }
        }
    }

    fun setModalExpanded(value: Boolean) {
        _modalExpanded.value = value
        viewModelScope.launch {
            dataStore.edit { it[MODAL_EXPANDED_KEY] = value }
        }
    }

    fun limparMensagemCalculo() {
        _mensagemCalculo.value = null
    }

    fun limparErroRota() {
        _routeError.value = null
    }

    private fun coordenadasIguais(
        a: Pair<Double, Double>,
        b: Pair<Double, Double>,
        toleranciaGraus: Double = 1e-5
    ): Boolean =
        abs(a.first - b.first) < toleranciaGraus &&
                abs(a.second - b.second) < toleranciaGraus

    fun trocarOrigemDestino() {
        val oc = _origemCoords.value
        val dc = _destinoCoords.value
        val ot = _origemText.value
        val dt = _destinoText.value
        limparResultadoCalculo()
        _origemText.value = dt
        _destinoText.value = ot
        _origemCoords.value = dc
        _destinoCoords.value = oc
        _swapAnimationTrigger.value = _swapAnimationTrigger.value + 1
    }

    /**
     * Litros = km totais ÷ autonomia (km/L).
     * Combustível (R$) = litros × preço por litro.
     * Total por km = valor/km × km totais (aparece apenas quando informado).
     * Custos (combustível + adicional + pedágio) são subtraídos para calcular o lucro.
     */
    fun calcularCustosRota() {
        _mensagemCalculo.value = null

        if (_origemCoords.value == null || _destinoCoords.value == null) {
            _mensagemCalculo.value = "Defina origem e destino."
            return
        }
        if (_routePoints.value.isEmpty() || _routeDistanceMeters.value <= 0.0) {
            _mensagemCalculo.value = "Aguarde a rota ser traçada no mapa."
            return
        }

        val kmTrecho = _routeDistanceMeters.value / 1000.0
        val idaVolta = _idaVolta.value
        val kmTotais = if (idaVolta) kmTrecho * 2.0 else kmTrecho

        val autonomiaKmL = _autonomia.value.toIntOrNull()?.toDouble()
        if (autonomiaKmL == null || autonomiaKmL <= 0.0) {
            _mensagemCalculo.value = "Informe uma autonomia válida (km/L)."
            return
        }

        val precoLitro = _valorCombustivel.value / 100.0
        if (precoLitro < 0.0) {
            _mensagemCalculo.value = "Informe o valor do combustível."
            return
        }

        val valorPorKm = _valorKm.value / 100.0
        if (valorPorKm < 0.0) {
            _mensagemCalculo.value = "Informe o valor por km."
            return
        }

        val adicional = _valorAdicional.value / 100.0

        val litros = kmTotais / autonomiaKmL
        val totalCombustivelReais = litros * precoLitro

        val pedagio = _resultadoPedagio.value
        val totalPedagioIda = when (_tipoVeiculo.value) {
            TipoVeiculo.CARRO -> pedagio?.totalCarro ?: 0.0
            TipoVeiculo.MOTO  -> pedagio?.totalMoto  ?: 0.0
        }
        val totalPedagioVolta = when (_tipoVeiculo.value) {
            TipoVeiculo.CARRO -> _resultadoPedagioVolta.value?.totalCarro ?: 0.0
            TipoVeiculo.MOTO  -> _resultadoPedagioVolta.value?.totalMoto  ?: 0.0
        }
        val totalPedagioViagem = if (idaVolta) totalPedagioIda + totalPedagioVolta else totalPedagioIda

        val custosTotaisReais = totalCombustivelReais + adicional + totalPedagioViagem
        val valorCobrar = valorPorKm * kmTotais
        val exibirValorCobrar = _valorKm.value > 0L
        val lucroReais = valorCobrar - custosTotaisReais

        val tempoTrechoMs = _routeTimeMs.value
        val tempoTotalMs = if (idaVolta) tempoTrechoMs * 2 else tempoTrechoMs

        _resultadoCalculo.value = RouteCalculationUi(
            totalKm = kmTotais,
            totalCombustivelReais = totalCombustivelReais,
            tempoViagemMs = tempoTotalMs,
            valorCobrar = valorCobrar,
            litros = litros,
            custosTotaisReais = custosTotaisReais,
            lucroReais = lucroReais,
            totalKmTexto = formatKm(kmTotais),
            combustivelTexto = currencyFormat.format(totalCombustivelReais),
            tempoTexto = formatDurationMs(tempoTotalMs),
            valorCobrarTexto = if (exibirValorCobrar) currencyFormat.format(valorCobrar) else "",
            litrosTexto = String.format(Locale("pt", "BR"), "%.1f L", litros),
            custosTotaisTexto = currencyFormat.format(custosTotaisReais),
            lucroTexto = currencyFormat.format(lucroReais),
            totalPedagioReais = totalPedagioViagem,
            totalPedagioTexto = currencyFormat.format(totalPedagioViagem),
            quantidadePedagios = pedagio?.quantidade ?: 0,
            exibirValorCobrar = exibirValorCobrar
        )
        _mostrarResultadoCalculo.value = true
    }

    fun voltarParaEdicaoRota() {
        _mostrarResultadoCalculo.value = false
    }

    fun atualizarOrigem(text: String, lat: Double, lng: Double) {
        val novo = Pair(lat, lng)
        val destino = _destinoCoords.value
        if (destino != null && coordenadasIguais(novo, destino)) {
            _mensagemCalculo.value = "Origem e destino não podem ser o mesmo endereço."
            return
        }
        limparResultadoCalculo()
        _origemText.value = text
        _origemCoords.value = novo
    }

    fun atualizarDestino(text: String, lat: Double, lng: Double) {
        val novo = Pair(lat, lng)
        val origem = _origemCoords.value
        if (origem != null && coordenadasIguais(novo, origem)) {
            _mensagemCalculo.value = "Origem e destino não podem ser o mesmo endereço."
            return
        }
        limparResultadoCalculo()
        _destinoText.value = text
        _destinoCoords.value = novo
    }

    fun atualizarOrigemComLocalizacao(lat: Double, lng: Double) {
        val novo = Pair(lat, lng)
        val destino = _destinoCoords.value
        if (destino != null && coordenadasIguais(novo, destino)) {
            _mensagemCalculo.value = "Origem e destino não podem ser o mesmo endereço."
            return
        }
        limparResultadoCalculo()
        _origemCoords.value = novo
    }

    fun atualizarDestinoComLocalizacao(lat: Double, lng: Double) {
        val novo = Pair(lat, lng)
        val origem = _origemCoords.value
        if (origem != null && coordenadasIguais(novo, origem)) {
            _mensagemCalculo.value = "Origem e destino não podem ser o mesmo endereço."
            return
        }
        limparResultadoCalculo()
        _destinoCoords.value = novo
    }

    fun setOrigemText(text: String) {
        limparResultadoCalculo()
        _origemText.value = text
    }

    fun setDestinoText(text: String) {
        limparResultadoCalculo()
        _destinoText.value = text
    }

    private fun limparResultadoCalculo() {
        _resultadoCalculo.value = null
        _mostrarResultadoCalculo.value = false
        _resultadoPedagio.value = null
        _resultadoPedagioVolta.value = null
        _routePointsVolta.value = emptyList()
    }

    private suspend fun calcularRota(
        origem: Pair<Double, Double>,
        destino: Pair<Double, Double>
    ) {
        limparResultadoCalculo()
        _isLoadingRoute.value = true
        _routeError.value = null
        try {
            // Rota de ida (exibida no mapa)
            val result: RouteGeometryResult = graphHopperService.calcularRota(
                origemLat = origem.first,
                origemLng = origem.second,
                destinoLat = destino.first,
                destinoLng = destino.second
            )

            val adjustedTime = calculateAdjustedTime(
                result.timeMs,
                result.roadClassDetails,
                result.points.size
            )

            _routePoints.value = result.points
            _routeDistanceMeters.value = result.distanceMeters
            _routeTimeMs.value = adjustedTime
            _resultadoPedagio.value = withContext(Dispatchers.Default) {
                tollService.calcularPedagios(result.points, _tipoVeiculo.value)
            }

            // Rota principal já desenhada: libera o botão imediatamente,
            // sem esperar o cálculo da rota de volta (feita em segundo plano).
            _isLoadingRoute.value = false
            calcularRotaVolta(origem, destino)

        } catch (e: Exception) {
            _routeError.value = "Erro ao calcular rota"
            _routePoints.value = emptyList()
            _routeDistanceMeters.value = 0.0
            _routeTimeMs.value = 0L
            _isLoadingRoute.value = false
        }
    }

    private suspend fun calcularRotaVolta(
        origem: Pair<Double, Double>,
        destino: Pair<Double, Double>
    ) {
        try {
            // Rota de volta (não exibida no mapa, apenas para cálculo de pedágio)
            val resultVolta: RouteGeometryResult = graphHopperService.calcularRota(
                origemLat = destino.first,
                origemLng = destino.second,
                destinoLat = origem.first,
                destinoLng = destino.second
            )
            _routePointsVolta.value = resultVolta.points
            _resultadoPedagioVolta.value = withContext(Dispatchers.Default) {
                tollService.calcularPedagios(resultVolta.points, _tipoVeiculo.value)
            }
        } catch (e: Exception) {
            _routePointsVolta.value = emptyList()
            _resultadoPedagioVolta.value = null
        }
    }

    private fun calculateAdjustedTime(
        baseTimeMs: Long,
        roadClassDetails: List<Triple<Int, Int, String>>,
        totalPoints: Int
    ): Long {
        if (roadClassDetails.isEmpty() || totalPoints <= 0) return baseTimeMs

        var adjustedTime = 0.0

        roadClassDetails.forEach { (start, end, roadClass) ->
            val segmentFraction = (end - start).toDouble() / totalPoints
            val segmentTime = baseTimeMs * segmentFraction
            val factor = roadTimeFactors[roadClass] ?: 1.2
            adjustedTime += segmentTime * factor
        }

        return adjustedTime.toLong()
    }

    private fun formatKm(km: Double): String =
        String.format(Locale("pt", "BR"), "%.1f km", km)

    private fun formatDurationMs(ms: Long): String {
        if (ms <= 0L) return "—"

        val totalMinutes = ms / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        // Se for acima de 24 horas, formata com dias
        if (hours >= 24) {
            val days = hours / 24
            val remainingHours = hours % 24

            return buildString {
                append("${days} d")
                if (remainingHours > 0) {
                    append(" ${remainingHours}h")
                }
            }
        }

        // Formatação padrão para menos de 24 horas
        return if (hours > 0) {
            if (minutes > 0) "${hours}h ${minutes} min" else "${hours}h"
        } else {
            "${minutes} min"
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                MapRouteViewModel(application)
            }
        }
    }
}

data class RouteCalculationUi(
    val totalKm: Double,
    val totalCombustivelReais: Double,
    val tempoViagemMs: Long,
    val valorCobrar: Double,
    val litros: Double,
    val custosTotaisReais: Double,
    val lucroReais: Double,
    val totalKmTexto: String,
    val combustivelTexto: String,
    val tempoTexto: String,
    val valorCobrarTexto: String,
    val litrosTexto: String,
    val custosTotaisTexto: String,
    val lucroTexto: String,
    val totalPedagioReais: Double,
    val totalPedagioTexto: String,
    val quantidadePedagios: Int,
    val exibirValorCobrar: Boolean
)