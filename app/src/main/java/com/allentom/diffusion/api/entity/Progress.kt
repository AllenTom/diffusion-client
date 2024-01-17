package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Progress (
    @SerializedName("progress") val progress: Float,
    @SerializedName("current_image") val currentImage: String,
    @SerializedName("state") val state: ProgressState,
):Serializable

data class ProgressState(
    @SerializedName("skipped") val skipped: Boolean,
    @SerializedName("interrupted") val interrupted: Boolean,
    @SerializedName("job") val job: String,
    @SerializedName("job_count") val jobCount: Int,
    @SerializedName("job_timestamp") val jobTimestamp: String,
    @SerializedName("job_no") val jobNo: Int,
    @SerializedName("sampling_step") val samplingStep: Int,
    @SerializedName("sampling_steps") val samplingSteps: Int,
):Serializable