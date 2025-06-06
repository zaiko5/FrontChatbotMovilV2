package com.example.frontchatbot.FirstActivity.api

import com.example.frontchatbot.FirstActivity.model.LoginRequest
import com.example.frontchatbot.FirstActivity.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login") // <<--- ¡MUY IMPORTANTE! Asegúrate de que esta sea la ruta correcta de tu endpoint de login.
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>
}