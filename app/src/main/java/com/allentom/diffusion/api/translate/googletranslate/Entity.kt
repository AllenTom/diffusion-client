package com.allentom.diffusion.api.translate.googletranslate

import com.google.gson.annotations.SerializedName

data class TranslateResult(
    val sentences: List<Sentence>,
    val src: String,
    val confidence: Double,
    val spell: Map<String, Any>,
    @SerializedName("ld_result")
    val ldResult: LdResult,
)

data class Sentence(
    val trans: String?,
    val orig: String?,
    val backend: Long?,
    @SerializedName("model_specification")
    val modelSpecification: List<Map<String, Any>>?,
    @SerializedName("translation_engine_debug_info")
    val translationEngineDebugInfo: List<TranslationEngineDebugInfo>?,
    @SerializedName("src_translit")
    val srcTranslit: String?,
)

data class TranslationEngineDebugInfo(
    @SerializedName("model_tracking")
    val modelTracking: ModelTracking,
)

data class ModelTracking(
    @SerializedName("checkpoint_md5")
    val checkpointMd5: String,
    @SerializedName("launch_doc")
    val launchDoc: String,
)

data class LdResult(
    val srclangs: List<String>,
    @SerializedName("srclangs_confidences")
    val srclangsConfidences: List<Double>,
    @SerializedName("extended_srclangs")
    val extendedSrclangs: List<String>,
)
