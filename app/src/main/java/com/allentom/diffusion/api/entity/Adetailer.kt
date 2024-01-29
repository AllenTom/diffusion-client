package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class AdetailerModelList(
    @SerializedName("ad_model")
    val models: List<String>
)