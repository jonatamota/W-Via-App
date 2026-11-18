package com.w_via.opencnpj.model

import com.google.gson.annotations.SerializedName

/**
 * Data model representing company partner/socio from OpenCNPJ API.
 */
data class CompanyPartner(
    @SerializedName("nomeSocio")
    val name: String?,
    
    @SerializedName("descricao")
    val description: String?,
    
    @SerializedName("identificadorSocio")
    val identifier: Int?,
    
    @SerializedName("cnpjCpfSocio")
    val cpfCnpj: String?,
    
    @SerializedName("dataEntradaSociedade")
    val entryDate: String?,
    
    @SerializedName("nomeRepresentante")
    val representativeName: String?,
    
    @SerializedName("faixaEtaria")
    val ageRange: String?
)
