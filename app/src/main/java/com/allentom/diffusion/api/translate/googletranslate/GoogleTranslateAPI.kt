package com.allentom.diffusion.api.translate.googletranslate

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleTranslateAPI {
    @FormUrlEncoded
    @POST("translate_a/single")
    suspend fun translate(
        @Field("q")
        text: String,
        @Field("sl")
        source: String = "auto",
        @Field("tl")
        target: String = "en",
        @Query("client") client: String = "at",
        @Query("dt") dt: String = "t",
        @Query("dj") dj: Int = 1,
    ): TranslateResult
}