package com.allentom.diffusion.api.translate.baidutranslate

import com.google.gson.annotations.SerializedName

data class TranslateResult(
    val from: String,
    val to: String,
    @SerializedName("trans_result")
    val transResult: List<Sentence>,
)

data class Sentence(
    val src: String,
    val dst: String,
)
