package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class ControlModelList(
    @SerializedName("model_list")
    val modelList: List<String>,
)