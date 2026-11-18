package com.w_via.features.utils

import java.text.Normalizer
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Cartório de registro civil de um município, carregado de `cartorios.json`.
 *
 * Fonte: dados públicos do Registro Civil (transparencia.registrocivil.org.br).
 * Além do nome, guarda telefone, endereço, e-mail, oficial (titular) e o
 * código CNJ — usados para auto-preenchimento ao selecionar um cartório.
 */
data class CartorioInfo(
    val nome: String,
    val telefone: String = "",
    val endereco: String = "",
    val email: String = "",
    val oficial: String = "",
    val numCnj: String = ""
)

/**
 * Cartórios de cada município brasileiro, carregados de `cartorios.json` —
 * um mapa `UF -> cidade -> [CartorioInfo]`.
 *
 * A busca usa a mesma normalização de `municipios_brasil.json`/
 * `locais_brasil.json` (caixa e acentuação insensíveis) para casar com o que o
 * usuário seleciona nos campos de cidade.
 */
object CartorioData {

    /**
     * Converte o conteúdo do JSON de cartórios em um mapa
     * `UF -> cidade -> [CartorioInfo]`. Cidades sem cartório ativo são omitidas.
     */
    fun fromJson(content: String): Map<String, Map<String, List<CartorioInfo>>> {
        val root = Json.parseToJsonElement(content).jsonObject
        return root.mapValues { (_, ufValue) ->
            ufValue.jsonObject.mapValues { (_, cidadeValue) ->
                cidadeValue.jsonArray.map { item ->
                    val obj = item.jsonObject
                    CartorioInfo(
                        nome = obj["cartorio"]?.jsonPrimitive?.content.orEmpty(),
                        telefone = obj["telefone"]?.jsonPrimitive?.content.orEmpty(),
                        endereco = obj["endereco"]?.jsonPrimitive?.content.orEmpty(),
                        email = obj["email"]?.jsonPrimitive?.content.orEmpty(),
                        oficial = obj["oficial"]?.jsonPrimitive?.content.orEmpty(),
                        numCnj = obj["num_cnj"]?.jsonPrimitive?.content.orEmpty()
                    )
                }
            }
        }
    }

    /** Siglas das UFs presentes nos dados, em ordem alfabética. */
    fun ufs(cartorios: Map<String, Map<String, List<CartorioInfo>>>): List<String> =
        cartorios.keys.sorted()

    /** Municípios que possuem ao menos um cartório, em ordem alfabética. */
    fun cidadesPara(
        cartorios: Map<String, Map<String, List<CartorioInfo>>>,
        uf: String?
    ): List<String> {
        val porUf = porUf(cartorios, uf) ?: return emptyList()
        return porUf.keys.sorted()
    }

    /** Cartórios do município informado (vazio quando UF/cidade ausentes ou desconhecidas). */
    fun cartoriosPara(
        cartorios: Map<String, Map<String, List<CartorioInfo>>>,
        uf: String?,
        cidade: String?
    ): List<CartorioInfo> {
        if (cidade.isNullOrBlank()) return emptyList()
        val porUf = porUf(cartorios, uf) ?: return emptyList()
        val alvo = normalize(cidade)
        return porUf.entries.firstOrNull { normalize(it.key) == alvo }?.value.orEmpty()
    }

    /** Nomes dos cartórios do município informado, para o autocomplete. */
    fun nomesPara(
        cartorios: Map<String, Map<String, List<CartorioInfo>>>,
        uf: String?,
        cidade: String?
    ): List<String> =
        cartoriosPara(cartorios, uf, cidade).map { it.nome }

    private fun porUf(
        cartorios: Map<String, Map<String, List<CartorioInfo>>>,
        uf: String?
    ): Map<String, List<CartorioInfo>>? {
        if (uf.isNullOrBlank()) return null
        return cartorios[uf.trim().uppercase(Locale.ROOT)]
    }

    /** Normalização idêntica à usada no autocomplete do app (NFD + lowercase). */
    private fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
}
