package com.example.frontchatbot.FirstActivity.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Objeto cliente de retrofit para consumir la api.
object RetrofitClient {
    private const val BASE_URL = "https://d78c-201-163-190-4.ngrok-free.app/" //Modificar a 10.0.2.2 para el emulador, a la ip de la laptop + desactivar el firewall para el celular, ambas con :8081 (puerto api), y a la ip de ngrok para consumir la api de benyi.

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}