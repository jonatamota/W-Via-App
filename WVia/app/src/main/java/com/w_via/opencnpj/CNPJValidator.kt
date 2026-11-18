package com.w_via.opencnpj

/**
 * Utility class for CNPJ validation.
 *
 * Suporta tanto o CNPJ numérico tradicional quanto o alfanumérico (em vigor
 * desde julho/2026): as 12 primeiras posições podem conter letras (A-Z) e
 * dígitos, e as duas últimas são os dígitos verificadores (DV), numéricos,
 * calculados pelo módulo 11 com os caracteres convertidos para base-36
 * (dígitos 0-9 mantêm o valor; A=10 … Z=35).
 */
object CNPJValidator {

    private val weightsFirstDv = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    private val weightsSecondDv = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)

    /**
     * Validates a CNPJ string (numeric or alphanumeric).
     */
    fun isValid(cnpj: String?): Boolean {
        if (cnpj.isNullOrBlank()) return false

        val chars = CNPJFormatter.unformat(cnpj) ?: return false
        if (chars.length != 14) return false
        if (allSame(chars)) return false

        // O DV (posições 13 e 14) é sempre numérico.
        if (chars[12].isLetter() || chars[13].isLetter()) return false

        // Primeiro DV (base = 12) e segundo DV (base = 13).
        val firstDv = verificationDigit(chars, weightsFirstDv) ?: return false
        if (firstDv != chars[12]) return false

        val secondDv = verificationDigit(chars, weightsSecondDv) ?: return false
        return secondDv == chars[13]
    }

    /**
     * Validates a CNPJ and returns the formatted version if valid.
     */
    fun validateAndFormat(cnpj: String?): String? {
        if (!isValid(cnpj)) return null
        return CNPJFormatter.format(cnpj)
    }

    /**
     * Checks if a CNPJ is already in the masked format (XX.XXX.XXX/XXXX-XX),
     * where X may be a letter or a digit and the last two are digits.
     */
    fun isFormatted(cnpj: String?): Boolean {
        if (cnpj.isNullOrBlank()) return false
        return cnpj.matches(
            Regex("^[A-Za-z0-9]{2}\\.[A-Za-z0-9]{3}\\.[A-Za-z0-9]{3}/[A-Za-z0-9]{4}-\\d{2}$")
        )
    }

    /**
     * Converte um caractere alfanumérico para o valor base-36 usado no cálculo
     * do dígito verificador. Retorna -1 para caracteres inválidos.
     */
    internal fun charValue(c: Char): Int {
        val cp = c.code
        return when {
            c.isDigit() -> c.digitToInt()
            cp in 'A'.code..'Z'.code -> cp - 'A'.code + 10
            cp in 'a'.code..'z'.code -> cp - 'a'.code + 10
            else -> -1
        }
    }

    /**
     * Soma `charValue(char) * peso` para cada posição de [chars], devolvendo o
     * dígito verificador correspondente (ou null se algum caractere for inválido).
     */
    private fun verificationDigit(chars: String, weights: IntArray): Char? {
        var sum = 0
        for (i in weights.indices) {
            val v = charValue(chars[i])
            if (v < 0) return null
            sum += v * weights[i]
        }
        val remainder = sum % 11
        return if (remainder < 2) '0' else ('0' + (11 - remainder))
    }

    private fun allSame(chars: String): Boolean = chars.all { it == chars[0] }
}