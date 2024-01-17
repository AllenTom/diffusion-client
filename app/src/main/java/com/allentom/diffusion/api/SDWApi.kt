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

data class AlwaysonScripts(
    @SerializedName("controlnet")
    val controlNet: ControlNetWrapper?,
)

data class ControlNetWrapper(
    @SerializedName("args")
    val args: List<ControlNetParam> = listOf(),
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