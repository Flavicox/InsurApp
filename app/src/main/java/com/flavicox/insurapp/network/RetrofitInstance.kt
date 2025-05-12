package com.flavicox.insurapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetrofitInstance {


    private const val BASE_URL = "https://insurapp-api.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // conexión
        .readTimeout(30, TimeUnit.SECONDS)    // esperando datos
        .writeTimeout(30, TimeUnit.SECONDS)   // escribiendo cuerpo
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    // Si luego tienes más servicios (como FieldApiService), los puedes agregar aquí
    // val fieldApi: FieldApiService by lazy {
    //     retrofit.create(FieldApiService::class.java)
    // }
}
