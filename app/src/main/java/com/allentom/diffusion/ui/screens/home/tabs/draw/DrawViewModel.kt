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
import com.allentom.diffusion.composables.TemplateItem
import com.allentom.diffusion.service.GenerateImageService
import com.allentom.diffusion.service.Img2imgGenerateParam
import com.allentom.diffusion.service.Text2ImageParam
import com.allentom.diffusion.service.img2Image
import com.allentom.diffusion.service.text2Image
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.prompt.EmbeddingPrompt
import com.allentom.diffusion.store.history.HistoryStore
import com.allentom.diffusion.store.history.ImageHistory
import com.allentom.diffusion.store.history.ImageHistoryEntity
import com.allentom.diffusion.store.history.SavedImg2imgParam
import com.allentom.diffusion.store.prompt.LoraPrompt
import com.allentom.diffusion.store.ModelStore
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.store.prompt.PromptStore
import com.allentom.diffusion.store.SaveControlNet
import com.allentom.diffusion.store.history.SaveHistory
import com.allentom.diffusion.store.history.SaveHrParam
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

data class TemplateParam(
    val template: List<TemplateItem> = emptyList(),
    val generateResult: List<Prompt> = emptyList()
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
)

data class BaseParam(
    val promptText: List<Prompt> = "1girl,serafuku,classroom".split(",").map { Prompt(it, 0) },
    val negativePromptText: List<Prompt> = "nsfw".split(",").map { Prompt(it, 0) },
    val embeddingModels: Map<String, Embedding> = emptyMap(),
    val width: Int = 300,
    val height: Int = 512,
    val steps: Int = 20,
    val niter: Int = 3,
    val samplerName: String = "DDIM",
    val cfgScale: Float = 7f,
    val seed: Int = -1,
    val useVae: String? = null,
    val enableRefiner: Boolean = false,
    val refinerModel: String? = null,
    val refinerSwitchAt: Float = 0.8f,
    val baseModelName: String? = null,
    val loraPrompt: List<LoraPrompt> = emptyList(),
    val embeddingPrompt: List<EmbeddingPrompt> = emptyList(),
)

data class Img2ImgParam(
    val imgBase64: String? = null,
    val resizeMode: Int = 0,
    val scaleBy: Float = 1f,
    val width: Int = 512,
    val height: Int = 512,
    val cfgScale: Float = 7f,
    val mask: String? = null,
    var inpaint: Boolean = false,
    val maskBlur: Float = 4f,
    val inpaintingMaskInvert: Int = 0,
    val inpaintingFill: Int = 0,
    val inpaintingFullRes: Int = 0,
    val inpaintingFullResPadding: Int = 0,
    val denoisingStrength: Float = 0.4f,
    val imgFilename: String? = null,
)

class GenerateTask(
    val context: Context,
    var genScope: CoroutineScope? = null,
    val baseParam: BaseParam,
    val xyzParam: XYZParam = XYZParam(),
    val controlNetParam: ControlNetParam? = null,
    val generateMode: String = "text2img",
    val hiresFixParam: SaveHrParam,
    val regionPromptParam: RegionPromptParam = RegionPromptParam(),
    val reactorParam: ReactorParam = ReactorParam(),
    val adetailerParam: AdetailerParam = AdetailerParam(),
    val img2ImgParam: Img2ImgParam = Img2ImgParam(),
    val id: String = Util.randomString(8),
    val createTime: Long = System.currentTimeMillis(),
    var alreadyRunFlag: Boolean = false
) {
    var currentGenIndex by mutableStateOf(0)
    var totalGenCount by mutableStateOf(0)

    var genItemList by mutableStateOf<List<GenImageItem>>(emptyList())
    var displayResultIndex by mutableStateOf(0)
    var isGenerating by mutableStateOf(false)
    var interruptFlag by mutableStateOf(false)
    var genXYZ by mutableStateOf<DisplayAxis?>(null)
    var currentHistory by mutableStateOf<SaveHistory?>(null)


    fun startGenerating(
        count: Int,
        seed: Int = -1
    ) {
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
            genItemList += GenImageItem(null, null, imageSeed, imageName, null)
        }
        currentHistory = null
        if (xyzParam.enable) {
            genXYZ = xyzParam.getDisplayAxis()
        } else {
            genXYZ = null
        }
    }

    fun updateGenItemByIndex(index: Int, update: (item: GenImageItem) -> GenImageItem) {
        genItemList = genItemList.toMutableList().also {
            it[index] = update(it[index])
        }

    }

    fun getNegativePrompt(): String {
        return (baseParam.negativePromptText.map { it.getPromptText() } + baseParam.embeddingPrompt.map { it.getPromptText() }).joinToString(
            ","
        )
    }

    fun getLoraPrompt(): String {
        return baseParam.loraPrompt.joinToString(",") {
            it.getPromptText().joinToString(",")
        }
    }

    fun getPositiveWithRegion(): String {
        var promptTextList = mutableListOf<String>()
        val maxRegion = regionPromptParam.getTotalRegionCount()
        for (i in 0 until maxRegion) {
            val regionText =
                baseParam.promptText.filter { it.regionIndex == i }.map { it.getPromptText() }
                    .joinToString(",")
            promptTextList.add(regionText)
        }
        return promptTextList.joinToString(" BREAK\n")
    }

    fun getPositivePrompt(): String {
        if (regionPromptParam.regionCount > 0 && regionPromptParam.enable) {
            return getPositiveWithRegion()
        }
        return baseParam.promptText.joinToString(",") { it.getPromptText() }
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

    fun initTask() {
        val genTotalCount = if (xyzParam.enable) {
            xyzParam.totalCount
        } else {
            baseParam.niter
        }
        var useSeed = baseParam.seed
        if (xyzParam.enable) {
            useSeed = (0..100000000).random()
        }
        startGenerating(genTotalCount, useSeed)
    }

    suspend fun generateImage() {
        alreadyRunFlag = true
        val builder = NotificationCompat.Builder(context, "gen_ch")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.generating))
            .setContentText(context.getString(R.string.generating))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(100, 0, false)
            .setOngoing(true)
        val notificationCompat = NotificationManagerCompat.from(context)
        try {
            isGenerating = true
            val genTotalCount = totalGenCount

            val saveImagePaths = emptyList<ImageHistory>().toMutableList()
            for (genIndex in 0 until genTotalCount) {
                currentGenIndex = genIndex
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
                            width = baseParam.width,
                            height = baseParam.height,
                            steps = baseParam.steps,
                            samplerName = baseParam.samplerName,
                            nIter = baseParam.niter,
                            cfgScale = baseParam.cfgScale,
                            seed = seed,
                            hiresFixParam = hiresFixParam,
                            controlNetParam = controlNetParam,
                            regionPromptParam = regionPromptParam,
                            refinerModel = if (baseParam.enableRefiner) baseParam.refinerModel else null,
                            refinerSwitchAt = if (baseParam.enableRefiner) baseParam.refinerSwitchAt else null,
                            reactorParam = reactorParam,
                            adetailerParam = adetailerParam
                        )
                        val xIndex = xyzParam.getXAxisIndex(currentGenIndex)
                        val yIndex = xyzParam.getYAxisIndex(currentGenIndex)
                        xIndex?.let {
                            param =
                                xyzParam.xAxis?.onText2ImageParamChange(param, xIndex) ?: param
                        }
                        yIndex?.let {
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
                            width = baseParam.width,
                            height = baseParam.height,
                            steps = baseParam.steps,
                            samplerName = baseParam.samplerName,
                            cfgScale = baseParam.cfgScale,
                            seed = seed,
                            imgBase64 = img2ImgParam.imgBase64,
                            resizeMode = img2ImgParam.resizeMode,
                            denoisingStrength = img2ImgParam.denoisingStrength,
                            scaleBy = img2ImgParam.scaleBy,
                            mask = if (img2ImgParam.inpaint) img2ImgParam.mask
                                ?: "" else "",
                            inpaintingMaskInvert = img2ImgParam.inpaintingMaskInvert,
                            maskBlur = img2ImgParam.maskBlur,
                            inpaintingFill = img2ImgParam.inpaintingFill,
                            inpaintFullRes = img2ImgParam.inpaintingFullRes,
                            inpaintFullResPadding = img2ImgParam.inpaintingFullResPadding,
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
                        baseParam.promptText.map { it.text },
                    )
                    PromptStore.updatePrompt(
                        context,
                        baseParam.negativePromptText.map { it.text },
                    )
                }

            }
            val savedImg2ImgParam: SavedImg2imgParam? = SavedImg2imgParam.create(
                context,
                img2ImgParam
            )

            val saveHistory = SaveHistory(
                prompt = baseParam.promptText,
                negativePrompt = baseParam.negativePromptText,
                steps = baseParam.steps,
                samplerName = baseParam.samplerName,
                sdModelCheckpoint = baseParam.baseModelName ?: "",
                width = baseParam.width,
                height = baseParam.height,
                batchSize = 1,
                time = System.currentTimeMillis(),
                imagePaths = saveImagePaths,
                cfgScale = baseParam.cfgScale,
                loraPrompt = baseParam.loraPrompt,
                embeddingPrompt = baseParam.embeddingPrompt,
                hrParam = hiresFixParam,
                savedImg2ImgParam = savedImg2ImgParam,
                controlNetParam = controlNetParam,
                regionRatio = regionPromptParam.dividerText,
                regionCount = regionPromptParam.regionCount,
                regionUseCommon = regionPromptParam.useCommon,
                regionEnable = regionPromptParam.enable,
                vaeName = baseParam.useVae,
                enableRefiner = baseParam.enableRefiner,
                refinerModelName = baseParam.refinerModel,
                refinerSwitchAt = baseParam.refinerSwitchAt,
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

data class TaskRunner(
    var queue: MutableList<GenerateTask> = mutableListOf(),
    var currentIndex: Int = 0
) {
    fun startTask(context: Context) {
        val act = context as Activity
        val intent = Intent(context, GenerateImageService::class.java)
        intent.putExtra("refreshIndex", -1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            act.startService(intent)
        }
    }


    val isGenerating: Boolean
        get() {
            return queue.any { it.isGenerating }
        }

    fun updateCurrentTask(update: (task: GenerateTask) -> GenerateTask) {
        queue[currentIndex] = update(queue[currentIndex])
    }

    fun updateTaskById(id: String, update: (task: GenerateTask) -> GenerateTask) {
        queue.find { it.id == id }?.let {
            val index = queue.indexOf(it)
            queue[index] = update(queue[index])
        }
    }

    val currentTask: GenerateTask?
        get() {
            return queue.getOrNull(currentIndex)
        }
    val unRunTask: List<GenerateTask>
        get() {
            return queue.filter { !it.alreadyRunFlag }
        }

    fun getNextUnRunTask(): GenerateTask? {
        return unRunTask.firstOrNull()
    }

    fun hasUnRunTask(): Boolean {
        return unRunTask.isNotEmpty()
    }

    fun cancelTaskById(id: String) {
        queue = queue.filter { it.id != id }.toMutableList()
    }
}


object DrawViewModel {
    var baseParam by mutableStateOf<BaseParam>(BaseParam())
    var regionPromptParam by mutableStateOf<RegionPromptParam>(RegionPromptParam())
    var embeddingModels by mutableStateOf(emptyMap<String, Embedding>())
    var samplerList by mutableStateOf(emptyList<Sampler>())
    var progress by mutableStateOf<Progress?>(null)
    var models by mutableStateOf<List<Model>>(emptyList())
    var vaeList by mutableStateOf<List<Vae>>(emptyList())
    var useVae by mutableStateOf<String?>(null)
    val isGenerating: Boolean
        get() {
            return runningTask?.isGenerating ?: false
        }
    var options by mutableStateOf<Option?>(null)
    var useModelName by mutableStateOf<String?>(null)
    var isSwitchingModel by mutableStateOf(false)
    var genXYZ by mutableStateOf<DisplayAxis?>(null)

    // hires fix
    var inputHiresFixParam by mutableStateOf<SaveHrParam>(
        SaveHrParam(
            enableScale = false,
            hrScale = 2f,
            hrDenosingStrength = 0.7f,
            hrUpscaler = "None",
        )
    )
    var upscalers by mutableStateOf<List<Upscale>>(emptyList())

    //lora
    var loraList by mutableStateOf<List<Lora>>(emptyList())

    var inputControlNetParams by mutableStateOf(ControlNetParam())
    var controlNetModelList by mutableStateOf<List<String>>(emptyList())

    var enableControlNetFeat by mutableStateOf(false)
    var generateMode by mutableStateOf("text2img")

    var img2ImgParam by mutableStateOf(Img2ImgParam())
    var inputImg2ImgMaskPreview by mutableStateOf<String?>(null)

    var reactorParam by mutableStateOf(ReactorParam())
    var reactorUpscalerList by mutableStateOf<List<String>>(emptyList())
    var reactorModelList by mutableStateOf<List<String>>(emptyList())


    var adetailerParam by mutableStateOf(AdetailerParam())
    var adetailerModelList by mutableStateOf<List<String>>(emptyList())
    var xyzParam by mutableStateOf(XYZParam())
    var templateParam by mutableStateOf(TemplateParam())
    var negativeTemplateParam by mutableStateOf(TemplateParam())

    val gson = Gson()
    var currentGenTaskId by mutableStateOf<String?>(null)
    var pinRunningTask by mutableStateOf(true)


    fun applyHistory(context: Context, useHistory: SaveHistory) {
        val history = HistoryStore.getHistoryById(context, useHistory.id) ?: return
        baseParam = baseParam.copy(
            promptText = history.prompt,
            negativePromptText = history.negativePrompt,
            embeddingPrompt = history.embeddingPrompt,
            steps = history.steps,
            width = history.width,
            height = history.height,
            samplerName = history.samplerName,
            cfgScale = history.cfgScale,
            enableRefiner = history.enableRefiner,
            refinerModel = history.refinerModelName,
            refinerSwitchAt = history.refinerSwitchAt ?: 0.8f,
            loraPrompt = history.loraPrompt,
        )
        regionPromptParam = RegionPromptParam(
            regionCount = history.regionCount ?: 1,
            dividerText = history.regionRatio ?: "1",
            useCommon = history.regionUseCommon ?: false,
            enable = history.regionEnable ?: false
        )


        inputHiresFixParam = history.hrParam


        history.savedImg2ImgParam?.let { savedImg2ImgParam: SavedImg2imgParam ->
            val inputImageBase64 = Util.readImageWithPathToBase64(savedImg2ImgParam.path)
            img2ImgParam = img2ImgParam.copy(
                imgBase64 = inputImageBase64,
                resizeMode = savedImg2ImgParam.resizeMode,
                scaleBy = savedImg2ImgParam.scaleBy,
                width = savedImg2ImgParam.width,
                height = savedImg2ImgParam.height,
                cfgScale = savedImg2ImgParam.cfgScale,
                denoisingStrength = savedImg2ImgParam.denoisingStrength ?: 0.4f,
                imgFilename = savedImg2ImgParam.path,
            )
            if (savedImg2ImgParam.inpaint == true) {
                val maskBase64 = Util.readImageWithPathToBase64(savedImg2ImgParam.maskPath!!)
                img2ImgParam = img2ImgParam.copy(
                    inpaint = true,
                    mask = maskBase64,
                    maskBlur = savedImg2ImgParam.maskBlur ?: 4f,
                    inpaintingMaskInvert = savedImg2ImgParam.maskInvert ?: 0,
                    inpaintingFill = savedImg2ImgParam.inpaintingFill ?: 0,
                    inpaintingFullRes = savedImg2ImgParam.inpaintingFullRes ?: 0,
                    inpaintingFullResPadding = savedImg2ImgParam.inpaintingFullResPadding ?: 32
                )
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

    fun startGenerate(context: Context) {
        runTask(context)
    }

    var runningTask by mutableStateOf<TaskRunner?>(null)
    fun runTask(context: Context) {
        val task = GenerateTask(
            context = context,
            baseParam = baseParam,
            xyzParam = xyzParam,
            controlNetParam = inputControlNetParams,
            generateMode = generateMode,
            hiresFixParam = inputHiresFixParam,
            regionPromptParam = regionPromptParam,
            reactorParam = reactorParam,
            adetailerParam = adetailerParam,
            img2ImgParam = img2ImgParam
        )
        if (runningTask == null) {
            runningTask = TaskRunner()
        }
        runningTask = runningTask!!.copy(
            queue = (runningTask!!.queue + task.apply {
                initTask()
            }).toMutableList()
        )
//        runningTask = TaskRunner().apply {
//            queue.add(task)
//        }
        if (!runningTask!!.isGenerating) {
            runningTask!!.startTask(context)
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
        baseParam.promptText.none { it.text == text }.let {
            if (it) {
                baseParam = baseParam.copy(
                    promptText = baseParam.promptText + Prompt(text, 0)
                )
            }
        }
    }

    fun addInputPrompt(prompt: Prompt, regionIndex: Int? = 0) {

        baseParam.promptText.none {
            if (regionIndex != null) {
                return@none it.text == prompt.text && it.regionIndex == regionIndex

            } else {
                return@none it.text == prompt.text
            }
        }.let {
            if (it) {
                baseParam = baseParam.copy(
                    promptText = baseParam.promptText + prompt
                )
            }
        }
    }

    fun replaceInputPrompt(promptList: List<Prompt>, regionIndex: Int = 0) {
        var newPrompt = baseParam.promptText.filter { it.regionIndex != regionIndex }
        newPrompt = newPrompt + promptList.map { it.copy(regionIndex = regionIndex) }
        baseParam = baseParam.copy(
            promptText = newPrompt
        )
    }


    fun removeInputPrompt(text: String) {
        baseParam = baseParam.copy(
            promptText = baseParam.promptText.filter { it.text != text }
        )
    }

    fun addInputNegativePrompt(text: String) {
        baseParam.negativePromptText.none { it.text == text }.let {
            if (it) {
                baseParam = baseParam.copy(
                    negativePromptText = baseParam.negativePromptText + Prompt(text, 0)
                )
            }
        }
    }

    fun addInputNegativePrompt(prompt: Prompt, regionIndex: Int? = 0) {
        baseParam.negativePromptText.none {
            if (regionIndex != null) {
                return@none it.text == prompt.text && it.regionIndex == regionIndex

            } else {
                return@none it.text == prompt.text
            }
        }.let {
            if (it) {
                baseParam = baseParam.copy(
                    negativePromptText = baseParam.negativePromptText + prompt
                )
            }
        }
    }

    fun replaceInputNegativePrompt(promptList: List<Prompt>, regionIndex: Int = 0) {
        var newPrompt = baseParam.negativePromptText.filter { it.regionIndex != regionIndex }
        newPrompt = newPrompt + promptList.map { it.copy(regionIndex = regionIndex) }
        baseParam = baseParam.copy(
            negativePromptText = newPrompt
        )
    }

    fun removeInputNegativePrompt(text: String) {
        baseParam = baseParam.copy(
            negativePromptText = baseParam.negativePromptText.filter { it.text != text }
        )
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