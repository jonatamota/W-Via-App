package com.w_via.features.utils

/**
 * Cálculo de posição do cursor após aplicar uma máscara de formatação.
 *
 * Replica a lógica de [PhoneFormatter], porém com um predicado de "caractere
 * significativo" configurável: dígitos para CPF/RG/INSS, letras+dígitos para o
 * CNPJ alfanumérico. Comum aos formatadores de documento.
 */
internal fun calcularNovaPosicaoCursor(
    textoAntigo: String,
    textoNovo: String,
    posicaoAntiga: Int,
    isSignificante: (Char) -> Boolean
): Int {
    val significativosAntes = textoAntigo.take(posicaoAntiga).count(isSignificante)
    var contados = 0
    var novaPosicao = 0

    for (i in textoNovo.indices) {
        if (isSignificante(textoNovo[i])) {
            contados++
            if (contados > significativosAntes) {
                break
            }
        }
        novaPosicao = i + 1
    }
    return minOf(novaPosicao, textoNovo.length)
}