package com.w_via.opencnpj.model

import com.google.gson.annotations.SerializedName

/**
 * Data model representing CNAE (Classificação Nacional de Atividades Econômicas) from OpenCNPJ API.
 */
data class CompanyCNAE(
    @SerializedName("cnae")
    val code: String?,
    
    @SerializedName("descricao")
    val description: String?
)
