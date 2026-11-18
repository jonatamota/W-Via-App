package com.w_via.opencnpj.model

import com.google.gson.annotations.SerializedName

/**
 * Wrapper response from OpenCNPJ API.
 */
data class OpenCNPJResponse(
    @SerializedName("success")
    val success: Boolean?,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("data")
    val data: CompanyInfo?
)
