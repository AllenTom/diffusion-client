package com.allentom.diffusion.api.translate.baidutranslate

import com.allentom.diffusion.Util
import com.allentom.diffusion.api.translate.TranslateLanguages
import com.allentom.diffusion.api.translate.TranslateResult
import com.allentom.diffusion.api.translate.Translator
import com.allentom.diffusion.store.AppConfigStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest

object BaiduTranslateApiHelper {
    val BASE_URL = "https://fanyi-api.baidu.com/"
    var appid: String? = null
    var secret: String? = null

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

fun getBaiduTranslateApiClient(): BaiduTranslateAPI {
    return BaiduTranslateApiHelper.instance.create(BaiduTranslateAPI::class.java)
}

class BaiduTranslator : Translator {
    private fun getLangParamMapping(lang: TranslateLanguages): String {
        return when (lang) {
            TranslateLanguages.Auto -> "auto"
            TranslateLanguages.Chinese -> "zh"
            TranslateLanguages.English -> "en"
            TranslateLanguages.Korean -> "kor"
            TranslateLanguages.Japanese -> "jp"
            TranslateLanguages.French -> "fra"
            TranslateLanguages.Spanish -> "spa"
            TranslateLanguages.Thai -> "th"
            TranslateLanguages.Arabic -> "ara"
            TranslateLanguages.Russian -> "ru"
            TranslateLanguages.Portuguese -> "pt"
            TranslateLanguages.German -> "de"
            TranslateLanguages.Italian -> "it"
            TranslateLanguages.Greek -> "el"
            TranslateLanguages.Dutch -> "nl"
            TranslateLanguages.Polish -> "pl"
            TranslateLanguages.Bulgarian -> "bul"
            TranslateLanguages.Estonian -> "est"
            TranslateLanguages.Danish -> "dan"
            TranslateLanguages.Finnish -> "fin"
            TranslateLanguages.Czech -> "cs"
            TranslateLanguages.Romanian -> "rom"
            TranslateLanguages.Slovenian -> "slo"
            TranslateLanguages.Swedish -> "swe"
            TranslateLanguages.Hungarian -> "hu"
            TranslateLanguages.TraditionalChinese -> "cht"
            TranslateLanguages.Vietnamese -> "vie"
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

    private fun generateSign(text: String, salt: String): String {
        val sign = BaiduTranslateApiHelper.appid + text + salt + BaiduTranslateApiHelper.secret
        // MD5加密
        val md = MessageDigest.getInstance("MD5")
        md.update(sign.toByteArray())
        val byteBuffer = md.digest()
        return byteBuffer.joinToString("") {
            it.toInt().and(0xff).toString(16).padStart(2, '0')
        }
    }

    override suspend fun translate(
        text: String,
        source: TranslateLanguages,
        to: TranslateLanguages
    ): TranslateResult {
        val salt = Util.randomString(6)
        val resp = getBaiduTranslateApiClient().translate(
            text = text,
            appid = BaiduTranslateApiHelper.appid!!,
            salt = salt,
            sign = generateSign(text, salt),
            source = getLangParamMapping(source),
            target = getLangParamMapping(to)
        )
        return TranslateResult(resp.transResult.map {
            it.dst
        })
    }

    override fun init() {
        with(BaiduTranslateApiHelper) {
            appid = AppConfigStore.config.baiduTranslateAppId
            secret = AppConfigStore.config.baiduTranslateSecretKey
            createInstance()
        }
    }

}