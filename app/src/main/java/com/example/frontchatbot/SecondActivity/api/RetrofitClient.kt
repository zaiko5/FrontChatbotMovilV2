package com.example.frontchatbot.SecondActivity.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Clase que crea el cliente de retrofit con el token
object RetrofitClient {
    fun create(token: String): ApiService {
        val client = OkHttpClient.Builder() //Creamos el cliente
            .addInterceptor(AuthInterceptor(token)) //Agregamos el interceptador con el token.
            .build()

        return Retrofit.Builder() //Retornamos el cliente con el token, usando la ruta url de la api.
            .baseUrl("https://d78c-201-163-190-4.ngrok-free.app/") //Modificar a 10.0.2.2 para el emulador, a la ip de la laptop + desactivar el firewall para el celular, ambas con :8081 (puerto api) y a la ip de ngrok para consumir la api de benyi.
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}