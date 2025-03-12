package com.example.nutcracker_streaming_app.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

interface NutcrackerApi {
    companion object RetrofitBuilder {
        private const val BASE_URL = "https://site-demo.pikemedia.live/"

        private fun getRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api: NutcrackerApi = getRetrofit().create()
    }
}