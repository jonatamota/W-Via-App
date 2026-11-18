package com.w_via.features.receipt

/** Opções do seletor de local de saída em "Dados do Funeral". */
enum class TipoLocalSaida(val label: String) {
    HOSPITAL("Hospital"),
    FUNERARIA("Funerária"),
    CASA("Casa"),
    IGREJA("Igreja"),
    OUTRO("Outro")
}

/** Opções do seletor de sepultamento em "Dados do Funeral". */
enum class TipoSepultamento(val label: String) {
    CEMITERIO("Cemitério"),
    FUNERARIA("Funerária")
}
