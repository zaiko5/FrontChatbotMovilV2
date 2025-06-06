package com.example.frontchatbot.SecondActivity.api

import com.example.frontchatbot.ThirdActivity.api.PromptService
import com.example.frontchatbot.ThirdActivity.model.PromptResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun create(token: String): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .build()

        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8081/") //Modificar a 10.0.2.2 para el emulador, a la ip de la laptop + desactivar el firewall para el celular, ambas con :8081 (puerto api)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}