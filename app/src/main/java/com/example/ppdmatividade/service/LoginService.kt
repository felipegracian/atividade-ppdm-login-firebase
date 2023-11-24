package com.example.ppdmatividade.service

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {

    @Headers("Content-Type: application/json")
    @POST("/usuario/cadastrarUsuario")
    suspend fun loginUsuario(@Body body: JsonObject): Response<JsonObject>
}