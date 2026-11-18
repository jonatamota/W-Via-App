package com.w_via.opencnpj.api

import com.w_via.opencnpj.model.OpenCNPJResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service interface for OpenCNPJ API.
 * Base URL: https://api.opencnpj.org/
 */
interface OpenCNPJService {
    
    /**
     * Fetches complete company information by CNPJ.
     * 
     * @param cnpj The CNPJ number (can be formatted or unformatted)
     * @return Response containing OpenCNPJResponse if successful
     */
    @GET("cnpj/{cnpj}")
    suspend fun getCompanyInfo(
        @Path("cnpj") cnpj: String
    ): Response<OpenCNPJResponse>
}
