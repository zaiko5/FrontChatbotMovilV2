package com.example.frontchatbot.SecondActivity.api

import retrofit2.http.GET
import retrofit2.http.Query

//Definicion de las interfaces que se van a utilizar para hacer las peticiones a la API.
interface ApiService {
    @GET("consultas/por-temav2") //Consulta para obtener las estadisticas por tema
    suspend fun getEstadisticasPorTema(
        @Query("year") year: Int?, //Parametros de la consulta
        @Query("month") month: Int?,
        @Query("week") week: Int?
    ): Map<String, Double> //Tipo de retorno de la api.

    @GET("consultas/por-subtemav2") //Para obtener las estadisticas por subtema
    suspend fun getEstadisticasPorSubTema(
        @Query("year") year: Int?,
        @Query("month") month: Int?,
        @Query("week") week: Int?
    ): Map<String, Double>

    @GET("consultas") // el endpoint raíz, para obtener el numero total de consultas
    suspend fun getTotalConsultas(
        @Query("year") year: Int?,
        @Query("month") month: Int?,
        @Query("week") week: Int?
    ): Long

    @GET("consultas/cantidad-usuarios") //Para obtener el numero de usuarios que han consultado
    suspend fun getCantidadUsuarios(
        @Query("year") year: Int?,
        @Query("month") month: Int?,
        @Query("week") week: Int?
    ):Long
}