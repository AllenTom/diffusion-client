package com.allentom.diffusion.api.translate.baidutranslate

import com.allentom.diffusion.Util
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

    override suspend fun translate(text: String): TranslateResult {
        val salt = Util.randomString(6)
        val resp = getBaiduTranslateApiClient().translate(
            text = text,
            appid = BaiduTranslateApiHelper.appid!!,
            salt = salt,
            sign = generateSign(text, salt)
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