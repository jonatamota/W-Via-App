package com.w_via.features.utils

/**
 * Formatação de CPF no padrão brasileiro (`###.###.###-##`).
 *
 * Espelha a API do [com.w_via.opencnpj.CNPJFormatter], aplicada ao CPF.
 */
object CPFFormatter {

    /**
     * Formata um CPF para o formato padrão (`###.###.###-##`).
     *
     * @param cpf CPF com ou sem formatação.
     * @return CPF formatado, ou `null` se a entrada não tiver 11 dígitos.
     */
    fun format(cpf: String?): String? {
        if (cpf.isNullOrBlank()) return null
        val digits = unformat(cpf) ?: return null
        if (digits.length != 11) return null

        return buildString {
            append(digits.substring(0, 3))
            append(".")
            append(digits.substring(3, 6))
            append(".")
            append(digits.substring(6, 9))
            append("-")
            append(digits.substring(9, 11))
        }
    }

    /**
     * Remove toda a formatação de um CPF, deixando apenas os dígitos.
     *
     * @return Somente os dígitos, ou `null` se a entrada for vazia.
     */
    fun unformat(cpf: String?): String? {
        if (cpf.isNullOrBlank()) return null
        return cpf.replace(Regex("[^0-9]"), "")
    }

    /**
     * Formata um CPF com uma máscara customizada (padrão `###.###.###-##`).
     */
    fun formatWithMask(cpf: String?, mask: String = "###.###.###-##"): String? {
        if (cpf.isNullOrBlank()) return null
        val unformatted = unformat(cpf) ?: return null
        return applyMask(unformatted, mask)
    }

    /**
     * Formata [cpf] progressivamente e recalcula a posição do cursor, mantendo o
     * caret estável ao digitar atrás de separadores.
     *
     * @return par (texto formatado, nova posição do cursor).
     */
    fun formatWithCursor(cpf: String, posicaoCursor: Int): Pair<String, Int> {
        val digitos = unformat(cpf).orEmpty()
        val textoFormatado = progressiveMask(digitos)
        val novaPosicao = calcularNovaPosicaoCursor(cpf, textoFormatado, posicaoCursor) { it.isDigit() }
        return textoFormatado to novaPosicao
    }

    /** Aplica a máscara do CPF conforme o número de dígitos (inserção progressiva). */
    private fun progressiveMask(digitos: String): String {
        if (digitos.isEmpty()) return ""
        return buildString {
            for (i in digitos.indices) {
                when (i) {
                    3 -> append(".")
                    6 -> append(".")
                    9 -> append("-")
                }
                append(digitos[i])
            }
        }
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
