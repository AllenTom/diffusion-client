package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Update
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.ui.screens.home.tabs.draw.AdetailerParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.AdetailerSlot
import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorParam

//val enabled: Boolean = false,
//val skipImg2img: Boolean = false,
//val adModel: String = "face_yolov8n.pt",
//val adPrompt: String = "",
//val adNegativePrompt: String = "",
//val adConfidence: Float = 0.3f,
//val adMaskKLargest: Long = 0,
//val adMaskMinRatio: Float = 0f,
//val adMaskMaxRatio: Float = 1f,
//val adDilateErode: Long = 32,
//val adXOffset: Long = 0,
//val adYOffset: Long = 0,
//val adMaskMergeInvert: String = "None",
//val adMaskBlur: Long = 4,
//val adDenoisingStrength: Float = 0.4f,
//val adInpaintOnlyMasked: Boolean = true,
//val adInpaintOnlyMaskedPadding: Long = 0,
//val adUseInpaintWidthHeight: Boolean = false,
//val adInpaintWidth: Long = 512,
//val adInpaintHeight: Long = 512,
//val adUseSteps: Boolean = true,
//val adSteps: Long = 28,
//val adUseCfgScale: Boolean = false,
//val adCfgScale: Float = 7.0f,
//val adUseCheckpoint: Boolean = false,
//val adCheckpoint: String = "Use same checkpoint",
//val adUseVae: Boolean = false,
//val adVae: String = "Use same VAE",
//val adUseSampler: Boolean = false,
//val adSampler: String = "DPM++ 2M Karras",
//val adUseNoiseMultiplier: Boolean = false,
//val adNoiseMultiplier: Float = 1.0f,
//val adUseClipSkip: Boolean = false,
//val adClipSkip: Long = 1,
//val adRestoreFace: Boolean = false,
//val adControlnetModel: String = "None",
//val adControlnetModule: String = "None",
//val adControlnetWeight: Float = 1.0f,
//val adControlnetGuidanceStart: Float = 0.0f,
//val adControlnetGuidanceEnd: Float = 1.0f
@Entity(tableName = "adetailer")
data class AdetailerEntity(
    @PrimaryKey(autoGenerate = true)
    var adId: Long = 0,
    var historyId: Long = 0,
    var enabled: Boolean = false,
    var skipImg2img: Boolean = false,
    var adModel: String = "",
    var adPrompt: String = "",
    var adNegativePrompt: String = "",
    var adConfidence: Float = 0.3f,
    var adMaskKLargest: Long = 0,
    var adMaskMinRatio: Float = 0f,
    var adMaskMaxRatio: Float = 1f,
    var adDilateErode: Long = 32,
    var adXOffset: Long = 0,
    var adYOffset: Long = 0,
    var adMaskMergeInvert: String = "None",
    var adMaskBlur: Long = 4,
    var adDenoisingStrength: Float = 0.4f,
    var adInpaintOnlyMasked: Boolean = true,
    var adInpaintOnlyMaskedPadding: Long = 0,
    var adUseInpaintWidthHeight: Boolean = false,
    var adInpaintWidth: Long = 512,
    var adInpaintHeight: Long = 512,
    var adUseSteps: Boolean = true,
    var adSteps: Long = 28,
    var adUseCfgScale: Boolean = false,
    var adCfgScale: Float = 7.0f,
    var adUseCheckpoint: Boolean = false,
    var adCheckpoint: String = "Use same checkpoint",
    var adUseVae: Boolean = false,
    var adVae: String = "Use same VAE",
    var adUseSampler: Boolean = false,
    var adSampler: String = "DPM++ 2M Karras",
    var adUseNoiseMultiplier: Boolean = false,
    var adNoiseMultiplier: Float = 1.0f,
    var adUseClipSkip: Boolean = false,
    var adClipSkip: Long = 1,
    var adRestoreFace: Boolean = false,
    var adControlnetModel: String = "None",
    var adControlnetModule: String = "None",
    var adControlnetWeight: Float = 1.0f,
    var adControlnetGuidanceStart: Float = 0.0f,
    var adControlnetGuidanceEnd: Float = 1.0f

)

@Dao
interface AdetailerDao {
    @Insert
    fun insert(adetailerEntity: AdetailerEntity)

    @Update
    fun update(adetailerEntity: AdetailerEntity)
}

fun SaveHistory.saveAdetailer(context: Context) {
    val database = AppDatabaseHelper.getDatabase(context)
    adetailerParam?.let {
        it.slots.forEach {
            database.adetailerDao().insert(
                AdetailerEntity(
                    historyId = id,
                    enabled = adetailerParam.enabled,
                    skipImg2img = adetailerParam.skipImg2img,
                    adModel = it.adModel,
                    adPrompt = it.adPrompt,
                    adNegativePrompt = it.adNegativePrompt,
                    adConfidence = it.adConfidence,
                    adMaskKLargest = it.adMaskKLargest,
                    adMaskMinRatio = it.adMaskMinRatio,
                    adMaskMaxRatio = it.adMaskMaxRatio,
                    adDilateErode = it.adDilateErode,
                    adXOffset = it.adXOffset,
                    adYOffset = it.adYOffset,
                    adMaskMergeInvert = it.adMaskMergeInvert,
                    adMaskBlur = it.adMaskBlur,
                    adDenoisingStrength = it.adDenoisingStrength,
                    adInpaintOnlyMasked = it.adInpaintOnlyMasked,
                    adInpaintOnlyMaskedPadding = it.adInpaintOnlyMaskedPadding,
                    adUseInpaintWidthHeight = it.adUseInpaintWidthHeight,
                    adInpaintWidth = it.adInpaintWidth,
                    adInpaintHeight = it.adInpaintHeight,
                    adUseSteps = it.adUseSteps,
                    adSteps = it.adSteps,
                    adUseCfgScale = it.adUseCfgScale,
                    adCfgScale = it.adCfgScale,
                    adUseCheckpoint = it.adUseCheckpoint,
                    adCheckpoint = it.adCheckpoint,
                    adUseVae = it.adUseVae,
                    adVae = it.adVae,
                    adUseSampler = it.adUseSampler,
                    adSampler = it.adSampler,
                    adUseNoiseMultiplier = it.adUseNoiseMultiplier,
                    adNoiseMultiplier = it.adNoiseMultiplier,
                    adUseClipSkip = it.adUseClipSkip,
                    adClipSkip = it.adClipSkip,
                    adRestoreFace = it.adRestoreFace,
                    adControlnetModel = it.adControlnetModel,
                    adControlnetModule = it.adControlnetModule,
                    adControlnetWeight = it.adControlnetWeight,
                    adControlnetGuidanceStart = it.adControlnetGuidanceStart,
                    adControlnetGuidanceEnd = it.adControlnetGuidanceEnd,
                )
            )
        }
    }
}

fun HistoryWithRelation.toAdetailerParam(): AdetailerParam? {
    if (adetailerEntityList.isNullOrEmpty()) {
        return null
    }

    val isEnable = adetailerEntityList.all { it.enabled }
    val isSkipImg2img = adetailerEntityList.all { it.skipImg2img }
    return AdetailerParam(
        enabled = isEnable,
        skipImg2img = isSkipImg2img,
        slots = adetailerEntityList.map {
            AdetailerSlot(
                adModel = it.adModel,
                adPrompt = it.adPrompt,
                adNegativePrompt = it.adNegativePrompt,
                adConfidence = it.adConfidence,
                adMaskKLargest = it.adMaskKLargest,
                adMaskMinRatio = it.adMaskMinRatio,
                adMaskMaxRatio = it.adMaskMaxRatio,
                adDilateErode = it.adDilateErode,
                adXOffset = it.adXOffset,
                adYOffset = it.adYOffset,
                adMaskMergeInvert = it.adMaskMergeInvert,
                adMaskBlur = it.adMaskBlur,
                adDenoisingStrength = it.adDenoisingStrength,
                adInpaintOnlyMasked = it.adInpaintOnlyMasked,
                adInpaintOnlyMaskedPadding = it.adInpaintOnlyMaskedPadding,
                adUseInpaintWidthHeight = it.adUseInpaintWidthHeight,
                adInpaintWidth = it.adInpaintWidth,
                adInpaintHeight = it.adInpaintHeight,
                adUseSteps = it.adUseSteps,
                adSteps = it.adSteps,
                adUseCfgScale = it.adUseCfgScale,
                adCfgScale = it.adCfgScale,
                adUseCheckpoint = it.adUseCheckpoint,
            )
        },
    )
}