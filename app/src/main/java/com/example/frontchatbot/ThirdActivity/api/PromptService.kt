package com.example.frontchatbot.ThirdActivity.api

import android.app.VoiceInteractor.Prompt
import com.example.frontchatbot.ThirdActivity.model.PromptResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PromptService {
    @GET("prompt/obtener-prompt")
    suspend fun getPrompt(): PromptResponse

    @POST("prompt/actualizar-prompt")
    suspend fun updatePrompt(@Body newPrompt: PromptResponse)
}