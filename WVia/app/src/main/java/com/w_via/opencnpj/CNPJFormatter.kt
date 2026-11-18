package com.w_via.opencnpj

import com.w_via.features.utils.calcularNovaPosicaoCursor

/**
 * Utility class for CNPJ formatting.
 *
 * Suporta o CNPJ alfanumérico (em vigor desde julho/2026): as 12 primeiras
 * posições podem misturar letras maiúsculas (A-Z) e dígitos (0-9), enquanto as
 * duas últimas são numéricas (dígitos verificadores).
 */
object CNPJFormatter {

    /**
     * Formats a CNPJ string to the standard mask (XX.XXX.XXX/XXXX-XX).
     * The base characters may be alphanumeric (ex.: "12.345.678/0001-95" e
     * "A1.B2C.34D/E5FG-95").
     *
     * @return Formatted CNPJ string, or null if it doesn't have 14 characters
     */
    fun format(cnpj: String?): String? {
        val chars = unformat(cnpj) ?: return null
        if (chars.length != 14) return null

        return buildString {
            append(chars.substring(0, 2))
            append(".")
            append(chars.substring(2, 5))
            append(".")
            append(chars.substring(5, 8))
            append("/")
            append(chars.substring(8, 12))
            append("-")
            append(chars.substring(12, 14))
        }
    }

    /**
     * Removes formatting from a CNPJ string, keeping only uppercase letters and
     * digits.
     */
    fun unformat(cnpj: String?): String? {
        if (cnpj.isNullOrBlank()) return null
        return cnpj.replace(Regex("[^A-Za-z0-9]"), "").uppercase()
    }

    /**
     * Formats a CNPJ applying [mask] position by position.
     *
     * @param mask Default "##.###.###/####-##" (the "#" slots are filled by the
     *   source characters, the rest are literals).
     */
    fun formatWithMask(cnpj: String?, mask: String = "##.###.###/####-##"): String? {
        val chars = unformat(cnpj) ?: return null
        return applyMask(chars, mask)
    }

    /**
     * Formats [cnpj] progressively and recomputes the caret position, keeping it
     * stable when typing behind separators. Supports the alphanumeric CNPJ
     * (letters are kept as uppercase in the output but still counted as
     * significant for the caret math).
     *
     * @return pair (formatted text, new caret position).
     */
    fun formatWithCursor(cnpj: String, posicaoCursor: Int): Pair<String, Int> {
        val chars = unformat(cnpj).orEmpty()
        val limited = if (chars.length > 14) chars.take(14) else chars
        val textoFormatado = applyMask(limited, "##.###.###/####-##")
        val overload = calcularNovaPosicaoCursor(cnpj, textoFormatado, posicaoCursor) { it.isLetterOrDigit() }
        return textoFormatado to overload
    }

    /**
     * Applies [mask] to the [chars] progressively. The optional literal
     * characters of the mask (".", "/", "-") are inserted as the value grows.
     */
    fun applyMask(chars: String?, mask: String): String {
        val source = chars ?: return ""
        val formatted = StringBuilder()
        var valueIndex = 0

        for (maskChar in mask) {
            if (valueIndex >= source.length) break
            if (maskChar == '#') {
                formatted.append(source[valueIndex])
                valueIndex++
            } else {
                formatted.append(maskChar)
            }
        }
        return formatted.toString()
    }
}