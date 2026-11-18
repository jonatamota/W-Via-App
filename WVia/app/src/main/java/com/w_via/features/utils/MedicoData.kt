package com.w_via.features.utils

import java.util.Locale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Médico de `medicos.json` — dados públicos de registro (estado, número do CRM
 * e nome completo). A base completa do Brasil é distribuída pelos CRMs de cada
 * UF; o arquivo atual contém uma amostra e pode ser substituído pela base
 * completa sem alterar o código.
 */
@Serializable
data class MedicoBasico(
    @SerialName("estado") val estado: String,
    @SerialName("nCRM") val nCRM: String,
    @SerialName("nome") val nome: String
)

/**
 * Leitura e busca de médicos por UF + número do CRM.
 *
 * A busca normaliza a UF para maiúsculas e compara apenas os dígitos do CRM
 * (ignora pontuação e zeros à esquerda), tolerante a como o usuário digita.
 */
object MedicoData {

    /** Converte o conteúdo do JSON de médicos em uma lista. */
    fun loadFromJson(content: String): List<MedicoBasico> =
        Json.decodeFromString(content)

    /**
     * Retorna o nome completo do médico correspondente à UF e ao número do CRM.
     * Retorna null quando não encontra uma referência.
     */
    fun buscarPor(medicos: List<MedicoBasico>, uf: String?, crm: String?): String? {
        if (uf.isNullOrBlank() || crm.isNullOrBlank()) return null
        val alvoUf = uf.trim().uppercase(Locale.ROOT)
        val alvoCrm = apenasDigitos(crm)
        if (alvoCrm.isEmpty()) return null
        return medicos.firstOrNull { medico ->
            medico.estado.trim().uppercase(Locale.ROOT) == alvoUf &&
                apenasDigitos(medico.nCRM) == alvoCrm
        }?.nome?.takeIf { it.isNotBlank() }
    }

    private fun apenasDigitos(texto: String): String =
        texto.filter { it.isDigit() }.trimStart('0')
}
