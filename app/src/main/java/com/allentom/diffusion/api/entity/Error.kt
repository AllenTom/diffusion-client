package com.allentom.diffusion.api.entity

data class SDWError(
    val error: String,
    val detail: String,
    val body: String,
    val errors: String
)