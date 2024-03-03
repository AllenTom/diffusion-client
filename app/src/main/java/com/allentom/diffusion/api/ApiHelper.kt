package com.allentom.diffusion.api

import com.allentom.diffusion.store.AppConfigStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class BasicAuthInterceptor(val token:String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val authenticatedRequest: Request = request.newBuilder()
            .header("Authorization", token).build()
        return chain.proceed(authenticatedRequest)
    }
}

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
    fun createInstance(baseUrl: String,auth: String?) {
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
                if (auth != null) {
                    addInterceptor(BasicAuthInterceptor(auth))
                }
            }
            .build()
        instance = Retrofit.Builder().baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createTestInstance(baseUrl: String, auth: String?) {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val client = OkHttpClient.Builder()
            .apply {
                if (AppConfigStore.config.disbaleSSLVerification) {
                    sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    hostnameVerifier { hostname, session -> true }
                }
                if (auth != null) {
                    addInterceptor(BasicAuthInterceptor(auth))
                }
            }
            .build()
        instance = Retrofit.Builder().baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}