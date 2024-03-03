package com.allentom.diffusion.api

import com.allentom.diffusion.api.entity.AdetailerModelList
import com.allentom.diffusion.api.entity.ControlModelList
import com.allentom.diffusion.api.entity.ControlNetDetectResult
import com.allentom.diffusion.api.entity.ControlNetModule
import com.allentom.diffusion.api.entity.ControlNetVersion
import com.allentom.diffusion.api.entity.ControlType
import com.allentom.diffusion.api.entity.ControlTypesResult
import com.allentom.diffusion.api.entity.ExtraSingleImageResult
import com.allentom.diffusion.api.entity.HelperPing
import com.allentom.diffusion.api.entity.Img2ImgResult
import com.allentom.diffusion.api.entity.Interrogate
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.api.entity.Model
import com.allentom.diffusion.api.entity.ModelHash
import com.allentom.diffusion.api.entity.Option
import com.allentom.diffusion.api.entity.Progress
import com.allentom.diffusion.api.entity.RactorResultImage
import com.allentom.diffusion.api.entity.ReactorModelList
import com.allentom.diffusion.api.entity.ReactorUpscaleList
import com.allentom.diffusion.api.entity.SDWEmbeddingList
import com.allentom.diffusion.api.entity.SDWExtension
import com.allentom.diffusion.api.entity.Sampler
import com.allentom.diffusion.api.entity.Text2ImageResult
import com.allentom.diffusion.api.entity.Upscale
import com.allentom.diffusion.api.entity.Vae
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

//"enabled": true,
//"processor_res": 64,
//"threshold_a": 64,
//"threshold_b": 64,
//"guidance_start": 0.0,
//"guidance_end": 1.0,
//"control_mode": 2,
//"weight": 1.0,
//"model": "control_sd15_depth [fef5e48e]",
data class ControlNetArg(
    @SerializedName("enabled")
    val enabled: Boolean = false,
    @SerializedName("processor_res")
    val processorRes: Float = 64f,
    @SerializedName("threshold_a")
    val thresholdA: Float = 64f,
    @SerializedName("threshold_b")
    val thresholdB: Float = 64f,
    @SerializedName("guidance_start")
    val guidanceStart: Float = 0f,
    @SerializedName("guidance_end")
    val guidanceEnd: Float = 1f,
    @SerializedName("control_mode")
    val controlMode: Int = 0,
    @SerializedName("weight")
    val weight: Float = 1f,
    @SerializedName("input_image")
    val inputImage: String = "",
    @SerializedName("model")
    val model: String = "",
    @SerializedName("module")
    val module: String = "none",
    @SerializedName("resize_mode")
    val resizeMode: Int = 1,
    @Expose
    val inputImagePath: String? = null,
    @Expose
    val historyId: Long = 0,
    @Expose
    val controlNetId: Long = 0,
)

//1	Active	True, False	Bool	False
//2	debug	True, False	Bool	False
//3	Mode	Matrix, Mask, Prompt	Text	Matrix
//4	Mode (Matrix)	Horizontal, Vertical, Colums, Rows	Text	Columns
//5	Mode (Mask)	Mask	Text	Mask
//6	Mode (Prompt)	Prompt, Prompt-Ex	Text	Prompt
//7	Ratios		Text	1,1,1
//8	Base Ratios		Text	0
//9	Use Base	True, False	Bool	False
//10	Use Common	True, False	Bool	False
//11	Use Neg-Common	True, False	Bool	False
//12	Calcmode	Attention, Latent	Text	Attention
//13	Not Change AND	True, False	Bool	False
//14	LoRA Textencoder		Text	0
//15	LoRA U-Net		Text	0
//16	Threshold		Text	0
//17	Mask		Text
//18	LoRA stop step		Text	0
//19	LoRA Hires stop step		Text	0
//20	flip	True, False	Bool	False
data class RegionalPrompterParam(
    @SerializedName("active")
    val active: Boolean = false,
    @SerializedName("debug")
    val debug: Boolean = false,
    @SerializedName("mode")
    val mode: String = "Matrix",
    @SerializedName("mode_matrix")
    val modeMatrix: String = "Columns",
    @SerializedName("mode_mask")
    val modeMask: String = "Mask",
    @SerializedName("mode_prompt")
    val modePrompt: String = "Prompt",
    @SerializedName("ratios")
    val ratios: String = "1,1,1",
    @SerializedName("base_ratios")
    val baseRatios: String = "0.2",
    @SerializedName("use_base")
    val useBase: Boolean = false,
    @SerializedName("use_common")
    val useCommon: Boolean = false,
    @SerializedName("use_neg_common")
    val useNegCommon: Boolean = false,
    @SerializedName("calcmode")
    val calcmode: String = "Attention",
    @SerializedName("not_change_and")
    val notChangeAnd: Boolean = false,
    @SerializedName("lora_textencoder")
    val loraTextencoder: String = "0",
    @SerializedName("lora_unet")
    val loraUnet: String = "0",
    @SerializedName("threshold")
    val threshold: String = "0",
    @SerializedName("mask")
    val mask: String = "",
    @SerializedName("lora_stop_step")
    val loraStopStep: String = "0",
    @SerializedName("lora_hires_stop_step")
    val loraHiresStopStep: String = "0",
    @SerializedName("flip")
    val flip: Boolean = false,
) {
    fun toParamArray(): List<Any> {
        return listOf(
            active,
            debug,
            mode,
            modeMatrix,
            modeMask,
            modePrompt,
            ratios,
            baseRatios,
            useBase,
            useCommon,
            useNegCommon,
            calcmode,
            notChangeAnd,
            loraTextencoder,
            loraUnet,
            threshold,
            mask,
            loraStopStep,
            loraHiresStopStep,
            flip,
        )
    }
}

//img_base64, #0
//True, #1 Enable ReActor
//'0', #2 Comma separated face number(s) from swap-source image
//'0', #3 Comma separated face number(s) for target image (result)
//'C:\stable-diffusion-webui\models\insightface\inswapper_128.onnx', #4 model path
//'CodeFormer', #4 Restore Face: None; CodeFormer; GFPGAN
//1, #5 Restore visibility value
//True, #7 Restore face -> Upscale
//'4x_NMKD-Superscale-SP_178000_G', #8 Upscaler (type 'None' if doesn't need), see full list here: http://127.0.0.1:7860/sdapi/v1/script-info -> reactor -> sec.8
//1.5, #9 Upscaler scale value
//1, #10 Upscaler visibility (if scale = 1)
//False, #11 Swap in source image
//True, #12 Swap in generated image
//1, #13 Console Log Level (0 - min, 1 - med or 2 - max)
//0, #14 Gender Detection (Source) (0 - No, 1 - Female Only, 2 - Male Only)
//0, #15 Gender Detection (Target) (0 - No, 1 - Female Only, 2 - Male Only)
//False, #16 Save the original image(s) made before swapping
//0.8, #17 CodeFormer Weight (0 = maximum effect, 1 = minimum effect), 0.5 - by default
//False, #18 Source Image Hash Check, True - by default
//False, #19 Target Image Hash Check, False - by default
//"CUDA", #20 CPU or CUDA (if you have it), CPU - by default
//True, #21 Face Mask Correction
//1, #22 Select Source, 0 - Image, 1 - Face Model, 2 - Source Folder
//"elena.safetensors", #23 Filename of the face model (from "models/reactor/faces"), e.g. elena.safetensors, don't forger to set #22 to 1
//"C:\PATH_TO_FACES_IMAGES", #24 The path to the folder containing source faces images, don't forger to set #22 to 2
//None, #25 skip it for API
//True, #26 Randomly select an image from the path
data class ReactorParamRequest(
    // #0
    val singleSourceImage: String = "",
    // #1
    val enable: Boolean = false,
    // #2
    val sourceImageAbove: String = "0",
    // #3
    val targetImageResult: String = "0",
    // #4
    val model: String = "inswapper_128.onnx",
    // #5 Restore Face
    val restoreFace: String = "CodeFormer",
    // #6 Restore Face Visibility
    val restoreFaceVisibility: Float = 1f,
    // #7 Postprocessing Order
    val postprocessingOrder: Boolean = true,
    // #8 Upscaler
    val upscaler: String = "None",
    // #9 Scale by
    val scaleBy: Float = 1f,
    // #10 Upscaler Visibility (if scale = 1)
    val upscalerVisibility: Float = 1f,
    // #11 Swap in source image
    val swapInSourceImage: Boolean = false,
    // #12 Swap in generated image
    val swapInGeneratedImage: Boolean = true,
    // #13 Console Log Level
    val consoleLogLevel: Int = 2,
    // #14 Gender Detection (Source)
    val genderDetectionSource: Int = 1,
    // #15 Gender Detection (Target)
    val genderDetectionTarget: Int = 1,
    // Save Original (Swap in generated only)
    val saveOriginalSwapInGeneratedOnly: Boolean = false,
    // CodeFormer Weight (Fidelity)
    val codeFormerWeightFidelity: Float = 0.8f,
    // Source Image Hash Check
    val sourceImageHashCheck: Boolean = false,
    // Target Image Hash Check
    val targetImageHashCheck: Boolean = false,
    //Execution Provider
    val executionProvider: String = "CPU",
    //Face Mask Correction
    val faceMaskCorrection: Boolean = false,
    //Select Source
    val selectSource: Int = 0,
    //Choose Face Model
    val chooseFaceModel: String = "None",
    //Source Folder
    val sourceFolder: String = "",
    //Multiple Source Images
    val multipleSourceImages: String? = "",
    //Random Image
    val randomImage: Boolean = false,
) {
    fun toParamArray(): List<Any?> {
        return listOf(
            singleSourceImage,
            enable,
            sourceImageAbove,
            targetImageResult,
            model,
            restoreFace,
            restoreFaceVisibility,
            postprocessingOrder,
            upscaler,
            scaleBy,
            upscalerVisibility,
            swapInSourceImage,
            swapInGeneratedImage,
            consoleLogLevel,
            genderDetectionSource,
            genderDetectionTarget,
            saveOriginalSwapInGeneratedOnly,
            codeFormerWeightFidelity,
            sourceImageHashCheck,
            targetImageHashCheck,
            executionProvider,
            faceMaskCorrection,
            selectSource,
            chooseFaceModel,
            sourceFolder,
            multipleSourceImages,
            randomImage
        )
    }
}

data class AdetailerSlotArg(
    @SerializedName("ad_model")
    val adModel: String,
    @SerializedName("ad_prompt")
    val adPrompt: String,
    @SerializedName("ad_negative_prompt")
    val adNegativePrompt: String,
    @SerializedName("ad_confidence")
    val adConfidence: Float,
    @SerializedName("ad_mask_k_largest")
    val adMaskKLargest: Long,
    @SerializedName("ad_mask_min_ratio")
    val adMaskMinRatio: Float,
    @SerializedName("ad_mask_max_ratio")
    val adMaskMaxRatio: Float,
    @SerializedName("ad_dilate_erode")
    val adDilateErode: Long,
    @SerializedName("ad_x_offset")
    val adXOffset: Long,
    @SerializedName("ad_y_offset")
    val adYOffset: Long,
    @SerializedName("ad_mask_merge_invert")
    val adMaskMergeInvert: String,
    @SerializedName("ad_mask_blur")
    val adMaskBlur: Long,
    @SerializedName("ad_denoising_strength")
    val adDenoisingStrength: Float,
    @SerializedName("ad_inpaint_only_masked")
    val adInpaintOnlyMasked: Boolean,
    @SerializedName("ad_inpaint_only_masked_padding")
    val adInpaintOnlyMaskedPadding: Long,
    @SerializedName("ad_use_inpaint_width_height")
    val adUseInpaintWidthHeight: Boolean,
    @SerializedName("ad_inpaint_width")
    val adInpaintWidth: Long,
    @SerializedName("ad_inpaint_height")
    val adInpaintHeight: Long,
    @SerializedName("ad_use_steps")
    val adUseSteps: Boolean,
    @SerializedName("ad_steps")
    val adSteps: Long,
    @SerializedName("ad_use_cfg_scale")
    val adUseCfgScale: Boolean,
    @SerializedName("ad_cfg_scale")
    val adCfgScale: Float,
    @SerializedName("ad_use_checkpoint")
    val adUseCheckpoint: Boolean,
    @SerializedName("ad_checkpoint")
    val adCheckpoint: String,
    @SerializedName("ad_use_vae")
    val adUseVae: Boolean,
    @SerializedName("ad_vae")
    val adVae: String,
    @SerializedName("ad_use_sampler")
    val adUseSampler: Boolean,
    @SerializedName("ad_sampler")
    val adSampler: String,
    @SerializedName("ad_use_noise_multiplier")
    val adUseNoiseMultiplier: Boolean,
    @SerializedName("ad_noise_multiplier")
    val adNoiseMultiplier: Float,
    @SerializedName("ad_use_clip_skip")
    val adUseClipSkip: Boolean,
    @SerializedName("ad_clip_skip")
    val adClipSkip: Long,
    @SerializedName("ad_restore_face")
    val adRestoreFace: Boolean,
    @SerializedName("ad_controlnet_model")
    val adControlnetModel: String,
    @SerializedName("ad_controlnet_module")
    val adControlnetModule: String,
    @SerializedName("ad_controlnet_weight")
    val adControlnetWeight: Float,
    @SerializedName("ad_controlnet_guidance_start")
    val adControlnetGuidanceStart: Float,
    @SerializedName("ad_controlnet_guidance_end")
    val adControlnetGuidanceEnd: Float,
)

data class AdetailerArg(
    val enabled: Boolean = false,
    val skipImg2img: Boolean = false,
    val slot: List<AdetailerSlotArg> = listOf(),
) {
    fun toParamArray(): List<Any> {
        return listOf(
            enabled,
            skipImg2img,
        ) + slot.map {
            with(it) {
                hashMapOf(
                    "ad_model" to adModel,
                    "ad_prompt" to adPrompt,
                    "ad_negative_prompt" to adNegativePrompt,
                    "ad_confidence" to adConfidence,
                    "ad_mask_k_largest" to adMaskKLargest,
                    "ad_mask_min_ratio" to adMaskMinRatio,
                    "ad_mask_max_ratio" to adMaskMaxRatio,
                    "ad_dilate_erode" to adDilateErode,
                    "ad_x_offset" to adXOffset,
                    "ad_y_offset" to adYOffset,
                    "ad_mask_merge_invert" to adMaskMergeInvert,
                    "ad_mask_blur" to adMaskBlur,
                    "ad_denoising_strength" to adDenoisingStrength,
                    "ad_inpaint_only_masked" to adInpaintOnlyMasked,
                    "ad_inpaint_only_masked_padding" to adInpaintOnlyMaskedPadding,
                    "ad_use_inpaint_width_height" to adUseInpaintWidthHeight,
                    "ad_inpaint_width" to adInpaintWidth,
                    "ad_inpaint_height" to adInpaintHeight,
                    "ad_use_steps" to adUseSteps,
                    "ad_steps" to adSteps,
                    "ad_use_cfg_scale" to adUseCfgScale,
                    "ad_cfg_scale" to adCfgScale,
                    "ad_use_checkpoint" to adUseCheckpoint,
                    "ad_checkpoint" to adCheckpoint,
                    "ad_use_vae" to adUseVae,
                    "ad_vae" to adVae,
                    "ad_use_sampler" to adUseSampler,
                    "ad_sampler" to adSampler,
                    "ad_use_noise_multiplier" to adUseNoiseMultiplier,
                    "ad_noise_multiplier" to adNoiseMultiplier,
                    "ad_use_clip_skip" to adUseClipSkip,
                    "ad_clip_skip" to adClipSkip,
                    "ad_restore_face" to adRestoreFace,
                    "ad_controlnet_model" to adControlnetModel,
                    "ad_controlnet_module" to adControlnetModule,
                    "ad_controlnet_weight" to adControlnetWeight,
                    "ad_controlnet_guidance_start" to adControlnetGuidanceStart,
                    "ad_controlnet_guidance_end" to adControlnetGuidanceEnd,
                )
            }
        }
    }

}

data class AlwaysonScripts(
    @SerializedName("controlnet")
    var controlNet: ControlNetWrapper? = null,
    @SerializedName("Regional Prompter")
    var regionalPrompter: RegionalPrompterWrapper? = null,
    @SerializedName("reactor")
    var reactor: ReactorWrapper? = null,
    @SerializedName("ADetailer") var adetailer: AdeatilerWrapper? = null,
)

data class ControlNetWrapper(
    @SerializedName("args")
    val args: List<ControlNetArg> = listOf(),
)

data class RegionalPrompterWrapper(
    @SerializedName("args")
    val args: List<Any>? = null
)

data class ReactorWrapper(
    @SerializedName("args")
    val args: List<Any?>? = null
)

data class AdeatilerWrapper(
    @SerializedName("args")
    val args: List<Any>? = null
)

data class OverrideSetting(
    @SerializedName("sd_model_checkpoint")
    val sdModelCheckpoint: String? = null,
    @SerializedName("sd_vae")
    val sdVae: String? = null,
)

data class Txt2ImgRequest(
    @SerializedName("batch_size")
    val batchSize: Int = 1,
    @SerializedName("prompt")
    val prompt: String,
    @SerializedName("width")
    val width: Int = 512,
    @SerializedName("height")
    val height: Int = 512,
    @SerializedName("negative_prompt")
    val negativePrompt: String,
    @SerializedName("steps")
    val steps: Int = 20,
    @SerializedName("sampler_name")
    val samplerName: String = "dimm",
    @SerializedName("n_iter")
    val nIterate: Int = 1,
    @SerializedName("cfg_scale")
    val cfgScale: Int = 7,
    @SerializedName("seed")
    val seed: Long = -1,
    @SerializedName("enable_hr")
    val enableHr: Boolean = false,
    @SerializedName("denoising_strength")
    val denoisingStrength: Float = 0.7f,
    @SerializedName("hr_second_pass_steps")
    val hrSecondPassSteps: Long = 0,
    @SerializedName("hr_scale")
    val hrScale: Float = 1f,
    @SerializedName("hr_upscaler")
    val hrUpscaler: String = "None",
    @SerializedName("hr_resize_x")
    val hrResizeX: Long = 0,
    @SerializedName("hr_resize_y")
    val hrResizeY: Long = 0,
    @SerializedName("alwayson_scripts")
    val alwaysonScripts: AlwaysonScripts? = AlwaysonScripts(null),
    @SerializedName("refiner_checkpoint")
    val refinerCheckpoint: String? = null,
    @SerializedName("refiner_switch_at")
    val refinerSwitchAt: Float? = null,
    @SerializedName("override_settings")
    val overrideSetting: OverrideSetting? = null,
)

//"resize_mode": 0,
//"image_cfg_scale": 0,
//"denoising_strength": 0.75,
// "scale_by": 1.0,
data class Img2ImgRequest(
    val batch_size: Int = 1,
    val prompt: String,
    val width: Int = 512,
    val height: Int = 512,
    val negative_prompt: String,
    val steps: Int = 20,
    val sampler_name: String = "dimm",
    val n_iter: Int = 1,
    val cfg_scale: Int = 7,
    val seed: Long = -1,
    val alwayson_scripts: AlwaysonScripts? = AlwaysonScripts(null),
    val init_images: List<String> = listOf(),
    val resize_mode: Int = 0,
    val image_cfg_scale: Float = 7f,
    val denoising_strength: Float = 0.75f,
    val scale_by: Float = 1f,
    // mask
    val mask: String? = null,
    //Mask mode
    val inpainting_mask_invert: Int = 0,
    //Mask blur
    val mask_blur: Float = 4f,
    //Masked content
    val inpainting_fill: Int = 0,
    //Inpaint area
    val inpaint_full_res: Int = 1,
    //inpaint_full_res_padding
    val inpaint_full_res_padding: Int = 32,

    )

data class InterrogateRequest(
    val image: String,
    val model: String,
)

data class OptionsRequestBody(
    @SerializedName("sd_model_checkpoint") val sdModelCheckpoint: String? = null,
    @SerializedName("sd_vae") val sdVae: String? = null,
)

data class ExtraImageRequest(
    @SerializedName("resize_mode") val resizeMode: Int = 0,
    @SerializedName("show_extras_results") val showExtrasResults: Boolean = true,
    @SerializedName("gfpgan_visibility") val gfpganVisibility: Float = 0f,
    @SerializedName("codeformer_visibility") val codeformerVisibility: Float = 0f,
    @SerializedName("codeformer_weight") val codeformerWeight: Float = 0f,
    @SerializedName("upscaling_resize") val upscalingResize: Float = 2f,
    @SerializedName("upscaling_resize_w") val upscalingResizeW: Int = 512,
    @SerializedName("upscaling_resize_h") val upscalingResizeH: Int = 512,
    @SerializedName("upscaling_crop") val upscalingCrop: Boolean = true,
    @SerializedName("upscaler_1") val upscaler1: String = "None",
    @SerializedName("upscaler_2") val upscaler2: String = "None",
    @SerializedName("extras_upscaler_2_visibility") val extrasUpscaler2Visibility: Int = 0,
    @SerializedName("upscale_first") val upscaleFirst: Boolean = false,
    @SerializedName("image") val image: String = "",
)

data class ControlNetDetectRequest(
    @SerializedName("controlnet_module") val controlNetModule: String = "none",
    @SerializedName("controlnet_processor_res") val controlNetProcessorRes: Float = 512f,
    @SerializedName("controlnet_threshold_a") val controlNetThresholdA: Float = 64f,
    @SerializedName("controlnet_threshold_b") val controlNetThresholdB: Float = 64f,
    @SerializedName("controlnet_input_images") val controlNetInputImages: List<String> = listOf(),
)

data class ReactorRequestBody(
    @SerializedName("source_image")
    val sourceImage: String,
    @SerializedName("target_image")
    val targetImage: String,
    @SerializedName("source_faces_index")
    val sourceFacesIndex: List<Long>,
    @SerializedName("face_index")
    val faceIndex: List<Long>,
    val upscaler: String,
    val scale: Float,
    @SerializedName("upscale_visibility")
    val upscaleVisibility: Float,
    @SerializedName("face_restorer")
    val faceRestorer: String,
    @SerializedName("restorer_visibility")
    val restorerVisibility: Float,
    @SerializedName("restore_first")
    val restoreFirst: Long,
    val model: String,
    @SerializedName("gender_source")
    val genderSource: Int,
    @SerializedName("gender_target")
    val genderTarget: Int,
    @SerializedName("save_to_file")
    val saveToFile: Int,
    @SerializedName("result_file_path")
    val resultFilePath: String,
)

interface SDWApi {
    @GET("/sdapi/v1/samplers")
    suspend fun getSamplers(): Response<List<Sampler>>

    @POST("/sdapi/v1/txt2img")
    suspend fun txt2img(
        @Body request: Txt2ImgRequest
    ): Response<Text2ImageResult>

    @POST("/sdapi/v1/img2img")
    suspend fun img2img(
        @Body request: Img2ImgRequest
    ): Response<Img2ImgResult>

    @GET("/sdapi/v1/progress")
    suspend fun getProgress(): Response<Progress>

    @GET("/sdapi/v1/sd-models")
    suspend fun getModels(): Response<List<Model>>

    @POST("/sdapi/v1/options")
    suspend fun setOptions(
        @Body request: OptionsRequestBody
    ): Response<Unit>

    @GET("/sdapi/v1/options")
    suspend fun getOptions(): Response<Option>

    @GET("/sdapi/v1/interrupt")
    suspend fun interrupt(): Response<Unit>

    @GET("/sdapi/v1/upscalers")
    suspend fun getUpscalers(): Response<List<Upscale>>

    @GET("/sdapi/v1/loras")
    suspend fun getLoras(): Response<List<Lora>>

    @POST("/sdapi/v1/interrogate")
    suspend fun interrogate(
        @Body request: InterrogateRequest
    ): Response<Interrogate>

    @GET("/controlnet/version")
    suspend fun getControlNetVersion(): Response<ControlNetVersion>

    @GET("/controlnet/model_list")
    suspend fun getControlModelList(): Response<ControlModelList>

    @POST("/sdapi/v1/extra-single-image")
    suspend fun extraSingleImage(
        @Body request: ExtraImageRequest
    ): Response<ExtraSingleImageResult>

    @GET("/controlnet/module_list")
    suspend fun getControlNetModuleList(): Response<ControlNetModule>

    @GET("/controlnet/control_types")
    suspend fun getControlNetControlTypes(): Response<ControlTypesResult>

    @POST("/controlnet/detect")
    suspend fun detect(
        @Body request: ControlNetDetectRequest
    ): Response<ControlNetDetectResult>

    @GET("/sdapi/v1/embeddings")
    suspend fun getEmbeddingList(): Response<SDWEmbeddingList>

    @GET("/sdapi/v1/sd-vae")
    suspend fun getVaeList(): Response<List<Vae>>

    @GET("/reactor/upscalers")
    suspend fun getReactorUpscaler(): Response<ReactorUpscaleList>

    @GET("/reactor/models")
    suspend fun getReactorModel(): Response<ReactorModelList>

    @GET("/adetailer/v1/ad_model")
    suspend fun getAdetailerModel(): Response<AdetailerModelList>

    @GET("/diffusionhelper/hash")
    suspend fun getHash(
        @Query("modelType")
        modelType: String,
        @Query("name")
        name: String
    ): Response<ModelHash>

    @GET("/diffusionhelper/ping")
    suspend fun ping(): Response<HelperPing>


    @POST("/reactor/image")
    suspend fun reactorImage(
        @Body request: ReactorRequestBody
    ): Response<RactorResultImage>

    @GET("/sdapi/v1/extensions")
    suspend fun getExtensions(): Response<List<SDWExtension>>

}