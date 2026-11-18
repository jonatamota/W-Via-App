package com.w_via.features.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Dados de municípios brasileiros carregados de `municipios_brasil.json`
 * (um mapa de sigla de UF para a lista de municípios daquela UF).
 */
object MunicipioData {

    /**
     * Converte o conteúdo do JSON de municípios em um mapa `UF -> municípios`.
     * A ordem das chaves do arquivo é preservada.
     */
    fun fromJson(content: String): Map<String, List<String>> {
        val root = Json.parseToJsonElement(content).jsonObject
        return root.mapValues { (_, value) ->
            value.jsonArray.map { it.jsonPrimitive.content }
        }
    }

    /** Siglas das UFs presentes nos dados, em ordem alfabética. */
    fun ufs(municipios: Map<String, List<String>>): List<String> = municipios.keys.sorted()

    /**
     * Municípios da UF informada. Aceita sigla com ou sem formatação
     * (case insensitive); retorna lista vazia quando a UF é nula ou desconhecida.
     */
    fun citiesForUf(
        municipios: Map<String, List<String>>,
        uf: String?
    ): List<String> {
        if (uf.isNullOrBlank()) return emptyList()
        return municipios[uf.trim().uppercase()] ?: emptyList()
    }
}
