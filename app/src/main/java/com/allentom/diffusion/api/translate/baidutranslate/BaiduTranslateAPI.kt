package com.allentom.diffusion.api.translate.baidutranslate

import retrofit2.http.GET
import retrofit2.http.Query

interface BaiduTranslateAPI {
    @GET("/api/trans/vip/translate")
    suspend fun translate(
        @Query("q")
        text: String,
        @Query("from")
        source: String = "auto",
        @Query("to")
        target: String = "en",
        @Query("appid")
        appid: String,
        @Query("salt")
        salt: String,
        @Query("sign")
        sign: String,
    ): TranslateResult

}