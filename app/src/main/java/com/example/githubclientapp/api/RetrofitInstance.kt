package com.example.githubclientapp.api

import com.example.githubclientapp.util.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val clientWebservice: ClientWebservice by lazy {
        retrofitInstance.create(ClientWebservice::class.java)
    }
}