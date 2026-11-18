package com.w_via.features.utils

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Converte um valor monetário para a forma por extenso em pt-BR.
 *
 * Exemplos:
 * - 1500.0  -> "um mil e quinhentos reais"
 * - 123.45  -> "cento e vinte e três reais e quarenta e cinco centavos"
 * - 1000.0  -> "um mil reais"
 * - 0.50    -> "cinquenta centavos"
 * - 1.0     -> "um real"
 */
fun valorPorExtenso(valor: Double): String {
    val centavos = abs((valor * 100).roundToLong())
    val parteInteira = centavos / 100
    val parteCentavos = centavos % 100

    val reais = if (parteInteira > 0) {
        "${numeroPorExtenso(parteInteira)} " + if (parteInteira == 1L) "real" else "reais"
    } else {
        ""
    }

    val centavosTexto = if (parteCentavos > 0) {
        "${numeroPorExtenso(parteCentavos)} " + if (parteCentavos == 1L) "centavo" else "centavos"
    } else {
        ""
    }

    return when {
        reais.isNotEmpty() && centavosTexto.isNotEmpty() -> "$reais e $centavosTexto"
        reais.isNotEmpty() -> reais
        centavosTexto.isNotEmpty() -> centavosTexto
        else -> "zero reais"
    }
}

/**
 * Converte um número (0 a 999.999.999) por extenso em pt-BR.
 * Os grupos (milhão, mil, restante) são unidos por " e ", no padrão
 * "um mil e quinhentos", "um milhão e quinhentos mil e ...".
 */
private fun numeroPorExtenso(valor: Long): String {
    if (valor == 0L) return "zero"

    val partes = mutableListOf<String>()

    val milhoes = valor / 1_000_000
    val milhares = (valor % 1_000_000) / 1_000
    val restante = valor % 1_000

    if (milhoes > 0) {
        val m = numeroPorExtenso(milhoes)
        partes += if (milhoes == 1L) "$m milhão" else "$m milhões"
    }

    if (milhares > 0) {
        val m = if (milhares == 1L) "um mil" else "${numeroPorExtenso(milhares)} mil"
        partes += m
    }

    if (restante > 0) {
        partes += centenaPorExtenso(restante)
    }

    return if (partes.size > 1) partes.joinToString(" e ") else partes[0]
}

private fun centenaPorExtenso(valor: Long): String {
    val resto = valor % 100
    val centenas = valor / 100

    val resultado = StringBuilder()

    if (centenas > 0) {
        when {
            valor == 100L -> return "cem"
            valor < 200 -> resultado.append("cento")
            else -> {
                when (centenas) {
                    2L -> resultado.append("duzentos")
                    3L -> resultado.append("trezentos")
                    4L -> resultado.append("quatrocentos")
                    5L -> resultado.append("quinhentos")
                    6L -> resultado.append("seiscentos")
                    7L -> resultado.append("setecentos")
                    8L -> resultado.append("oitocentos")
                    else -> resultado.append("novecentos")
                }
            }
        }
    }

    if (resto > 0) {
        if (resultado.isNotEmpty()) resultado.append(" e ")
        resultado.append(dezenaUnidadePorExtenso(resto))
    }

    return resultado.toString()
}

private fun dezenaUnidadePorExtenso(valor: Long): String {
    if (valor < 20) {
        return when (valor) {
            1L -> "um"; 2L -> "dois"; 3L -> "três"; 4L -> "quatro"; 5L -> "cinco"
            6L -> "seis"; 7L -> "sete"; 8L -> "oito"; 9L -> "nove"
            10L -> "dez"; 11L -> "onze"; 12L -> "doze"; 13L -> "treze"
            14L -> "quatorze"; 15L -> "quinze"; 16L -> "dezesseis"
            17L -> "dezessete"; 18L -> "dezoito"; 19L -> "dezenove"
            else -> "zero"
        }
    }

    val dezenas = valor / 10
    val unidades = valor % 10

    val dezenaTexto = when (dezenas) {
        2L -> "vinte"; 3L -> "trinta"; 4L -> "quarenta"; 5L -> "cinquenta"
        6L -> "sessenta"; 7L -> "setenta"; 8L -> "oitenta"; else -> "noventa"
    }

    return if (unidades > 0) {
        "$dezenaTexto e ${dezenaUnidadePorExtenso(unidades)}"
    } else {
        dezenaTexto
    }
}
