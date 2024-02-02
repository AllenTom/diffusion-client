package com.allentom.diffusion.ui.screens.home.tabs.draw

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.OptionsRequestBody
import com.allentom.diffusion.api.entity.ApiError
import com.allentom.diffusion.api.entity.ApiException
import com.allentom.diffusion.api.entity.Embedding
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.api.entity.Model
import com.allentom.diffusion.api.entity.Option
import com.allentom.diffusion.api.entity.Progress
import com.allentom.diffusion.api.entity.Sampler
import com.allentom.diffusion.api.entity.Upscale
import com.allentom.diffusion.api.entity.Vae
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.service.GenerateImageService
import com.allentom.diffusion.service.Img2imgGenerateParam
import com.allentom.diffusion.service.Text2ImageParam
import com.allentom.diffusion.service.img2Image
import com.allentom.diffusion.service.text2Image
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.EmbeddingPrompt
import com.allentom.diffusion.store.history.HistoryStore
import com.allentom.diffusion.store.history.ImageHistory
import com.allentom.diffusion.store.history.ImageHistoryEntity
import com.allentom.diffusion.store.history.Img2imgParam
import com.allentom.diffusion.store.LoraPrompt
import com.allentom.diffusion.store.ModelStore
import com.allentom.diffusion.store.Prompt
import com.allentom.diffusion.store.PromptStore
import com.allentom.diffusion.store.SaveControlNet
import com.allentom.diffusion.store.history.SaveHistory
import com.allentom.diffusion.store.history.SaveHrParam
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.max

suspend fun fetchDataFromApi(): List<Sampler> {
    val list = getApiClient().getSamplers()
    val body = list.body()
    if (list.isSuccessful && body != null) {
        return body
    }
    return emptyList()
}

suspend fun fetchModelFromApi(): List<Model> {
    val list = getApiClient().getModels()
    val body = list.body()
    if (list.isSuccessful && body != null) {
        return body
    }
    return emptyList()
}

suspend fun fetchVaeFromApi(): List<Vae> {
    val list = getApiClient().getVaeList()
    val body = list.body()
    if (list.isSuccessful && body != null) {
        return body
    }
    return emptyList()
}

suspend fun fetchUpscalersFromApi(): List<Upscale> {
    val list = getApiClient().getUpscalers()
    val body = list.body()
    if (list.isSuccessful && body != null) {
        return body
    }
    return emptyList()
}

suspend fun fetchLoraFromApi(): List<Lora> {
    val list = getApiClient().getLoras()
    val body = list.body()
    if (list.isSuccessful && body != null) {
        return body
    }
    return emptyList()
}

suspend fun fetchEmbeddingFromApi(): Map<String, Embedding> {
    val list = getApiClient().getEmbeddingList()
    val body = list.body()
    if (list.isSuccessful && body != null) {
        return list.body()!!.loaded
    }
    return emptyMap()
}

suspend fun fetchReactorUpscalerFromApi(): List<String> {
    try {
        val list = getApiClient().getReactorUpscaler()
        val body = list.body()
        if (list.isSuccessful && body != null) {
            return list.body()!!.upscalers
        }
    } catch (e: java.lang.Exception) {
        return emptyList()
    }
    return emptyList()
}

suspend fun fetchReactorModelFromApi(): List<String> {
    try {
        val list = getApiClient().getReactorModel()
        val body = list.body()
        if (list.isSuccessful && body != null) {
            return list.body()!!.models
        }
    } catch (e: java.lang.Exception) {
        return emptyList()
    }
    return emptyList()
}

suspend fun fetchAdetailerModelFromApi(): List<String> {
    try {
        val list = getApiClient().getAdetailerModel()
        val body = list.body()
        if (list.isSuccessful && body != null) {
            return list.body()!!.models
        }
    } catch (e: java.lang.Exception) {
        return emptyList()
    }
    return emptyList()
}

data class GenImageItem(
    val imageBase64: String?,
    val progress: Progress?,
    val seed: Int,
    val imageName: String,
    val error: ApiError?,
    val isInterrupted: Boolean = false
) {
    fun getDisplayImageBase64(): String? {
        if (imageBase64 != null) {
            return imageBase64
        }
        if (progress != null) {
            return progress.currentImage
        }
        return null
    }
}

data class RegionPromptParam(
    val dividerText: String = "1",
    val regionCount: Int = 1,
    val useCommon: Boolean = true,
    val enable: Boolean = false
) {
    fun getTotalRegionCount(): Int {
        if (useCommon) {
            return regionCount + 1
        }
        return regionCount
    }
}

data class ReactorParam(
    val enabled: Boolean = false,
    val singleImageResult: String? = null,
    val singleImageResultFilename: String? = null,
    val genderDetectionSource: Int = 0,
    val genderDetectionTarget: Int = 0,
    val restoreFace: String = "None",
    val restoreFaceVisibility: Float = 1f,
    val codeFormerWeightFidelity: Float = 0.5f,
    val postprocessingOrder: Boolean = true,
    val upscaler: String = "None",
    val scaleBy: Float = 1f,
    val upscalerVisibility: Float = 1f,
    val model: String? = null
)

data class AdetailerSlot(
    val adModel: String = "face_yolov8n.pt",
    val adPrompt: String = "",
    val adNegativePrompt: String = "",
    val adConfidence: Float = 0.3f,
    val adMaskKLargest: Long = 0,
    val adMaskMinRatio: Float = 0f,
    val adMaskMaxRatio: Float = 1f,
    val adDilateErode: Long = 32,
    val adXOffset: Long = 0,
    val adYOffset: Long = 0,
    val adMaskMergeInvert: String = "None",
    val adMaskBlur: Long = 4,
    val adDenoisingStrength: Float = 0.4f,
    val adInpaintOnlyMasked: Boolean = true,
    val adInpaintOnlyMaskedPadding: Long = 0,
    val adUseInpaintWidthHeight: Boolean = false,
    val adInpaintWidth: Long = 512,
    val adInpaintHeight: Long = 512,
    val adUseSteps: Boolean = true,
    val adSteps: Long = 28,
    val adUseCfgScale: Boolean = false,
    val adCfgScale: Float = 7.0f,
    val adUseCheckpoint: Boolean = false,
    val adCheckpoint: String = "Use same checkpoint",
    val adUseVae: Boolean = false,
    val adVae: String = "Use same VAE",
    val adUseSampler: Boolean = false,
    val adSampler: String = "DPM++ 2M Karras",
    val adUseNoiseMultiplier: Boolean = false,
    val adNoiseMultiplier: Float = 1.0f,
    val adUseClipSkip: Boolean = false,
    val adClipSkip: Long = 1,
    val adRestoreFace: Boolean = false,
    val adControlnetModel: String = "None",
    val adControlnetModule: String = "None",
    val adControlnetWeight: Float = 1.0f,
    val adControlnetGuidanceStart: Float = 0.0f,
    val adControlnetGuidanceEnd: Float = 1.0f
)

data class AdetailerParam(
    val enabled: Boolean = false,
    val skipImg2img: Boolean = false,
    val slots: List<AdetailerSlot> = listOf(AdetailerSlot())
)

data class ControlNetParam(
    val slots: List<ControlNetSlot> = listOf(ControlNetSlot())
)

data class ControlNetSlot(
    val controlNetHistoryId: Long = 0,
    val controlNetId: Long = 0,
    val historyId: Long = 0,
    val enabled: Boolean = false,
    val guidanceStart: Float = 0f,
    val guidanceEnd: Float = 1f,
    val controlMode: Int = 0,
    val weight: Float = 1f,
    val model: String? = null,
    val inputImage: String? = null,
    val inputImagePath: String? = null
)

data class XYZParam(
    val xAxis: GenModifier? = null,
    val yAxis: GenModifier? = null
) {
    val enable: Boolean
        get() = xAxis != null || yAxis != null
    val totalCount: Int
        get() {
            if (xAxis != null && yAxis != null) {
                return xAxis.getGenCount() * yAxis.getGenCount()
            }
            if (xAxis != null) {
                return xAxis.getGenCount()
            }
            if (yAxis != null) {
                return yAxis.getGenCount()
            }
            return 0
        }

    fun getYAxisIndex(index: Int): Int? {
        val xAxisCount = max(xAxis?.getGenCount() ?: 0, 1)

        return index / xAxisCount

        return null
    }

    fun getXAxisIndex(index: Int): Int? {
        val xAxisCount = max(xAxis?.getGenCount() ?: 0, 1)
        return index % xAxisCount
    }

    fun getDisplayAxis(): DisplayAxis {
        return DisplayAxis(
            xAxisName = xAxis?.getKey(),
            xAxisValues = xAxis?.getStringValue()?.split(",") ?: emptyList(),
            yAxisName = yAxis?.getKey(),
            yAxisValues = yAxis?.getStringValue()?.split(",") ?: emptyList()
        )
    }

}

data class DisplayAxis(
    val xAxisName: String? = null,
    val xAxisValues: List<String> = emptyList(),
    val yAxisName: String? = null,
    val yAxisValues: List<String> = emptyList()
) {

}


object DrawViewModel {
    var genScope: CoroutineScope? = null
    var inputPromptText by mutableStateOf(
        "1girl,serafuku,classroom".split(",").map { Prompt(it, 0) })
    var inputNegativePromptText by mutableStateOf(
        "nsfw".split(",").map { Prompt(it, 0) })
    var regionPromptParam by mutableStateOf<RegionPromptParam>(RegionPromptParam())
    var embeddingList by mutableStateOf<List<EmbeddingPrompt>>(emptyList())
    var embeddingModels by mutableStateOf(emptyMap<String, Embedding>())
    var inputWidth by mutableFloatStateOf(300f)
    var inputHeight by mutableFloatStateOf(512f)
    var inputSteps by mutableFloatStateOf(20f)
    var inputNiter by mutableFloatStateOf(1f)
    var inputSamplerName by mutableStateOf("DDIM")
    var inputCfgScale by mutableFloatStateOf(7f)
    var inputSeed by mutableIntStateOf(-1)
    var samplerList by mutableStateOf(emptyList<Sampler>())
    var progress by mutableStateOf<Progress?>(null)
    var models by mutableStateOf<List<Model>>(emptyList())
    var vaeList by mutableStateOf<List<Vae>>(emptyList())
    var useVae by mutableStateOf<String?>(null)
    var isGenerating by mutableStateOf(false)
    var options by mutableStateOf<Option?>(null)
    var useModelName by mutableStateOf<String?>(null)
    var isSwitchingModel by mutableStateOf(false)
    var currentGenIndex by mutableStateOf(0)
    var totalGenCount by mutableStateOf(0)

    var genItemList by mutableStateOf<List<GenImageItem>>(emptyList())
    var genXYZ by mutableStateOf<DisplayAxis?>(null)

    var displayResultIndex by mutableStateOf(0)
    var inputEnableHiresFix by mutableStateOf(false)
    var inputHrScale by mutableFloatStateOf(2f)
    var inputHrSteps by mutableFloatStateOf(0f)
    var inputHrDenoisingStrength by mutableFloatStateOf(0.7f)
    var inputUpscaler by mutableStateOf<String>("None")
    var upscalers by mutableStateOf<List<Upscale>>(emptyList())
    var loraList by mutableStateOf<List<Lora>>(emptyList())
    var inputLoraList by mutableStateOf<List<LoraPrompt>>(emptyList())

    var inputControlNetParams by mutableStateOf(ControlNetParam())
    var controlNetModelList by mutableStateOf<List<String>>(emptyList())

    var enableControlNetFeat by mutableStateOf(false)
    var inputImg2ImgImgBase64 by mutableStateOf<String?>(null)
    var inputImg2ImgImgFilename by mutableStateOf<String?>(null)
    var generateMode by mutableStateOf("text2img")
    var inputImg2ImgDenoisingStrength by mutableFloatStateOf(0.7f)
    var inputImg2ImgResizeMode by mutableIntStateOf(0)
    var inputImg2ImgScaleBy by mutableFloatStateOf(1f)
    var inputImg2ImgWidth by mutableFloatStateOf(512f)
    var inputImg2ImgHeight by mutableFloatStateOf(512f)
    var inputImg2ImgCfgScale by mutableFloatStateOf(7f)
    var inputImg2ImgMask by mutableStateOf<String?>(null)
    var inputImg2ImgMaskPreview by mutableStateOf<String?>(null)
    var inputImg2ImgInpaint by mutableStateOf(false)
    var inputImg2ImgMaskBlur by mutableFloatStateOf(4f)
    var inputImg2ImgInpaintingMaskInvert by mutableStateOf(0)
    var inputImg2ImgInpaintingFill by mutableStateOf(0)
    var inputImg2ImgInpaintingFullRes by mutableStateOf(0)

    var enableRefiner by mutableStateOf(false)
    var refinerModel by mutableStateOf<String?>(null)
    var refinerSwitchAt by mutableStateOf(0.8f)

    var inputImg2ImgInpaintingFullResPadding by mutableStateOf(32)

    var reactorParam by mutableStateOf<ReactorParam>(ReactorParam())
    var reactorUpscalerList by mutableStateOf<List<String>>(emptyList())
    var reactorModelList by mutableStateOf<List<String>>(emptyList())

    var currentHistory by mutableStateOf<SaveHistory?>(null)

    var adetailerParam by mutableStateOf<AdetailerParam>(AdetailerParam())
    var adetailerModelList by mutableStateOf<List<String>>(emptyList())
    var xyzParam by mutableStateOf<XYZParam>(XYZParam())

    fun startGenerating(
        count: Int,
        seed: Int = -1
    ) {
        isGenerating = true
        totalGenCount = count
        currentGenIndex = 0
        displayResultIndex = 0
        genItemList = emptyList()
        for (i in 0 until count) {
            val imageSeed = if (seed == -1) {
                (0..100000000).random()
            } else {
                seed
            }
            val imageName = "diffusion_${i}_${imageSeed}.jpg"
            genItemList = genItemList + GenImageItem(null, null, imageSeed, imageName, null)
        }
        currentHistory = null
        if (xyzParam.enable) {
            genXYZ = xyzParam.getDisplayAxis()
        } else {
            genXYZ = null
        }
    }

    var interruptFlag by mutableStateOf(false)
    val genImageListMutex = Mutex()
    val gson = Gson()

    suspend fun updateGenItemByIndex(index: Int, update: (item: GenImageItem) -> GenImageItem) {
        genImageListMutex.withLock {
            genItemList = genItemList.toMutableList().also {
                it[index] = update(it[index])
            }
        }
    }

    fun getPositivePrompt(): String {
        if (regionPromptParam.regionCount > 0 && regionPromptParam.enable) {
            return getPositiveWithRegion()
        }
        return inputPromptText.joinToString(",") { it.getPromptText() }
    }

    fun getPositiveWithRegion(): String {
        var promptTextList = mutableListOf<String>()
        val maxRegion = regionPromptParam.getTotalRegionCount()
        for (i in 0 until maxRegion) {
            val regionText =
                inputPromptText.filter { it.regionIndex == i }.map { it.getPromptText() }
                    .joinToString(",")
            promptTextList.add(regionText)
        }
        return promptTextList.joinToString(" BREAK\n")
    }

    fun getNegativePrompt(): String {
        return (inputNegativePromptText.map { it.getPromptText() } + embeddingList.map { it.getPromptText() }).joinToString(
            ","
        )
    }

    fun getLoraPrompt(): String {
        return inputLoraList.joinToString(",") { it.getPromptText().joinToString(",") }
    }

    fun updateRegionCount(count: Int) {
        regionPromptParam = regionPromptParam.copy(regionCount = count)
        inputPromptText = inputPromptText.map {
            if (it.regionIndex >= count) {
                it.copy(regionIndex = 0)
            } else {
                it
            }
        }
    }

    fun applyHistory(context: Context, useHistory: SaveHistory) {
        val history = HistoryStore.getHistoryById(context, useHistory.id) ?: return
        inputPromptText = history.prompt
        inputNegativePromptText = history.negativePrompt
        inputSteps = history.steps.toFloat()
        inputSamplerName = history.samplerName
        inputWidth = history.width.toFloat()
        inputHeight = history.height.toFloat()
        inputCfgScale = history.cfgScale
        embeddingList = history.embeddingPrompt
        regionPromptParam = RegionPromptParam(
            regionCount = history.regionCount ?: 1,
            dividerText = history.regionRatio ?: "1",
            useCommon = history.regionUseCommon ?: false,
            enable = history.regionEnable ?: false
        )
        enableRefiner = history.enableRefiner
        refinerModel = history.refinerModelName
        refinerSwitchAt = history.refinerSwitchAt ?: 0.8f



        inputUpscaler = history.hrParam.hrUpscaler
        inputHrScale = history.hrParam.hrScale
        inputHrDenoisingStrength = history.hrParam.hrDenosingStrength
        inputEnableHiresFix = history.hrParam.enableScale

        history.img2imgParam?.let { img2imgParam: Img2imgParam ->
            val inputImageBase64 = Util.readImageWithPathToBase64(img2imgParam.path)
            inputImg2ImgWidth = img2imgParam.width.toFloat()
            inputImg2ImgHeight = img2imgParam.height.toFloat()
            inputImg2ImgCfgScale = img2imgParam.cfgScale
            inputImg2ImgDenoisingStrength = img2imgParam.denoisingStrength
            inputImg2ImgResizeMode = img2imgParam.resizeMode
            inputImg2ImgScaleBy = img2imgParam.scaleBy
            inputImg2ImgImgFilename = img2imgParam.path
            inputImg2ImgImgBase64 = inputImageBase64
            if (img2imgParam.inpaint == true) {
                val maskBase64 = Util.readImageWithPathToBase64(img2imgParam.maskPath!!)
                inputImg2ImgInpaint = true
                inputImg2ImgMask = maskBase64
                img2imgParam.maskBlur?.let {
                    inputImg2ImgMaskBlur = it
                }
                img2imgParam.maskInvert?.let {
                    inputImg2ImgInpaintingMaskInvert = it
                }
                img2imgParam.inpaintingFill?.let {
                    inputImg2ImgInpaintingFill = it
                }
                img2imgParam.inpaintingFullRes?.let {
                    inputImg2ImgInpaintingFullRes = it
                }
                img2imgParam.inpaintingFullResPadding?.let {
                    inputImg2ImgInpaintingFullResPadding = it
                }

                val previewBase64 = Util.combineBase64Images(
                    inputImageBase64,
                    maskBase64,
                )
                inputImg2ImgMaskPreview = previewBase64
            }
        }

        history.controlNetParam?.let {
            inputControlNetParams = it
        }
        // for lora
        inputLoraList = history.loraPrompt

        history.reactorParam?.let {
            reactorParam = it
        }
        history.adetailerParam?.let {
            adetailerParam = it
        }
        history.xyzParam?.let {
            xyzParam = it
        }
    }

    fun applyControlNetParams(context: Context, slotIndex: Int, saveControlNet: SaveControlNet) {
        val imageBase64 = Util.readImageWithPathToBase64(saveControlNet.path)
        // find history use
        val saveControlNetHistory =
            HistoryStore.findLatestControlNetUse(
                context = context,
                controlNetId = saveControlNet.id
            )
        inputControlNetParams = inputControlNetParams.copy(
            slots = inputControlNetParams.slots.mapIndexed { index, slot ->
                if (index == slotIndex) {
                    slot.copy(
                        controlNetHistoryId = saveControlNet.id,
                        controlNetId = saveControlNetHistory?.controlNetId ?: slot.controlNetId,
                        historyId = saveControlNetHistory?.historyId ?: slot.historyId,
                        enabled = true,
                        inputImage = imageBase64,
                        inputImagePath = saveControlNet.path,
                        controlMode = saveControlNetHistory?.controlMode ?: slot.controlMode,
                        weight = saveControlNetHistory?.weight ?: slot.weight,
                        guidanceStart = saveControlNetHistory?.guidanceStart ?: slot.guidanceStart,
                        guidanceEnd = saveControlNetHistory?.guidanceEnd ?: slot.guidanceEnd,
                        model = saveControlNetHistory?.model ?: slot.model
                    )
                } else {
                    slot
                }
            }

        )

    }

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "gen progress"
            val descriptionText = "gen progress"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("gen_ch", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(context, NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    fun startGenerate(context: Context, refreshIndex: Int? = null) {
        val act = context as Activity

        val intent = Intent(context, GenerateImageService::class.java)
        intent.putExtra("refreshIndex", refreshIndex)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            act.startService(intent)
        }
    }

    fun interruptGenerate() {
        genScope?.launch {
            try {
                getApiClient().interrupt()
                interruptFlag = true
                genItemList = genItemList.mapIndexed { index, item ->
                    if (index >= currentGenIndex) {
                        return@mapIndexed item.copy(
                            isInterrupted = true
                        )
                    }
                    item

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun generateImage(context: Context, refreshIndex: Int? = null) {
        genScope?.launch {
            val builder = NotificationCompat.Builder(context, "gen_ch")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(context.getString(R.string.generating))
                .setContentText(context.getString(R.string.generating))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100, 0, false)
                .setOngoing(true)
            val notificationCompat = NotificationManagerCompat.from(context)
            try {
                val genTotalCount = refreshIndex.let {
                    if (it == null) {
                        if (xyzParam.enable) {
                            xyzParam.totalCount
                        } else {
                            inputNiter.toInt()
                        }
                    } else {
                        1
                    }
                }
                if (refreshIndex == null) {
                    var useSeed = inputSeed
                    if (xyzParam.enable) {
                        useSeed = (0..100000000).random()
                    }
                    startGenerating(genTotalCount, useSeed)
                }
                val saveImagePaths = emptyList<ImageHistory>().toMutableList()
                var controlNetParam: ControlNetParam? = null
                if (enableControlNetFeat) {
                    controlNetParam = inputControlNetParams
                }
                for (genIndex in 0 until genTotalCount) {
                    if (refreshIndex != null) {
                        currentGenIndex = refreshIndex
                    } else {
                        currentGenIndex = genIndex
                    }
                    val seed = genItemList[currentGenIndex].seed
                    var resultImage: String? = null
                    with(builder) {
                        setProgress(100, (currentGenIndex + 1) * 100 / genTotalCount, false)
                        setContentText(
                            context.getString(
                                R.string.generate_btn_progress,
                                currentGenIndex + 1,
                                genTotalCount
                            )
                        )
                        notificationCompat.apply {
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                notify(1, builder.build())
                            }
                        }
                    }
                    if (generateMode == "text2img") {
                        try {
                            var param = Text2ImageParam(
                                prompt = listOf(
                                    getLoraPrompt(),
                                    getPositivePrompt()
                                ).filter { it.isNotBlank() }.joinToString(","),
                                negativePrompt = getNegativePrompt(),
                                width = inputWidth.toInt(),
                                height = inputHeight.toInt(),
                                steps = inputSteps.toInt(),
                                samplerName = inputSamplerName,
                                nIter = inputNiter.toInt(),
                                cfgScale = inputCfgScale,
                                seed = seed,
                                enableScale = inputEnableHiresFix,
                                hrScale = inputHrScale,
                                hrDenosingStrength = inputHrDenoisingStrength,
                                hrUpscaler = inputUpscaler,
                                controlNetParam = controlNetParam,
                                regionPromptParam = regionPromptParam,
                                refinerModel = if (enableRefiner) refinerModel else null,
                                refinerSwitchAt = if (enableRefiner) refinerSwitchAt else null,
                                reactorParam = reactorParam,
                                adetailerParam = adetailerParam
                            )
                            // x = 4 y = 2
                            // index = 4
                            // xIndex = 4 % 4 = 0
                            // yIndex = 4 / 4 = 1
                            val xIndex = xyzParam.getXAxisIndex(currentGenIndex)
                            val yIndex = xyzParam.getYAxisIndex(currentGenIndex)
                            xIndex?.let {
//                                Log.d(
//                                    "xIndex",
//                                    "xIndex: $xIndex $currentGenIndex % ${xAxis!!.getGenCount()}"
//                                )
                                param =
                                    xyzParam.xAxis?.onText2ImageParamChange(param, xIndex) ?: param
                            }
                            yIndex?.let {
//                                Log.d(
//                                    "yIndex",
//                                    "yIndex: $yIndex $currentGenIndex / ${xAxis!!.getGenCount()}"
//                                )
                                param =
                                    xyzParam.yAxis?.onText2ImageParamChange(param, yIndex) ?: param
                            }

                            text2Image(param)?.let {
                                resultImage = it
                            }
                        } catch (e: ApiException) {
                            genItemList.getOrNull(currentGenIndex)?.let {
                                updateGenItemByIndex(currentGenIndex) {
                                    it.copy(error = e.error)
                                }
                            }
                        }
                    }
                    if (generateMode == "img2img") {
                        try {
                            val param = Img2imgGenerateParam(
                                prompt = listOf(
                                    getPositivePrompt(),
                                    getLoraPrompt()
                                ).joinToString(","),
                                negativePrompt = getNegativePrompt(),
                                width = inputImg2ImgWidth.toInt(),
                                height = inputImg2ImgHeight.toInt(),
                                steps = inputSteps.toInt(),
                                samplerName = inputSamplerName,
                                cfgScale = inputCfgScale,
                                seed = seed,
                                imgBase64 = inputImg2ImgImgBase64,
                                resizeMode = inputImg2ImgResizeMode,
                                denoisingStrength = inputImg2ImgDenoisingStrength,
                                scaleBy = inputImg2ImgScaleBy,
                                mask = if (inputImg2ImgInpaint) inputImg2ImgMask ?: "" else "",
                                inpaintingMaskInvert = inputImg2ImgInpaintingMaskInvert,
                                maskBlur = inputImg2ImgMaskBlur,
                                inpaintingFill = inputImg2ImgInpaintingFill,
                                inpaintFullRes = inputImg2ImgInpaintingFullRes,
                                inpaintFullResPadding = inputImg2ImgInpaintingFullResPadding,
                                adetailerParam = adetailerParam,
                                regionPromptParam = regionPromptParam,
                                reactorParam = reactorParam
                            )
                            img2Image(param)?.let {
                                resultImage = it
                            }
                        } catch (e: ApiException) {
                            genItemList.getOrNull(currentGenIndex)?.let {
                                updateGenItemByIndex(currentGenIndex) {
                                    it.copy(error = e.error)
                                }
                            }
                        }
                    }
                    if (resultImage == null) {
                        continue
                    }

                    if (interruptFlag) {
                        interruptFlag = false
                        break
                    }
                    updateGenItemByIndex(currentGenIndex) {
                        it.copy(imageBase64 = resultImage)
                    }
                    val imagePath = Util.saveImageBase64ToAppData(
                        context,
                        resultImage!!,
                        genItemList[currentGenIndex].imageName
                    )
                    if (refreshIndex != null) {
                        continue
                    }
                    saveImagePaths += ImageHistory(
                        path = imagePath,
                        seed = seed,
                        name = genItemList[currentGenIndex].imageName,
                        favourite = false,
                        historyId = 0
                    )

                    withContext(Dispatchers.IO) {
                        PromptStore.updatePrompt(
                            context,
                            inputPromptText.map { it.text },
                        )
                        PromptStore.updatePrompt(
                            context,
                            inputNegativePromptText.map { it.text },
                        )
                    }

                }
                var img2ImgParam: Img2imgParam? = null
                if (generateMode == "img2img") {
                    val saveFilename = inputImg2ImgImgFilename ?: "${UUID.randomUUID()}.png"
                    inputImg2ImgImgBase64?.let { inputImageBase64 ->
                        val savePath = Util.saveImg2ImgFile(
                            context,
                            inputImageBase64,
                            saveFilename
                        )
                        img2ImgParam = Img2imgParam(
                            denoisingStrength = inputImg2ImgDenoisingStrength,
                            resizeMode = inputImg2ImgResizeMode,
                            scaleBy = inputImg2ImgScaleBy,
                            width = inputImg2ImgWidth.toInt(),
                            height = inputImg2ImgHeight.toInt(),
                            cfgScale = inputImg2ImgCfgScale,
                            path = savePath,
                            historyId = 0
                        )
                        if (inputImg2ImgInpaint) {
                            val maskFilename = "${UUID.randomUUID()}.png"
                            val maskPath = Util.saveImg2ImgMaskFile(
                                context,
                                inputImg2ImgMask ?: "",
                                maskFilename
                            )
                            img2ImgParam = img2ImgParam?.copy(
                                maskPath = maskPath,
                                inpaint = true,
                                maskBlur = inputImg2ImgMaskBlur,
                                maskInvert = inputImg2ImgInpaintingMaskInvert,
                                inpaintingFill = inputImg2ImgInpaintingFill,
                                inpaintingFullRes = inputImg2ImgInpaintingFullRes,
                                inpaintingFullResPadding = inputImg2ImgInpaintingFullResPadding
                            )
                        }
                    }
                }
                val saveHistory = SaveHistory(
                    prompt = inputPromptText,
                    negativePrompt = inputNegativePromptText,
                    steps = inputSteps.toInt(),
                    samplerName = inputSamplerName,
                    sdModelCheckpoint = useModelName ?: "",
                    width = inputWidth.toInt(),
                    height = inputHeight.toInt(),
                    batchSize = 1,
                    time = System.currentTimeMillis(),
                    imagePaths = saveImagePaths,
                    cfgScale = inputCfgScale,
                    loraPrompt = inputLoraList,
                    embeddingPrompt = embeddingList,
                    hrParam = SaveHrParam(
                        enableScale = inputEnableHiresFix,
                        hrScale = inputHrScale,
                        hrDenosingStrength = inputHrDenoisingStrength,
                        hrUpscaler = inputUpscaler
                    ),
                    img2imgParam = img2ImgParam,
                    controlNetParam = controlNetParam,
                    regionRatio = regionPromptParam.dividerText,
                    regionCount = regionPromptParam.regionCount,
                    regionUseCommon = regionPromptParam.useCommon,
                    regionEnable = regionPromptParam.enable,
                    vaeName = useVae,
                    enableRefiner = enableRefiner,
                    refinerModelName = refinerModel,
                    refinerSwitchAt = refinerSwitchAt,
                    reactorParam = reactorParam,
                    adetailerParam = adetailerParam,
                    xyzParam = xyzParam
                )
                genScope?.launch(Dispatchers.IO) {
                    HistoryStore.saveHistoryToDatabase(context, saveHistory)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isGenerating = false
                builder.setProgress(0, 0, false)
                builder.setContentTitle(context.getString(R.string.generated))
                builder.setContentText(context.getString(R.string.generated))
                builder.setOngoing(false)
                notificationCompat.apply {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        notify(1, builder.build())
                    }
                }
            }
        }

    }

    fun favouriteImage(context: Context, imgItem: GenImageItem) {
        val db = AppDatabaseHelper.getDatabase(context)
        val imgHistory = db.imageHistoryDao().getImageHistoryWithName(imgItem.imageName)
            ?: return
        db.imageHistoryDao().update(
            imgHistory.copy(
                favourite = true
            )
        )
    }

    fun favouriteImageHistory(context: Context, history: ImageHistory, historyId: Long) {
        val db = AppDatabaseHelper.getDatabase(context)
        db.imageHistoryDao().update(
            ImageHistoryEntity.fromImageHistory(history, historyId).copy(
                favourite = true,

                )
        )
    }

    fun addInputPrompt(text: String) {
        inputPromptText.none { it.text == text }.let {
            if (it) {
                inputPromptText = inputPromptText + Prompt(text, 0)
            }
        }
    }

    fun addInputPrompt(prompt: Prompt) {
        inputPromptText.none { it.text == prompt.text }.let {
            if (it) {
                inputPromptText = inputPromptText + prompt
            }
        }
    }

    fun removeInputPrompt(text: String) {
        inputPromptText = inputPromptText.filter { it.text != text }
    }

    fun addInputNegativePrompt(text: String) {
        inputNegativePromptText.none { it.text == text }.let {
            if (it) {
                inputNegativePromptText = inputNegativePromptText + Prompt(text, 0)
            }
        }
    }

    fun addInputNegativePrompt(prompt: Prompt) {
        inputNegativePromptText.none { it.text == prompt.text }.let {
            if (it) {
                inputNegativePromptText = inputNegativePromptText + prompt
            }
        }
    }

    fun removeInputNegativePrompt(text: String) {
        inputNegativePromptText = inputNegativePromptText.filter { it.text != text }
    }

    suspend fun loadLora(context: Context): List<Lora> {
        val loraList = fetchLoraFromApi()
        return loraList.map {
            val ent = PromptStore.getOrCreateLoraPromptByName(context, it.name).toPrompt()
            it.copy(entity = ent)
        }
    }

    suspend fun initViewModel(context: Context) {
        var startTime: Long
        var endTime: Long

        //check helper
        try {
            val result = getApiClient().ping()
            if (result.isSuccessful && result.body()?.pong == true) {
                AppConfigStore.config = AppConfigStore.config.copy(
                    enablePlugin = true
                )
            }
        } catch (
            e: Exception
        ) {
            e.printStackTrace()
        }



        startTime = System.currentTimeMillis()
        if (AppConfigStore.config.isInitPrompt) {
            PromptStore.refresh(context)
            AppConfigStore.config = AppConfigStore.config.copy(isInitPrompt = false)
            AppConfigStore.saveData(context)
        }
        endTime = System.currentTimeMillis()
        Log.d("initViewModel", "Call time for AppConfigStore operations: ${endTime - startTime} ms")
        startTime = System.currentTimeMillis()
        val latestHistory = HistoryStore.getLatestHistory(context)
        if (latestHistory !== null) {
            applyHistory(context, latestHistory)
        }
        endTime = System.currentTimeMillis()
        Log.d("initViewModel", "Call time for load latest history: ${endTime - startTime} ms")

        startTime = System.currentTimeMillis()
        samplerList = fetchDataFromApi()
        endTime = System.currentTimeMillis()
        Log.d("initViewModel", "Call time for fetchDataFromApi: ${endTime - startTime} ms")

        startTime = System.currentTimeMillis()
        models = fetchModelFromApi()
        ModelStore.insertNameIfNotExistMany(context, models.map { it.modelName })
        endTime = System.currentTimeMillis()
        Log.d("initViewModel", "Call time for fetchModelFromApi: ${endTime - startTime} ms")

        startTime = System.currentTimeMillis()
        vaeList = fetchVaeFromApi()
        endTime = System.currentTimeMillis()
        Log.d("initViewModel", "Call time for fetchVaeFromApi: ${endTime - startTime} ms")

        startTime = System.currentTimeMillis()
        upscalers = fetchUpscalersFromApi()
        endTime = System.currentTimeMillis()
        Log.d("initViewModel", "Call time for fetchUpscalersFromApi: ${endTime - startTime} ms")

        startTime = System.currentTimeMillis()
        loraList = loadLora(context)
        endTime = System.currentTimeMillis()
        Log.d("initViewModel", "Call time for fetchLoraFromApi: ${endTime - startTime} ms")

        startTime = System.currentTimeMillis()
        embeddingModels = fetchEmbeddingFromApi()
        endTime = System.currentTimeMillis()
        Log.d("initViewModel", "Call time for fetchEmbeddingFromApi: ${endTime - startTime} ms")

        startTime = System.currentTimeMillis()
        reactorUpscalerList = fetchReactorUpscalerFromApi()
        endTime = System.currentTimeMillis()
        Log.d(
            "initViewModel",
            "Call time for fetchReactorUpscalerFromApi: ${endTime - startTime} ms"
        )

        startTime = System.currentTimeMillis()
        reactorModelList = fetchReactorModelFromApi()
        if (reactorModelList.isNotEmpty()) {
            reactorParam = reactorParam.copy(model = reactorModelList.first())
        }
        endTime = System.currentTimeMillis()
        Log.d(
            "initViewModel",
            "Call time for fetchReactorModelFromApi: ${endTime - startTime} ms"
        )

        startTime = System.currentTimeMillis()
        adetailerModelList = fetchAdetailerModelFromApi()
        endTime = System.currentTimeMillis()
        Log.d(
            "initViewModel",
            "Call time for fetchAdetailerModelFromApi: ${endTime - startTime} ms"
        )

        // check controlnet version
        try {
            startTime = System.currentTimeMillis()
            getApiClient().getControlNetVersion().body()
            endTime = System.currentTimeMillis()
            Log.d("initViewModel", "Call time for getControlNetVersion: ${endTime - startTime} ms")
            enableControlNetFeat = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (enableControlNetFeat) {
            startTime = System.currentTimeMillis()
            controlNetModelList =
                getApiClient().getControlModelList().body()?.modelList ?: emptyList()
            endTime = System.currentTimeMillis()
            Log.d("initViewModel", "Call time for getControlModelList: ${endTime - startTime} ms")
        }

        startTime = System.currentTimeMillis()
        options = getApiClient().getOptions().body()
        endTime = System.currentTimeMillis()
        Log.d("initViewModel", "Call time for getOptions: ${endTime - startTime} ms")
        useModelName = options?.sdModelCheckpoint
        useVae = options?.sdVae
    }

    suspend fun switchModel(modelName: String) {
        isSwitchingModel = true
        getApiClient().setOptions(
            OptionsRequestBody(
                sdModelCheckpoint = modelName
            )
        )
        useModelName = modelName
        isSwitchingModel = false

    }

    suspend fun switchVae(modelName: String) {
        isSwitchingModel = true
        getApiClient().setOptions(
            OptionsRequestBody(
                sdVae = modelName
            )
        )
        useVae = modelName
        isSwitchingModel = false
    }
}