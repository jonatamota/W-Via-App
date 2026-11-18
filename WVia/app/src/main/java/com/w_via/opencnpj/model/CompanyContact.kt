package com.w_via.opencnpj.model

import com.google.gson.annotations.SerializedName

/**
 * Data model representing company contact information from OpenCNPJ API.
 */
data class CompanyContact(
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("telefone")
    val phone: String?
)
