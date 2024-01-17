package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class ExtraSingleImageResult (
    @SerializedName("image") val image: String,
)