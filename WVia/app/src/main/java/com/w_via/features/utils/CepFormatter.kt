package com.w_via.features.utils

/**
 * Formatação de CEP no padrão brasileiro (`00000-000`).
 *
 * A máscara é aplicada de forma progressiva enquanto o usuário digita os 8
 * dígitos do CEP.
 */
object CepFormatter {

    private const val MAX_LENGTH = 8

    /**
     * Formata um CEP para o padrão (`00000-000`), aplicando a máscara à medida
     * que os dígitos são digitados.
     *
     * @param cep CEP com ou sem formatação.
     * @return CEP formatado, ou string vazia se a entrada for nula/vazia.
     */
    fun format(cep: String?): String {
        val digits = unformat(cep)
        if (digits.isEmpty()) return ""

        val limited = if (digits.length > MAX_LENGTH) digits.take(MAX_LENGTH) else digits
        if (limited.length <= 5) return limited
        return limited.substring(0, 5) + "-" + limited.substring(5)
    }

    /**
     * Remove toda a formatação de um CEP, deixando apenas os dígitos.
     *
     * @return Somente os dígitos, ou string vazia se a entrada for vazia.
     */
    fun unformat(cep: String?): String {
        if (cep.isNullOrBlank()) return ""
        return cep.replace(Regex("[^0-9]"), "")
    }

    /** True quando o CEP possui exatamente 8 dígitos. */
    fun isValid(cep: String?): Boolean = unformat(cep).length == MAX_LENGTH
}
