package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Option(
    @SerializedName("sd_model_checkpoint") val sdModelCheckpoint: String,
    @SerializedName("sd_vae") val sdVae: String,
    @SerializedName("interrogate_deepbooru_score_threshold") val interrogateDeepbooruScoreThreshold: Float,
):Serializable
