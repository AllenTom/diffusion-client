package com.allentom.diffusion.api

import com.allentom.diffusion.api.entity.ControlModelList
import com.allentom.diffusion.api.entity.ControlNetDetectResult
import com.allentom.diffusion.api.entity.ControlNetModule
import com.allentom.diffusion.api.entity.ControlNetVersion
import com.allentom.diffusion.api.entity.ExtraSingleImageResult
import com.allentom.diffusion.api.entity.HelperPing
import com.allentom.diffusion.api.entity.Img2ImgResult
import com.allentom.diffusion.api.entity.Interrogate
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.api.entity.Model
import com.allentom.diffusion.api.entity.ModelHash
import com.allentom.diffusion.api.entity.Option
import com.allentom.diffusion.api.entity.Progress
import com.allentom.diffusion.api.entity.SDWEmbeddingList
import com.allentom.diffusion.api.entity.Sampler
import com.allentom.diffusion.api.entity.Text2ImageResult
import com.allentom.diffusion.api.entity.Upscale
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
data class ControlNetParam(
    @SerializedName("enabled")
    val enabled: Boolean = false,
    @SerializedName("processor_res")
    val processorRes: Int = 64,
    @SerializedName("threshold_a")
    val thresholdA: Int = 64,
    @SerializedName("threshold_b")
    val thresholdB: Int = 64,
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

data class AlwaysonScripts(
    @SerializedName("controlnet")
    var controlNet: ControlNetWrapper? = null,
    @SerializedName("Regional Prompter")
    var regionalPrompter: RegionalPrompterWrapper? = null,
)

data class ControlNetWrapper(
    @SerializedName("args")
    val args: List<ControlNetParam> = listOf(),
)

data class RegionalPrompterWrapper(
    @SerializedName("args")
    val args: List<Any>? = null
)

data class Txt2ImgRequest(
    val batch_size: Int = 1,
    val prompt: String,
    val width: Int = 512,
    val height: Int = 512,
    val negative_prompt: String,
    val steps: Int = 20,
    val sampler_name: String = "dimm",
    val n_iter: Int = 1,
    val cfg_scale: Int = 7,
    val seed: Int = -1,
    val enable_hr: Boolean = false,
    val denoising_strength: Float = 0.7f,
    val hr_scale: Float = 1f,
//    val hr_steps:Int = 0,
    val hr_upscaler: String = "None",
    val alwayson_scripts: AlwaysonScripts? = AlwaysonScripts(null),
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
    val seed: Int = -1,
    val alwayson_scripts: AlwaysonScripts? = AlwaysonScripts(null),
    val init_images: List<String> = listOf(),
    val resize_mode: Int = 0,
    val image_cfg_scale: Float = 7f,
    val denoising_strength: Float = 0.75f,
    val scale_by: Float = 1f,
    // mask
    val mask: String = "",
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
    @SerializedName("sd_model_checkpoint") val sdModelCheckpoint: String,
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

    @POST("/controlnet/detect")
    suspend fun detect(
        @Body request: ControlNetDetectRequest
    ): Response<ControlNetDetectResult>

    @GET("/sdapi/v1/embeddings")
    suspend fun getEmbeddingList(): Response<SDWEmbeddingList>

    @GET("/diffusionhelper/hash")
    suspend fun getHash(
        @Query("modelType")
        modelType: String,
        @Query("name")
        name: String
    ): Response<ModelHash>

    @GET("/diffusionhelper/ping")
    suspend fun ping(): Response<HelperPing>
}