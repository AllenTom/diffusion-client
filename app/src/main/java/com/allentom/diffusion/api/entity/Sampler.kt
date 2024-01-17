package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Sampler(
    @SerializedName("name") val name: String,
):Serializable
