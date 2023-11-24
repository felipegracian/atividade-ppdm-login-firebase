package com.example.ppdmatividade.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitHelper {

    private const val baseurl = "http://10.107.144.12:3000"

    private val retrofitFactory =
        Retrofit.Builder().
        baseUrl(baseurl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    fun getLoginService(): LoginService {
        return retrofitFactory.create(LoginService::class.java)
    }
}