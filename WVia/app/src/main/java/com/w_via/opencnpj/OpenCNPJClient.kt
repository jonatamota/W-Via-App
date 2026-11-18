package com.w_via.opencnpj

import com.w_via.opencnpj.api.OpenCNPJService
import com.w_via.opencnpj.model.CompanyInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Client class for OpenCNPJ API integration.
 * Provides methods to query company information by CNPJ.
 */
class OpenCNPJClient private constructor() {
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val service: OpenCNPJService by lazy {
        retrofit.create(OpenCNPJService::class.java)
    }
    
    companion object {
        private const val BASE_URL = "https://kitana.opencnpj.com/"
        
        @Volatile
        private var instance: OpenCNPJClient? = null
        
        /**
         * Returns singleton instance of OpenCNPJClient.
         */
        fun getInstance(): OpenCNPJClient {
            return instance ?: synchronized(this) {
                instance ?: OpenCNPJClient().also { instance = it }
            }
        }
    }
    
    /**
     * Fetches company information by CNPJ.
     * 
     * @param cnpj The CNPJ number (can be formatted or unformatted)
     * @return Result containing CompanyInfo if successful, or error if failed
     */
    suspend fun getCompanyInfo(cnpj: String): Result<CompanyInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // Remove formatting before querying
                val unformattedCNPJ = CNPJFormatter.unformat(cnpj) ?: cnpj
                
                if (!CNPJValidator.isValid(unformattedCNPJ)) {
                    return@withContext Result.failure(
                        IllegalArgumentException("CNPJ inválido: $cnpj")
                    )
                }
                
                val response = service.getCompanyInfo(unformattedCNPJ)
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success == true && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(
                            Exception(apiResponse.message ?: "Erro na API: resposta sem dados")
                        )
                    }
                } else {
                    Result.failure(
                        Exception("Erro na API: ${response.code()} - ${response.message()}")
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Checks if a CNPJ exists in the database.
     * 
     * @param cnpj The CNPJ number to check
     * @return true if CNPJ exists, false otherwise
     */
    suspend fun cnpjExists(cnpj: String): Boolean {
        return getCompanyInfo(cnpj).isSuccess
    }
}
