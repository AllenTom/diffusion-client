package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class SDWExtension(
    val name: String,
    val remote: String,
    val branch: String,
    @SerializedName("commit_hash")
    val commitHash: String,
    val version: String,
    @SerializedName("commit_date")
    val commitDate: String,
    val enabled: Boolean,
)