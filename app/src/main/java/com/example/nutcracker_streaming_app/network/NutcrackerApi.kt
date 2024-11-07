package com.example.nutcracker_streaming_app.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NutcrackerApi {

    @GET("api/components")
    suspend fun getAllPlanets(@Query("search") search: String? = null): Unit

    @GET("api/components/{id}")
    suspend fun getPlanet(@Path("id") id: String): Unit

    companion object RetrofitBuilder {
        private const val BASE_URL = "http://192.168.2.102:8000/"

        private fun getRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api: NutcrackerApi = getRetrofit().create()
    }

}