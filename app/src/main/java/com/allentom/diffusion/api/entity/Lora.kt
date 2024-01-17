package com.allentom.diffusion.api.entity

import com.allentom.diffusion.store.LoraPrompt
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Lora(
    @SerializedName("name") val name: String,
    @SerializedName("alias") val alias: String,
    @Expose val entity: LoraPrompt? = null
) : Serializable
