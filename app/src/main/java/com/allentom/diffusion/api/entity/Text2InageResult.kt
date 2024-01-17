package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class Text2ImageResult (
    @SerializedName("images") val images: List<String>,
)

data class Img2ImgResult (
    @SerializedName("images") val images: List<String>,
)