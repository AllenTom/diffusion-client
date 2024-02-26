package com.allentom.diffusion.service

import android.util.Log
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.AdeatilerWrapper
import com.allentom.diffusion.api.AdetailerArg
import com.allentom.diffusion.api.AdetailerSlotArg
import com.allentom.diffusion.api.AlwaysonScripts
import com.allentom.diffusion.api.ControlNetArg
import com.allentom.diffusion.api.ControlNetWrapper
import com.allentom.diffusion.api.Img2ImgRequest
import com.allentom.diffusion.api.OverrideSetting
import com.allentom.diffusion.api.ReactorParamRequest
import com.allentom.diffusion.api.ReactorWrapper
import com.allentom.diffusion.api.RegionalPrompterParam
import com.allentom.diffusion.api.RegionalPrompterWrapper
import com.allentom.diffusion.api.Txt2ImgRequest
import com.allentom.diffusion.api.entity.throwApiExceptionIfNotSuccessful
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.store.history.SaveHrParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.AdetailerParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.ControlNetParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.RegionPromptParam

fun applyControlParams(
    alwaysonScripts: AlwaysonScripts,
    controlNetParam: ControlNetParam?
): AlwaysonScripts {
    if (controlNetParam != null) {
        alwaysonScripts.controlNet = ControlNetWrapper(
            args = controlNetParam.slots.map {
                ControlNetArg(
                    enabled = it.enabled,
                    guidanceStart = it.guidanceStart,
                    guidanceEnd = it.guidanceEnd,
                    controlMode = it.controlMode,
                    weight = it.weight,
                    model = it.model ?: "",
                    inputImage = it.inputImage ?: "",
                    inputImagePath = it.inputImagePath
                )
            }
        )
    }
    return alwaysonScripts
}

fun applyRegionParams(
    alwaysonScripts: AlwaysonScripts,
    regionPromptParam: RegionPromptParam?
): AlwaysonScripts {
    if (regionPromptParam != null && regionPromptParam.regionCount > 1 && regionPromptParam.enable) {
        var param = RegionalPrompterParam(
            active = true,
            ratios = regionPromptParam.dividerText,
            useCommon = true
        )
        alwaysonScripts.regionalPrompter = RegionalPrompterWrapper(
            args = param.toParamArray()
        )
    }
    return alwaysonScripts
}

fun applyReactorParam(
    alwaysonScripts: AlwaysonScripts,
    reactorParam: ReactorParam?
): AlwaysonScripts {
    if (reactorParam != null && reactorParam.enabled) {
        reactorParam.singleImageResultFilename
        val args = ReactorParamRequest(
            enable = true,
            singleSourceImage = Util.generateDataImageString(
                reactorParam.singleImageResultFilename!!,
                reactorParam.singleImageResult!!.replace(
                    "\n",
                    ""
                )
            ),
            genderDetectionSource = reactorParam.genderDetectionSource,
            genderDetectionTarget = reactorParam.genderDetectionTarget,
            restoreFace = reactorParam.restoreFace,
            restoreFaceVisibility = reactorParam.restoreFaceVisibility,
            codeFormerWeightFidelity = reactorParam.codeFormerWeightFidelity,
            postprocessingOrder = reactorParam.postprocessingOrder,
            model = reactorParam.model ?: "inswapper_128.onnx",
        )
        alwaysonScripts.reactor = ReactorWrapper(
            args = args.toParamArray()
        )
    }
    return alwaysonScripts
}

fun applyAdetailerParam(
    alwaysonScripts: AlwaysonScripts,
    adetailerParam: AdetailerParam?
): AlwaysonScripts {
    if (adetailerParam != null && adetailerParam.enabled) {
        val slots = adetailerParam.slots.map { slotParam ->
            AdetailerSlotArg(
                adModel = slotParam.adModel,
                adPrompt = slotParam.adPrompt,
                adNegativePrompt = slotParam.adNegativePrompt,
                adConfidence = slotParam.adConfidence,
                adMaskKLargest = slotParam.adMaskKLargest,
                adMaskMinRatio = slotParam.adMaskMinRatio,
                adMaskMaxRatio = slotParam.adMaskMaxRatio,
                adDilateErode = slotParam.adDilateErode,
                adXOffset = slotParam.adXOffset,
                adYOffset = slotParam.adYOffset,
                adMaskMergeInvert = slotParam.adMaskMergeInvert,
                adMaskBlur = slotParam.adMaskBlur,
                adDenoisingStrength = slotParam.adDenoisingStrength,
                adInpaintOnlyMasked = slotParam.adInpaintOnlyMasked,
                adInpaintOnlyMaskedPadding = slotParam.adInpaintOnlyMaskedPadding,
                adUseInpaintWidthHeight = slotParam.adUseInpaintWidthHeight,
                adInpaintWidth = slotParam.adInpaintWidth,
                adInpaintHeight = slotParam.adInpaintHeight,
                adUseSteps = slotParam.adUseSteps,
                adSteps = slotParam.adSteps,
                adUseCfgScale = slotParam.adUseCfgScale,
                adCfgScale = slotParam.adCfgScale,
                adUseCheckpoint = slotParam.adUseCheckpoint,
                adCheckpoint = slotParam.adCheckpoint,
                adUseVae = slotParam.adUseVae,
                adVae = slotParam.adVae,
                adUseSampler = slotParam.adUseSampler,
                adSampler = slotParam.adSampler,
                adUseNoiseMultiplier = slotParam.adUseNoiseMultiplier,
                adNoiseMultiplier = slotParam.adNoiseMultiplier,
                adUseClipSkip = slotParam.adUseClipSkip,
                adClipSkip = slotParam.adClipSkip,
                adRestoreFace = slotParam.adRestoreFace,
                adControlnetModel = slotParam.adControlnetModel,
                adControlnetModule = slotParam.adControlnetModule,
                adControlnetWeight = slotParam.adControlnetWeight,
                adControlnetGuidanceStart = slotParam.adControlnetGuidanceStart,
                adControlnetGuidanceEnd = slotParam.adControlnetGuidanceEnd
            )
        }
        val args = AdetailerArg(
            enabled = true,
            skipImg2img = false,
            slot = slots
        )
        alwaysonScripts.adetailer = AdeatilerWrapper(
            args = args.toParamArray()
        )
    }
    return alwaysonScripts
}

data class Text2ImageParam(
    val prompt: String,
    val negativePrompt: String,
    val width: Int,
    val height: Int,
    val steps: Int,
    val samplerName: String,
    val nIter: Int,
    val cfgScale: Float,
    val seed: Long,
    val hiresFixParam: SaveHrParam? = null,
    val controlNetParam: ControlNetParam? = null,
    val regionPromptParam: RegionPromptParam? = null,
    val refinerModel: String? = null,
    val refinerSwitchAt: Float? = null,
    val reactorParam: ReactorParam? = null,
    val adetailerParam: AdetailerParam? = null,
    val overrideSetting: OverrideSetting? = null
)

suspend fun text2Image(
    param: Text2ImageParam
): String? {
    with(param) {
        var alwaysonScripts = AlwaysonScripts()
        alwaysonScripts = applyControlParams(alwaysonScripts, controlNetParam)
        alwaysonScripts = applyRegionParams(alwaysonScripts, regionPromptParam)
        alwaysonScripts = applyReactorParam(alwaysonScripts, reactorParam)
        alwaysonScripts = applyAdetailerParam(alwaysonScripts, adetailerParam)

        Log.d("prompt", "prompt: $prompt")
        Log.d("alwayson", DrawViewModel.gson.toJson(alwaysonScripts))

        var request = Txt2ImgRequest(
            prompt = prompt,
            negativePrompt = negativePrompt,
            width = width,
            height = height,
            steps = steps,
            samplerName = samplerName,
            nIterate = 1,
            cfgScale = cfgScale.toInt(),
            seed = seed,
            alwaysonScripts = alwaysonScripts,
            refinerCheckpoint = refinerModel,
            refinerSwitchAt = refinerSwitchAt,
            overrideSetting = overrideSetting,
        )

        hiresFixParam?.let {
            request = request.copy(
                enableHr = it.enableScale,
                hrScale = if (it.hrMode == "scale") it.hrScale else 1f,
                denoisingStrength = it.hrDenosingStrength,
                hrUpscaler = it.hrUpscaler,
                hrSecondPassSteps = it.hrSteps,
                hrResizeX = if (it.hrMode == "resize") it.hrResizeWidth else 0,
                hrResizeY = if (it.hrMode == "resize") it.hrResizeHeight else 0,
            )
        }

        val list = getApiClient().txt2img(
            request = request
        )
        val body = list.body()
        list.throwApiExceptionIfNotSuccessful()
        if (list.isSuccessful && body != null && body.images.isNotEmpty()) {
            return body.images.first()
        }
        return null
    }
}

data class Img2imgGenerateParam(
    val prompt: String,
    val negativePrompt: String,
    val width: Int,
    val height: Int,
    val steps: Int,
    val samplerName: String,
    val cfgScale: Float,
    val seed: Long,
    val imgBase64: String?,
    val resizeMode: Int?,
    val denoisingStrength: Float?,
    val scaleBy: Float?,
    val mask: String? = null,
    val inpaintingMaskInvert: Int = 0,
    val maskBlur: Float = 4f,
    val inpaintingFill: Int = 0,
    val inpaintFullRes: Int = 1,
    val inpaintFullResPadding: Int = 32,
    val regionPromptParam: RegionPromptParam? = null,
    val reactorParam: ReactorParam? = null,
    val adetailerParam: AdetailerParam? = null
)

suspend fun img2Image(
    param: Img2imgGenerateParam
): String? {
    with(param) {
        var alwaysonScripts = AlwaysonScripts()
        alwaysonScripts = applyAdetailerParam(alwaysonScripts, adetailerParam)
        alwaysonScripts = applyRegionParams(alwaysonScripts, regionPromptParam)
        alwaysonScripts = applyReactorParam(alwaysonScripts, reactorParam)
        val request = Img2ImgRequest(
            prompt = prompt,
            negative_prompt = negativePrompt,
            width = width,
            height = height,
            steps = steps,
            sampler_name = samplerName,
            n_iter = 1,
            cfg_scale = cfgScale.toInt(),
            seed = seed,
            init_images = listOf(imgBase64 ?: ""),
            resize_mode = resizeMode ?: 0,
            image_cfg_scale = cfgScale,
            denoising_strength = denoisingStrength ?: 0.75f,
            scale_by = scaleBy ?: 1f,
            mask = mask?.takeIf { it.isNotBlank() },
            inpainting_mask_invert = inpaintingMaskInvert,
            mask_blur = maskBlur,
            inpainting_fill = inpaintingFill,
            inpaint_full_res = inpaintFullRes,
            inpaint_full_res_padding = inpaintFullResPadding,
            alwayson_scripts = alwaysonScripts
        )
        val list = getApiClient().img2img(
            request = request
        )
        val body = list.body()
        if (list.isSuccessful && body != null && body.images.isNotEmpty()) {
            return body.images.first()
        }
        return null
    }
}