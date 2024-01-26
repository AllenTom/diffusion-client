package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Option(
    @SerializedName("sd_model_checkpoint") val sdModelCheckpoint: String,
    @SerializedName("sd_vae") val sdVae: String,
):Serializable
