package com.allentom.diffusion.api.entity

data class ReactorUpscaleList(
    val upscalers: List<String>,
)

data class ReactorModelList(
    val models: List<String>,
)

data class RactorResultImage(
    val image: String,
)