package com.allentom.diffusion.api.civitai.entities

import com.google.gson.annotations.SerializedName
import java.io.Serializable
data class CivitaiModel(
    val id: Long,
    val name: String,
    val description: String?,
    val type: String,
    val poi: Boolean,
    val nsfw: Boolean,
    val allowNoCredit: Boolean,
    val allowCommercialUse: Any,
    val allowDerivatives: Boolean,
    val allowDifferentLicense: Boolean,
    val stats: Stats,
    val creator: Creator,
    val tags: List<String>,
    val modelVersions: List<CivitaiModelVersion>,
)
data class CivitaiModelVersion(
    val id: Long,
    val modelId: Long,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val status: String,
    val publishedAt: String,
    val baseModel: String,
    val baseModelType: String,
    val earlyAccessTimeFrame: Long,
    val description: String?,
    val stats: Stats,
    val model: Model,
    val files: List<File>,
    val images: List<CivitaiImageItem>,
    val downloadUrl: String,
    val trainedWords: List<String> = emptyList(),
):Serializable

data class Stats(
    val downloadCount: Long,
    val ratingCount: Long,
    val rating: Float,

    val favoriteCount: Long?,
    val commentCount: Long?,
    val tippedAmountCount: Long?,
):Serializable
data class Creator(
    val username: String,
    val image: String,
)
data class Model(
    val name: String,
    val type: String,
    val nsfw: Boolean,
    val poi: Boolean,
):Serializable

data class File(
    val id: Long,
    @SerializedName("sizeKB")
    val sizeKb: Double,
    val name: String,
    val type: String,
    val metadata: FileMetadata,
    val pickleScanResult: String,
    val pickleScanMessage: String,
    val virusScanResult: String,
    val virusScanMessage: Any?,
    val scannedAt: String,
    val hashes: Hashes,
    val primary: Boolean,
    val downloadUrl: String,
):Serializable

data class FileMetadata(
    val fp: String,
    val size: String,
    val format: String,
):Serializable

data class Hashes(
    @SerializedName("AutoV1")
    val autoV1: String,
    @SerializedName("AutoV2")
    val autoV2: String,
    @SerializedName("SHA256")
    val sha256: String,
    @SerializedName("CRC32")
    val crc32: String,
    @SerializedName("BLAKE3")
    val blake3: String,
):Serializable

data class Image(
    val url: String,
    val nsfw: String,
    val width: Long,
    val height: Long,
    val hash: String,
    val type: String,
    val metadata: ImageMetadata,
    val meta: Meta?,
)

data class ImageMetadata(
    val hash: String,
    val width: Long,
    val height: Long,
)

data class Meta(
    @SerializedName("Size")
    val size: String?,
    @SerializedName("Model")
    val model: String?,
    @SerializedName("Clip skip")
    val clipSkip: String,
    val resources: List<Resource>,
    @SerializedName("Model hash")
    val modelHash: String,
    @SerializedName("Hires steps")
    val hiresSteps: String,
    @SerializedName("Hires upscale")
    val hiresUpscale: String,
    @SerializedName("Hires upscaler")
    val hiresUpscaler: String,
    @SerializedName("Denoising strength")
    val denoisingStrength: String,
    val seed: Long?,
    val steps: Long?,
    val prompt: String?,
    val sampler: String?,
    val cfgScale: Float?,
    val negativePrompt: String?,
    @SerializedName("Variation seed")
    val variationSeed: String?,
    @SerializedName("Variation seed strength")
    val variationSeedStrength: String?,
)

data class Resource(
    val hash: String?,
    val name: String,
    val type: String,
    val weight: Double?,
)