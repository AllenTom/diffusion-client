package com.allentom.diffusion.api.translate.googletranslate

import com.allentom.diffusion.api.translate.TranslateLanguages
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
    private fun getLangParamMapping(lang: TranslateLanguages): String {
        return when (lang) {
            TranslateLanguages.Auto -> "auto"
            TranslateLanguages.Chinese -> "zh-CN"
            TranslateLanguages.English -> "en"
            TranslateLanguages.Korean -> "ko"
            TranslateLanguages.Japanese -> "ja"
            TranslateLanguages.French -> "fr"
            TranslateLanguages.Spanish -> "es"
            TranslateLanguages.Thai -> "th"
            TranslateLanguages.Arabic -> "ar"
            TranslateLanguages.Russian -> "ru"
            TranslateLanguages.Portuguese -> "pt"
            TranslateLanguages.German -> "de"
            TranslateLanguages.Italian -> "it"
            TranslateLanguages.Greek -> "el"
            TranslateLanguages.Dutch -> "nl"
            TranslateLanguages.Polish -> "pl"
            TranslateLanguages.Bulgarian -> "bg"
            TranslateLanguages.Estonian -> "et"
            TranslateLanguages.Danish -> "da"
            TranslateLanguages.Finnish -> "fi"
            TranslateLanguages.Czech -> "cs"
            TranslateLanguages.Romanian -> "ro"
            TranslateLanguages.Slovenian -> "sl"
            TranslateLanguages.Swedish -> "sv"
            TranslateLanguages.Hungarian -> "hu"
            TranslateLanguages.TraditionalChinese -> "zh-TW"
            TranslateLanguages.Vietnamese -> "vi"
        }
    }

    override fun supportFromLanguage(): List<TranslateLanguages> {
        return listOf(
            TranslateLanguages.Auto,
            TranslateLanguages.Chinese,
            TranslateLanguages.English,
            TranslateLanguages.Korean,
            TranslateLanguages.Japanese,
            TranslateLanguages.French,
            TranslateLanguages.Spanish,
            TranslateLanguages.Thai,
            TranslateLanguages.Arabic,
            TranslateLanguages.Russian,
            TranslateLanguages.Portuguese,
            TranslateLanguages.German,
            TranslateLanguages.Italian,
            TranslateLanguages.Greek,
            TranslateLanguages.Dutch,
            TranslateLanguages.Polish,
            TranslateLanguages.Bulgarian,
            TranslateLanguages.Estonian,
            TranslateLanguages.Danish,
            TranslateLanguages.Finnish,
            TranslateLanguages.Czech,
            TranslateLanguages.Romanian,
            TranslateLanguages.Slovenian,
            TranslateLanguages.Swedish,
            TranslateLanguages.Hungarian,
            TranslateLanguages.TraditionalChinese,
            TranslateLanguages.Vietnamese
        )
    }

    override fun supportToLanguage(): List<TranslateLanguages> {
        return listOf(
            TranslateLanguages.Chinese,
            TranslateLanguages.English,
            TranslateLanguages.Korean,
            TranslateLanguages.Japanese,
            TranslateLanguages.French,
            TranslateLanguages.Spanish,
            TranslateLanguages.Thai,
            TranslateLanguages.Arabic,
            TranslateLanguages.Russian,
            TranslateLanguages.Portuguese,
            TranslateLanguages.German,
            TranslateLanguages.Italian,
            TranslateLanguages.Greek,
            TranslateLanguages.Dutch,
            TranslateLanguages.Polish,
            TranslateLanguages.Bulgarian,
            TranslateLanguages.Estonian,
            TranslateLanguages.Danish,
            TranslateLanguages.Finnish,
            TranslateLanguages.Czech,
            TranslateLanguages.Romanian,
            TranslateLanguages.Slovenian,
            TranslateLanguages.Swedish,
            TranslateLanguages.Hungarian,
            TranslateLanguages.TraditionalChinese,
            TranslateLanguages.Vietnamese
        )
    }

    override suspend fun translate(
        text: String,
        source: TranslateLanguages,
        to: TranslateLanguages
    ): TranslateResult {
        val resp = getGoogleTranslateApiClient().translate(
            text,
            source = getLangParamMapping(source),
            target = getLangParamMapping(to)
        )
        return TranslateResult(resp.sentences.mapNotNull { it.trans })
    }

    override fun init() {
        GoogleTranslateApiHelper.createInstance()
    }
}