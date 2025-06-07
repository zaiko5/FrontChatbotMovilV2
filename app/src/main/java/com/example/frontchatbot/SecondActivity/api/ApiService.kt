package com.example.frontchatbot.SecondActivity.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("consultas/por-temav2")
    suspend fun getEstadisticasPorTema(
        @Query("year") year: Int?,
        @Query("month") month: Int?,
        @Query("week") week: Int?
    ): Map<String, Double>

    @GET("consultas/por-subtemav2")
    suspend fun getEstadisticasPorSubTema(
        @Query("year") year: Int?,
        @Query("month") month: Int?,
        @Query("week") week: Int?
    ): Map<String, Double>

    @GET("consultas") // el endpoint raíz
    suspend fun getTotalConsultas(
        @Query("year") year: Int?,
        @Query("month") month: Int?,
        @Query("week") week: Int?
    ): Long

    @GET("consultas/cantidad-usuarios")
    suspend fun getCantidadUsuarios(
        @Query("year") year: Int?,
        @Query("month") month: Int?,
        @Query("week") week: Int?
    ):Long
}