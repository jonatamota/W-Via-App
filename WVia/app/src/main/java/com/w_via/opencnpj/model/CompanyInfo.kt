package com.w_via.opencnpj.model

import com.google.gson.annotations.SerializedName

/**
 * Complete data model representing company information from OpenCNPJ API.
 */
data class CompanyInfo(
    @SerializedName("cnpj")
    val cnpj: String?,
    
    @SerializedName("razaoSocial")
    val corporateName: String?,
    
    @SerializedName("nomeFantasia")
    val tradeName: String?,
    
    @SerializedName("situacaoCadastral")
    val registrationStatus: String?,
    
    @SerializedName("dataSituacaoCadastral")
    val registrationStatusDate: String?,
    
    @SerializedName("motivoSituacaoCadastral")
    val registrationStatusReason: String?,
    
    @SerializedName("naturezaJuridica")
    val legalNature: String?,
    
    @SerializedName("dataInicioAtividades")
    val activityStartDate: String?,
    
    @SerializedName("capitalSocial")
    val shareCapital: Double?,
    
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("telefone")
    val phone: String?,
    
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
    
    @SerializedName("cnaes")
    val cnaes: List<CompanyCNAE>?,
    
    @SerializedName("socios")
    val partners: List<CompanyPartner>?
) {
    // Propriedades em português para facilitar acesso
    val emailPt: String?
        get() = email
    
    val cep: String?
        get() = zipCode
    
    val logradouro: String?
        get() = street
    
    val bairro: String?
        get() = neighborhood
    
    val uf: String?
        get() = state
    
    val municipio: String?
        get() = city
    
    val numero: String?
        get() = number
    
    val complemento: String?
        get() = complement
    
    /**
     * Returns formatted address.
     */
    fun getFullAddress(): String {
        return buildString {
            street?.let { append(it) }
            number?.let { 
                if (isNotEmpty()) append(", ")
                append(it) 
            }
            complement?.let { 
                if (isNotEmpty()) append(" - ")
                append(it) 
            }
            neighborhood?.let { 
                if (isNotEmpty()) append(", ")
                append(it) 
            }
            city?.let { 
                if (isNotEmpty()) append(" - ")
                append(it) 
            }
            state?.let { 
                if (isNotEmpty()) append("/")
                append(it) 
            }
            zipCode?.let { 
                if (isNotEmpty()) append(" - CEP: ")
                append(it) 
            }
        }
    }
    
    /**
     * Returns formatted phone number.
     */
    fun getFormattedPhone(): String? {
        return phone
    }
    
    /**
     * Returns formatted share capital as BRL currency.
     */
    fun getFormattedShareCapital(): String {
        return "R$ ${String.format("%.2f", shareCapital ?: 0.0)}"
    }
}
