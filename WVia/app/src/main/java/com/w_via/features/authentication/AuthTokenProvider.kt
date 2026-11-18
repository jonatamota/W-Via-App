package com.w_via.features.authentication

interface AuthTokenProvider {
    suspend fun getAccessToken(): String?
}