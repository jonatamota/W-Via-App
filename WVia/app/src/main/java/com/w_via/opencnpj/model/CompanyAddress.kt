package com.w_via.opencnpj.model

import com.google.gson.annotations.SerializedName

/**
 * Data model representing company address from OpenCNPJ API.
 */
data class CompanyAddress(
    @SerializedName("logradouro")
    val street: String?,
    
    @SerializedName("numero")
    val number: String?,
    
    @SerializedName("complemento")
    val complement: String?,
    
    @SerializedName("bairro")
    val neighborhood: String?,
    
    @SerializedName("municipio")
    val city: String?,
    
    @SerializedName("uf")
    val state: String?,
    
    @SerializedName("cep")
    val zipCode: String?,
    
    @SerializedName("codigo_ibge_municipio")
    val ibgeCityCode: String?
)
