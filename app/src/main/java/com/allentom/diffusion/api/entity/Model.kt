package com.allentom.diffusion.api.entity

import com.allentom.diffusion.store.ModelEntity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Model(
    @SerializedName("title") val title: String,
    @SerializedName("model_name") val modelName: String,
    @SerializedName("sha256") val sha256: String,
    val entity:ModelEntity
):Serializable
