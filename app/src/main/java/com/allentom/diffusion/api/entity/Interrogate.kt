package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class Interrogate(
    @SerializedName("caption")
    val caption: String,
)
