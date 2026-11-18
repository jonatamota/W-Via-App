package com.w_via.features.utils

import java.text.Normalizer
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Hospitais e cemitérios de cada município brasileiro, carregados de
 * `locais_brasil.json` — um mapa `UF -> cidade -> LocaisCidade`.
 *
 * Os nomes das cidades foram alinhados com `municipios_brasil.json`, então a
 * busca usa a mesma normalização (caixa e acentuação insensíveis) para casar
 * com o que o usuário seleciona no campo de cidade.
 */
object LocaisBrasilData {

    /** Hospitais e cemitérios de um município. */
    data class LocaisCidade(
        val hospitais: List<String> = emptyList(),
        val cemiterios: List<String> = emptyList()
    )

    /**
     * Converte o conteúdo do JSON de locais em um mapa `UF -> cidade -> Locais`.
     * Cidades sem hospitais nem cemitérios são omitidas do arquivo.
     */
    fun fromJson(content: String): Map<String, Map<String, LocaisCidade>> {
        val root = Json.parseToJsonElement(content).jsonObject
        return root.mapValues { (_, ufValue) ->
            ufValue.jsonObject.mapValues { (_, cidadeValue) ->
                val obj = cidadeValue.jsonObject
                LocaisCidade(
                    hospitais = obj["hospitais"]
                        ?.jsonArray
                        ?.map { it.jsonPrimitive.content }
                        .orEmpty(),
                    cemiterios = obj["cemiterios"]
                        ?.jsonArray
                        ?.map { it.jsonPrimitive.content }
                        .orEmpty()
                )
            }
        }
    }

    /** Siglas das UFs presentes nos dados, em ordem alfabética. */
    fun ufs(locais: Map<String, Map<String, LocaisCidade>>): List<String> =
        locais.keys.sorted()

    /** Municípios que possuem ao menos um hospital ou cemitério, em ordem alfabética. */
    fun cidadesPara(
        locais: Map<String, Map<String, LocaisCidade>>,
        uf: String?
    ): List<String> {
        val porUf = porUf(locais, uf) ?: return emptyList()
        return porUf.keys.sorted()
    }

    /** Hospitais do município informado (vazio quando UF/cidade ausentes ou desconhecidas). */
    fun hospitaisPara(
        locais: Map<String, Map<String, LocaisCidade>>,
        uf: String?,
        cidade: String?
    ): List<String> =
        locaisCidade(locais, uf, cidade)?.hospitais.orEmpty()

    /** Cemitérios do município informado (vazio quando UF/cidade ausentes ou desconhecidas). */
    fun cemiteriosPara(
        locais: Map<String, Map<String, LocaisCidade>>,
        uf: String?,
        cidade: String?
    ): List<String> =
        locaisCidade(locais, uf, cidade)?.cemiterios.orEmpty()

    private fun porUf(
        locais: Map<String, Map<String, LocaisCidade>>,
        uf: String?
    ): Map<String, LocaisCidade>? {
        if (uf.isNullOrBlank()) return null
        return locais[uf.trim().uppercase(Locale.ROOT)]
    }

    private fun locaisCidade(
        locais: Map<String, Map<String, LocaisCidade>>,
        uf: String?,
        cidade: String?
    ): LocaisCidade? {
        if (cidade.isNullOrBlank()) return null
        val porUf = porUf(locais, uf) ?: return null
        val alvo = normalize(cidade)
        return porUf.entries.firstOrNull { normalize(it.key) == alvo }?.value
    }

    /** Normalização idêntica à usada no autocomplete do app (NFD + lowercase). */
    private fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
}