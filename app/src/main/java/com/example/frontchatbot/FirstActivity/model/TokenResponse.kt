package com.example.frontchatbot.FirstActivity.model

import com.google.gson.annotations.SerializedName

//Data class para modelar la respuesta del login (el token)
data class TokenResponse(
    @SerializedName("access_token") //Necesario para poner el nombre real de la respuesta, ya que en la api se usa snake y aqui camel.
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)
