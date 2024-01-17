package com.allentom.diffusion.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiHelper {
    lateinit var instance:Retrofit
    fun createInstance(baseUrl:String) {
        val client = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()
        instance =  Retrofit.Builder().baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())

            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
    fun createTestInstance(baseUrl:String) {
        val client = OkHttpClient.Builder()
            .build()
        instance =  Retrofit.Builder().baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}