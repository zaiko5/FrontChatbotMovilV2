package com.example.frontchatbot.SecondActivity.api

import okhttp3.Interceptor
import okhttp3.Response

//Clase que intercepta y agrega el token a todas las peticiones
class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(newRequest)
    }
}