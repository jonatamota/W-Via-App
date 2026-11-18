package com.w_via.features.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.w_via.features.utils.CartorioData
import com.w_via.features.utils.CartorioInfo
import com.w_via.features.utils.LocaisBrasilData
import com.w_via.features.utils.MedicoBasico
import com.w_via.features.utils.MedicoData
import com.w_via.features.utils.MunicipioData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Dados de profissões carregados de `profissoes.json` (um array de strings),
 * usados para alimentar o autocomplete inline do [AutocompleteTextField].
 */
object ProfissoesData {

    /** Converte o conteúdo do JSON de profissões em uma lista de strings. */
    fun loadFromJson(content: String): List<String> =
        Json.parseToJsonElement(content).jsonArray.map { it.jsonPrimitive.content }
}

/** Carrega `municipios_brasil.json` uma única vez (cache em [remember]). */
@Composable
fun rememberMunicipios(): Map<String, List<String>> {
    val context = LocalContext.current
    return remember {
        runCatching {
            context.assets.open("municipios_brasil.json")
                .bufferedReader()
                .use { MunicipioData.fromJson(it.readText()) }
        }.getOrDefault(emptyMap())
    }
}

/** Carrega `profissoes.json` uma única vez (cache em [remember]). */
@Composable
fun rememberProfissoes(): List<String> {
    val context = LocalContext.current
    return remember {
        runCatching {
            context.assets.open("profissoes.json")
                .bufferedReader()
                .use { ProfissoesData.loadFromJson(it.readText()) }
        }.getOrDefault(emptyList())
    }
}

/**
 * Carrega `locais_brasil.json` (hospitais e cemitérios por município) uma única
 * vez (cache em [remember]).
 */
@Composable
fun rememberLocaisBrasil(): Map<String, Map<String, LocaisBrasilData.LocaisCidade>> {
    val context = LocalContext.current
    return remember {
        runCatching {
            context.assets.open("locais_brasil.json")
                .bufferedReader()
                .use { LocaisBrasilData.fromJson(it.readText()) }
        }.getOrDefault(emptyMap())
    }
}

/**
 * Carrega `cartorios.json` (cartórios por UF/cidade) uma única vez
 * (cache em [remember]).
 */
@Composable
fun rememberCartorios(): Map<String, Map<String, List<CartorioInfo>>> {
    val context = LocalContext.current
    return remember {
        runCatching {
            context.assets.open("cartorios.json")
                .bufferedReader()
                .use { CartorioData.fromJson(it.readText()) }
        }.getOrDefault(emptyMap())
    }
}

/**
 * Carrega `medicos.json` uma única vez (cache em [remember]).
 */
@Composable
fun rememberMedicos(): List<MedicoBasico> {
    val context = LocalContext.current
    return remember {
        runCatching {
            context.assets.open("medicos.json")
                .bufferedReader()
                .use { MedicoData.loadFromJson(it.readText()) }
        }.getOrDefault(emptyList())
    }
}

/**
 * Carrega `graus_parentesco.json` uma única vez (cache em [remember]). Enquanto
 * o arquivo ainda não existir, retorna lista vazia com segurança.
 */
@Composable
fun rememberGrausParentesco(): List<String> {
    val context = LocalContext.current
    return remember {
        runCatching {
            context.assets.open("graus_parentesco.json")
                .bufferedReader()
                .use { ProfissoesData.loadFromJson(it.readText()) }
        }.getOrDefault(emptyList())
    }
}
