package com.w_via.features.utils

/**
 * Formatação de INSS/NIT no padrão brasileiro (`000.00000.00-0`).
 *
 * O número de benefício do INSS tem 11 dígitos; a máscara é aplicada de forma
 * progressiva enquanto o usuário digita. Espelha a API do
 * [com.w_via.opencnpj.CNPJFormatter], aplicada ao INSS.
 */
object InssFormatter {

    private const val MASK = "000.00000.00-0"
    private const val MAX_LENGTH = 11

    /**
     * Formata um número de INSS para o padrão (`000.00000.00-0`), aplicando a
     * máscara à medida que os dígitos são digitados.
     *
     * @param value INSS com ou sem formatação.
     * @return INSS formatado, ou string vazia se a entrada for nula/vazia.
     */
    fun format(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return applyMask(unformat(value), MASK)
    }

    /**
     * Remove toda a formatação de um INSS, deixando apenas os dígitos.
     *
     * @return Somente os dígitos, ou string vazia se a entrada for vazia.
     */
    fun unformat(value: String?): String {
        if (value.isNullOrBlank()) return ""
        val digits = value.replace(Regex("[^0-9]"), "")
        return if (digits.length > MAX_LENGTH) digits.take(MAX_LENGTH) else digits
    }

    /**
     * Formata [value] progressivamente e recalcula a posição do cursor.
     *
     * @return par (texto formatado, nova posição do cursor).
     */
    fun formatWithCursor(value: String, posicaoCursor: Int): Pair<String, Int> {
        val digitos = unformat(value)
        val textoFormatado = applyMask(digitos, MASK)
        val novaPosicao = calcularNovaPosicaoCursor(value, textoFormatado, posicaoCursor) { it.isDigit() }
        return textoFormatado to novaPosicao
    }

    private fun applyMask(value: String, mask: String): String {
        val formatted = StringBuilder()
        var valueIndex = 0

        for (maskChar in mask) {
            if (valueIndex >= value.length) break

            if (maskChar == '#') {
                formatted.append(value[valueIndex])
                valueIndex++
            } else {
                formatted.append(maskChar)
            }
        }

        return formatted.toString()
    }
}
