package com.example.frontchatbot.FirstActivity.model

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token") // Necesario si quieres usar camelCase en Kotlin: accessToken
    val accessToken: String,

    @SerializedName("refresh_token") // Necesario si quieres usar camelCase en Kotlin: refreshToken
    val refreshToken: String
)
