package com.allentom.diffusion.api.translate.googletranslate

import com.allentom.diffusion.api.translate.TranslateResult
import com.allentom.diffusion.api.translate.Translator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GoogleTranslateApiHelper {
    val BASE_URL = "https://translate.google.com/"
    lateinit var instance: Retrofit
    fun createInstance() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        instance = Retrofit.Builder().baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

fun getGoogleTranslateApiClient(): GoogleTranslateAPI {
    return GoogleTranslateApiHelper.instance.create(GoogleTranslateAPI::class.java)
}

class GoogleTranslator : Translator {
    override suspend fun translate(text: String): TranslateResult {
        val resp = getGoogleTranslateApiClient().translate(text)
        return TranslateResult(resp.sentences.mapNotNull { it.trans })
    }

    override fun init() {
        GoogleTranslateApiHelper.createInstance()
    }
}