package com.example.frontchatbot.FirstActivity.api

import com.example.frontchatbot.FirstActivity.model.LoginRequest
import com.example.frontchatbot.FirstActivity.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

//Definimos la interfaz para las peticiones que haremos en esta pantalla, en este caso solo será el login.
interface AuthService {
    @POST("auth/login") //Aquí debe de estar correctamente la ruta despues de la base de la api, y definimos el tipo de peticion que será POST.
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse> //En el cuerpo de la peticion, se mandará un objeto LoginRequest, definido en otra clase.
}