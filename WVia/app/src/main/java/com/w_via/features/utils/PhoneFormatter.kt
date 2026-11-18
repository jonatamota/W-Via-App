package com.w_via.features.utils

import androidx.compose.ui.text.TextRange

/**
 * Formatação de telefone brasileiro com DDD no padrão "(11) 99999-9999",
 * mantendo a posição do cursor durante a edição.
 */
object PhoneFormatter {

    /** Máscara aplicada (sem formatação = apenas dígitos). */
    fun format(telefone: String): String {
        val digitos = telefone.filter { it.isDigit() }
        return quandoDigitos(digitos)
    }

    /**
     * Formata [telefone] e recalcula a posição do cursor, compatível com
     * inserção e exclusão de caracteres no meio do texto.
     *
     * @return par (texto formatado, nova posição do cursor).
     */
    fun formatWithCursor(telefone: String, posicaoCursor: Int): Pair<String, Int> {
        val digitos = telefone.filter { it.isDigit() }
        val textoFormatado = quandoDigitos(digitos)
        val novaPosicaoCursor = calcularNovaPosicaoCursor(telefone, textoFormatado, posicaoCursor)
        return Pair(textoFormatado, novaPosicaoCursor)
    }

    /** Aplica a máscara no texto conforme o número de dígitos. */
    private fun quandoDigitos(digitos: String): String = when {
        digitos.isEmpty() -> ""
        digitos.length <= 2 -> "($digitos"
        digitos.length <= 6 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2)}"
        digitos.length <= 10 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 6)}-${digitos.substring(6)}"
        else -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 7)}-${digitos.substring(7, minOf(11, digitos.length))}"
    }

    /** Recalcula a posição do cursor comparando dígitos antes desse ponto. */
    private fun calcularNovaPosicaoCursor(textoAntigo: String, textoNovo: String, posicaoAntiga: Int): Int {
        val digitosAntesDoCaractere = textoAntigo.take(posicaoAntiga).count { it.isDigit() }
        var digitosContados = 0
        var novaPosicao = 0

        for (i in textoNovo.indices) {
            if (textoNovo[i].isDigit()) {
                digitosContados++
                if (digitosContados > digitosAntesDoCaractere) {
                    break
                }
            }
            novaPosicao = i + 1
        }
        return minOf(novaPosicao, textoNovo.length)
    }

    /** Número de dígitos (útil para validar telefone completo = 10 ou 11). */
    fun digitCount(value: String): Int = value.count { it.isDigit() }

    /**
     * True quando o telefone tem DDD + 9 dígitos (celular, 11 no total) ou
     * DDD + 8 dígitos (fixo, 10 no total).
     */
    fun isValid(value: String): Boolean {
        val count = digitCount(value)
        return count == 10 || count == 11
    }

    /** Seleção de texto para usar em [TextFieldValue] após formatar. */
    fun selectionFor(position: Int): TextRange = TextRange(position)
}