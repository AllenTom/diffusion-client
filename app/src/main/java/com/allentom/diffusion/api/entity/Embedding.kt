package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class SDWEmbeddingList(
    @SerializedName("loaded") var loaded: Map<String, Embedding>,
    @SerializedName("skipped") var skipped: Map<String, Embedding>
)

data class Embedding(
    @SerializedName("step") val step: Int? = null,
    @SerializedName("sd_checkpoint") val sdCheckpoint: String? = null,
    @SerializedName("sd_checkpoint_name") val sdCheckpointName: String? = null,
    @SerializedName("shape") val shape: Int? = null,
    @SerializedName("vectors") val vectors: Int? = null
)