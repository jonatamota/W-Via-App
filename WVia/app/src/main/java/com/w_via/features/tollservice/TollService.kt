package com.w_via.features.tollservice

import android.content.Context
import org.json.JSONObject
import kotlin.math.*

data class Praca(
    val id: String,
    val nome: String,
    val estado: String,
    val concessionaria: String,
    val lat: Double,
    val lng: Double,
    val bearing: Int,
    val valorCarro: Double,
    val valorMoto: Double
)

enum class TipoVeiculo { CARRO, MOTO }

class TollService(private val context: Context) {

    private val pracas: List<Praca> by lazy { carregarPracas() }

    private fun carregarPracas(): List<Praca> {
        return try {
            val json = context.assets.open("pedagios_brasil.json")
                .bufferedReader()
                .use { it.readText() }

            val root = JSONObject(json)
            val array = root.getJSONArray("pedagios")
            val lista = mutableListOf<Praca>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val valores = obj.getJSONObject("valores")
                lista.add(
                    Praca(
                        id = obj.getString("id"),
                        nome = obj.getString("nome"),
                        estado = obj.getString("estado"),
                        concessionaria = obj.optString("concessionaria", ""),
                        lat = obj.getDouble("lat"),
                        lng = obj.getDouble("lng"),
                        bearing = obj.optInt("bearing", 0),
                        valorCarro = valores.getDouble("carro"),
                        valorMoto = valores.getDouble("moto")
                    )
                )
            }
            lista
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun calcularPedagios(
        routePoints: List<Pair<Double, Double>>,
        tipoVeiculo: TipoVeiculo
    ): ResultadoPedagio {

        val hitPracas = mutableMapOf<Praca, Double>()

        for (ponto in routePoints) {
            for (praca in pracas) {
                if (abs(ponto.first - praca.lat) > 0.02 || abs(ponto.second - praca.lng) > 0.02) continue

                val dist = distanciaMetros(
                    ponto.first, ponto.second,
                    praca.lat, praca.lng
                )

                if (dist <= 150.0) {
                    val currentMin = hitPracas[praca]
                    if (currentMin == null || dist < currentMin) {
                        hitPracas[praca] = dist
                    }
                }
            }
        }

        val pracasAgrupadas = hitPracas.entries.groupBy { normalizeGroupName(it.key.nome) }
        val listaFinal = mutableListOf<Praca>()

        for ((_, group) in pracasAgrupadas) {
            val melhorPraca = group.minByOrNull { it.value }!!
            if (melhorPraca.value > 100.0) continue

            val praca = melhorPraca.key

            // Verifica distância perpendicular ao segmento mais próximo
            val distPerp = distanciaPerpendicular(routePoints, praca.lat, praca.lng)
            if (distPerp > 15.0) continue // praça está em pista paralela

            listaFinal.add(praca)
        }

        val totalCarro = listaFinal.sumOf { it.valorCarro }
        val totalMoto = listaFinal.sumOf { it.valorMoto }
        val totalVeiculo = when (tipoVeiculo) {
            TipoVeiculo.CARRO -> totalCarro
            TipoVeiculo.MOTO -> totalMoto
        }

        return ResultadoPedagio(
            pracas = listaFinal,
            totalCarro = totalCarro,
            totalMoto = totalMoto,
            totalVeiculo = totalVeiculo,
            quantidade = listaFinal.size
        )
    }

    /**
     * Calcula a menor distância perpendicular da praça a qualquer segmento
     * da polyline que esteja dentro de 300m da praça.
     */
    private fun distanciaPerpendicular(
        routePoints: List<Pair<Double, Double>>,
        pracaLat: Double,
        pracaLng: Double
    ): Double {
        var menorDist = Double.MAX_VALUE

        for (i in 0 until routePoints.size - 1) {
            val a = routePoints[i]
            val b = routePoints[i + 1]

            // Filtra segmentos muito distantes para performance
            if (abs(a.first - pracaLat) > 0.02 && abs(b.first - pracaLat) > 0.02) continue

            val dist = distanciaPontoAoSegmento(
                pracaLat, pracaLng,
                a.first, a.second,
                b.first, b.second
            )

            if (dist < menorDist) menorDist = dist
        }

        return menorDist
    }

    /**
     * Distância perpendicular de um ponto P ao segmento AB.
     * Usa projeção vetorial em coordenadas planas (válido para distâncias pequenas).
     */
    private fun distanciaPontoAoSegmento(
        pLat: Double, pLng: Double,
        aLat: Double, aLng: Double,
        bLat: Double, bLng: Double
    ): Double {
        // Converte para metros usando fator de escala local
        val cosLat = cos(Math.toRadians((aLat + bLat) / 2))
        val mPorGrauLat = 111320.0
        val mPorGrauLng = 111320.0 * cosLat

        val ax = aLng * mPorGrauLng
        val ay = aLat * mPorGrauLat
        val bx = bLng * mPorGrauLng
        val by = bLat * mPorGrauLat
        val px = pLng * mPorGrauLng
        val py = pLat * mPorGrauLat

        val dx = bx - ax
        val dy = by - ay
        val lenSq = dx * dx + dy * dy

        if (lenSq == 0.0) {
            // Segmento degenerado (A == B)
            return sqrt((px - ax).pow(2) + (py - ay).pow(2))
        }

        // Projeção de P sobre AB, clampada entre 0 e 1
        val t = ((px - ax) * dx + (py - ay) * dy) / lenSq
        val tClamped = t.coerceIn(0.0, 1.0)

        val projX = ax + tClamped * dx
        val projY = ay + tClamped * dy

        return sqrt((px - projX).pow(2) + (py - projY).pow(2))
    }

    private fun normalizeGroupName(nome: String): String {
        var base = nome
        base = base.replace(Regex("(?i)\\s*\\(.*?\\)"), "")
        base = base.replace(Regex("(?i)^[P]\\d+\\s*[-–]\\s*"), "")
        base = base.replace(Regex("(?i)\\s+[-–]?\\s*\\b(?:\\d+|[A-Z])\\b$"), "")
        return base.trim().lowercase()
    }

    private fun distanciaMetros(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}

data class ResultadoPedagio(
    val pracas: List<Praca>,
    val totalCarro: Double,
    val totalMoto: Double,
    val totalVeiculo: Double,
    val quantidade: Int
)