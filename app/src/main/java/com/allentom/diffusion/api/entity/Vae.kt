package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class Vae (
    @SerializedName("model_name")
    val modelName: String,
)