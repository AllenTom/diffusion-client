package com.allentom.diffusion.api

import com.allentom.diffusion.store.AppConfigStore
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


object ApiHelper {
    private var trustAllCerts: Array<TrustManager> = arrayOf(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )
    lateinit var instance: Retrofit
    fun createInstance(baseUrl: String) {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val client = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .apply {
                if (AppConfigStore.config.disbaleSSLVerification) {
                    sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    hostnameVerifier { hostname, session -> true }
                }
            }
            .build()
        instance = Retrofit.Builder().baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createTestInstance(baseUrl: String) {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val client = OkHttpClient.Builder()
            .apply {
                if (AppConfigStore.config.disbaleSSLVerification) {
                    sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    hostnameVerifier { hostname, session -> true }
                }
            }
            .build()
        instance = Retrofit.Builder().baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}