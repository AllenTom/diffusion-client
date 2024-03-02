package com.allentom.diffusion.api.civitai.entities

import com.google.gson.annotations.SerializedName

data class CivitaiImageListResult(
    val items: List<CivitaiImageItem>,
    val metadata: ImageMetadata,
)

data class CivitaiImageItem(
    val id: Long,
    val url: String,
    val hash: String,
    val width: Long,
    val height: Long,
    val nsfwLevel: String,
    val nsfw: Boolean,
    val createdAt: String,
    val postId: Long,
    val stats: ImageStats,
    val meta: ImageMeta?,
    val username: String,
)

data class ImageStats(
    val cryCount: Long,
    val laughCount: Long,
    val likeCount: Long,
    val dislikeCount: Long,
    val heartCount: Long,
    val commentCount: Long,
)

data class ImageMeta(
    val seed: Long,
    @SerializedName("Model")
    val model: String?,
    val steps: Long,
    val prompt: String,
    val sampler: String,
    val cfgScale: Float,
    @SerializedName("Clip Skip")
    val clipSkip: String?,
    val resources: List<ImageResource>?,
    val negativePrompt: String?,
    val civitaiResources: List<CivitaiResource>?,
    @SerializedName("ENSD")
    val ensd: String?,
    @SerializedName("Size")
    val size: String?,
    val hashes: ImageHashes?,
    @SerializedName("clipSkip")
    val clipSkip2: Long?,
    @SerializedName("Model hash")
    val modelHash: String?,
    @SerializedName("Hires steps")
    val hiresSteps: String?,
    @SerializedName("Hires upscale")
    val hiresUpscale: String?,
    @SerializedName("Hires upscaler")
    val hiresUpscaler: String?,
    @SerializedName("Denoising strength")
    val denoisingStrength: String?,
    @SerializedName("VAE")
    val vae: String?,
    @SerializedName("Version")
    val version: String?,
    @SerializedName("VAE hash")
    val vaeHash: String?,
    @SerializedName("Pad conds")
    val padConds: String?,
    @SerializedName("RealDownblouseXL2")
    val realDownblouseXl2: String?,
    @SerializedName("RNG")
    val rng: String?,
    @SerializedName("Ilulu-10")
    val ilulu10: String?,
    @SerializedName("bad-artist")
    val badArtist: String?,
    @SerializedName("AuroraNegative")
    val auroraNegative: String?,
    @SerializedName("EasyNegativeV2")
    val easyNegativeV2: String?,
    @SerializedName("ADetailer model")
    val adetailerModel: String?,
    @SerializedName("ADetailer version")
    val adetailerVersion: String?,
    @SerializedName("ADetailer mask blur")
    val adetailerMaskBlur: String?,
    @SerializedName("ADetailer confidence")
    val adetailerConfidence: String?,
    @SerializedName("ADetailer dilate erode")
    val adetailerDilateErode: String?,
    @SerializedName("ADetailer inpaint padding")
    val adetailerInpaintPadding: String?,
    @SerializedName("ADetailer denoising strength")
    val adetailerDenoisingStrength: String?,
    @SerializedName("ADetailer inpaint only masked")
    val adetailerInpaintOnlyMasked: String?,
)

data class ImageResource(
    val hash: String?,
    val name: String,
    val type: String,
    val weight: Float?,
)

data class CivitaiResource(
    val type: String?,
    val modelVersionId: Long?,
)

data class ImageHashes(
    val model: String,
    val vae: String?,
    @SerializedName("lora:RealDownblouseXL2")
    val loraRealDownblouseXl2: String?,
)

