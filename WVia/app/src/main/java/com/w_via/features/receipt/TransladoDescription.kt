package com.w_via.features.receipt

/**
 * Monta o texto de translado usado no "Adicionar Translado" do passo de serviço:
 *
 * `Translado de [nome do local, cidade - uf] À [nome do local, cidade - uf]`
 *
 * Apenas os dados disponíveis são incluídos: se faltar a cidade ou a UF, o que
 * estiver preenchido é usado; se não houver dados, a parte correspondente é
 * omitida. O resultado é retornado em maiúsculas para combinar com o campo
 * Descrição do recibo.
 */
fun buildTransladoDescription(
    localSaida: String,
    cidadeSaida: String,
    ufSaida: String,
    localSepultamento: String,
    cidadeSepultamento: String,
    ufSepultamento: String
): String {
    val saida = montarLocal(localSaida, cidadeSaida, ufSaida)
    val sepultamento = montarLocal(localSepultamento, cidadeSepultamento, ufSepultamento)

    val texto = buildString {
        if (saida.isNotEmpty()) {
            append("Translado de $saida")
        }
        if (sepultamento.isNotEmpty()) {
            if (saida.isNotEmpty()) {
                append(" À ")
            } else {
                append("Translado À ")
            }
            append(sepultamento)
        }
    }
    return texto.uppercase()
}

private fun montarLocal(nome: String, cidade: String, uf: String): String {
    val partes = buildList {
        nome.trim().takeIf { it.isNotBlank() }?.let(::add)
        val geograficos = listOfNotNull(
            cidade.trim().takeIf { it.isNotBlank() },
            uf.trim().takeIf { it.isNotBlank() }
        ).joinToString(" - ")
            .takeIf { it.isNotBlank() }
        if (geograficos != null) add(geograficos)
    }
    return partes.joinToString(", ")
}
