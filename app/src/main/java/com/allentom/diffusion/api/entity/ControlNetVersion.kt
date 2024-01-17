package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class ControlNetVersion(
    @SerializedName("version")
    val version: String,
)