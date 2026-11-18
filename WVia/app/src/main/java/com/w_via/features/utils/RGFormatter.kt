package com.w_via.features.utils

/**
 * Formatação de RG no padrão brasileiro (`##.###.###-#`).
 *
 * Aceita apenas dígitos numéricos (letras são descartadas) e aplica a máscara
 * progressivamente. Espelha a API do [com.w_via.opencnpj.CNPJFormatter],
 * aplicada ao RG.
 */
object RGFormatter {

    private const val MASK = "##.###.###-#"
    private const val MAX_LENGTH = 9

    /**
     * Formata um RG para o padrão (`##.###.###-#`), aplicando a máscara à medida
     * que os caracteres são digitados.
     *
     * @param rg RG com ou sem formatação.
     * @return RG formatado, ou string vazia se a entrada for nula/vazia.
     */
    fun format(rg: String?): String {
        if (rg.isNullOrBlank()) return ""
        return applyMask(unformat(rg), MASK)
    }

    /**
     * Remove toda a formatação de um RG, mantendo apenas os dígitos numéricos.
     * Útil para validação e consulta.
     */
    fun unformat(rg: String?): String {
        if (rg.isNullOrBlank()) return ""
        val cleaned = rg.filter(Char::isDigit)
        return if (cleaned.length > MAX_LENGTH) cleaned.take(MAX_LENGTH) else cleaned
    }

    /**
     * Formata [rg] progressivamente e recalcula a posição do cursor.
     *
     * @return par (texto formatado, nova posição do cursor).
     */
    fun formatWithCursor(rg: String, posicaoCursor: Int): Pair<String, Int> {
        val digitos = unformat(rg)
        val textoFormatado = applyMask(digitos, MASK)
        val novaPosicao = calcularNovaPosicaoCursor(rg, textoFormatado, posicaoCursor) { it.isDigit() }
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
