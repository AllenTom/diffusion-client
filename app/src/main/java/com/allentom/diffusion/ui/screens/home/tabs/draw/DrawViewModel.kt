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
import com.allentom.diffusion.api.AlwaysonScripts
import com.allentom.diffusion.api.ControlNetParam
import com.allentom.diffusion.api.ControlNetWrapper
import com.allentom.diffusion.api.Img2ImgRequest
import com.allentom.diffusion.api.OptionsRequestBody
import com.allentom.diffusion.api.Txt2ImgRequest
import com.allentom.diffusion.api.entity.ApiError
import com.allentom.diffusion.api.entity.ApiException
import com.allentom.diffusion.api.entity.Embedding
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.api.entity.Model
import com.allentom.diffusion.api.entity.Option
import com.allentom.diffusion.api.entity.Progress
import com.allentom.diffusion.api.entity.Sampler
import com.allentom.diffusion.api.entity.Upscale
import com.allentom.diffusion.api.entity.throwApiExceptionIfNotSuccessful
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.service.GenerateImageService
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.EmbeddingPrompt
import com.allentom.diffusion.store.HistoryStore
import com.allentom.diffusion.store.ImageHistory
import com.allentom.diffusion.store.ImageHistoryEntity
import com.allentom.diffusion.store.Img2imgParam
import com.allentom.diffusion.store.LoraPrompt
import com.allentom.diffusion.store.ModelStore
import com.allentom.diffusion.store.Prompt
import com.allentom.diffusion.store.PromptStore
import com.allentom.diffusion.store.SaveControlNet
import com.allentom.diffusion.store.SaveHistory
import com.allentom.diffusion.store.SaveHrParam
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

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

class GenImageItem(
    val imageBase64: String?,
    val progress: Progress?,
    val seed: Int?,
    val imageName: String,
    val error: ApiError?,
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

    fun copy(
        imageBase64: String? = this.imageBase64,
        progress: Progress? = this.progress,
        seed: Int? = this.seed,
        imageName: String = this.imageName,
        error: ApiError? = this.error
    ): GenImageItem {
        return GenImageItem(imageBase64, progress, seed, imageName, error)
    }
}

object DrawViewModel {
    var genScope: CoroutineScope? = null
    var inputPromptText by mutableStateOf<List<Prompt>>(
        "1girl,serafuku,classroom".split(",").map { Prompt(it, 0) })
    var inputNegativePromptText by mutableStateOf<List<Prompt>>(
        "nsfw".split(",").map { Prompt(it, 0) })
    var embeddingList by mutableStateOf<List<EmbeddingPrompt>>(emptyList())
    var embeddingModels by mutableStateOf(emptyMap<String, Embedding>())
    var inputWidth by mutableFloatStateOf(300f)
    var inputHeight by mutableFloatStateOf(512f)
    var inputSteps by mutableFloatStateOf(20f)
    var inputNiter by mutableFloatStateOf(2f)
    var inputSamplerName by mutableStateOf("DDIM")
    var inputCfgScale by mutableFloatStateOf(7f)
    var inputSeed by mutableIntStateOf(-1)
    var samplerList by mutableStateOf(emptyList<Sampler>())
    var progress by mutableStateOf<Progress?>(null)
    var models by mutableStateOf<List<Model>>(emptyList())
    var isGenerating by mutableStateOf(false)
    var options by mutableStateOf<Option?>(null)
    var useModelName by mutableStateOf<String?>(null)
    var isSwitchingModel by mutableStateOf(false)
    var currentGenIndex by mutableStateOf(0)
    var totalGenCount by mutableStateOf(0)
    var genItemList by mutableStateOf<List<GenImageItem>>(emptyList())
    var displayResultIndex by mutableStateOf(0)
    var inputEnableHiresFix by mutableStateOf(false)
    var inputHrScale by mutableFloatStateOf(2f)
    var inputHrSteps by mutableFloatStateOf(0f)
    var inputHrDenoisingStrength by mutableFloatStateOf(0.7f)
    var inputUpscaler by mutableStateOf<String>("None")
    var upscalers by mutableStateOf<List<Upscale>>(emptyList())
    var loraList by mutableStateOf<List<Lora>>(emptyList())
    var inputLoraList by mutableStateOf<List<LoraPrompt>>(emptyList())
    var inputControlNetEnable by mutableStateOf(false)
    var inputControlNetGuidanceStart by mutableFloatStateOf(0f)
    var inputControlNetGuidanceEnd by mutableFloatStateOf(1f)
    var inputControlNetControlMode by mutableIntStateOf(0)
    var inputControlNetControlWeight by mutableFloatStateOf(1f)
    var inputControlNetControlModel by mutableStateOf<String?>(null)
    var inputContentNetImageBase64 by mutableStateOf<String?>(null)
    var inputContentNetImagePath by mutableStateOf<String?>(null)
    val ControlNetModeList =
        listOf("Balanced", "My prompt is more important", "ControlNet is more important")
    var controlNetModelList by mutableStateOf<List<String>>(emptyList())
    var enableControlNetFeat by mutableStateOf(false)
    var inputImg2ImgImgBase64 by mutableStateOf<String?>(null)
    var inputImg2ImgImgFilename by mutableStateOf<String?>(null)
    var generateMode by mutableStateOf("text2img")
    var inputImg2ImgDenoisingStrength by mutableFloatStateOf(0.7f)
    var inputImg2ImgResizeMode by mutableIntStateOf(0)
    var inputImg2ImgResizeModeList =
        listOf("Just resize", "Crop and resize", "Resize and fill", "Just resize (latent upscale)")
    var inputImg2ImgScaleBy by mutableFloatStateOf(1f)
    var inputImg2ImgWidth by mutableFloatStateOf(512f)
    var inputImg2ImgHeight by mutableFloatStateOf(512f)
    var inputImg2ImgCfgScale by mutableFloatStateOf(7f)
    var currentHistory by mutableStateOf<SaveHistory?>(null)
    fun startGenerating(count: Int) {
        isGenerating = true
        totalGenCount = count
        currentGenIndex = 0
        displayResultIndex = 0
        genItemList = emptyList()
        for (i in 0 until count) {
            val imageName = "diffusion_${i}_${System.currentTimeMillis()}.jpg"
            genItemList = genItemList + GenImageItem(null, null, null, imageName, null)
        }
        currentHistory = null
    }

    var interruptFlag = false
    var skipFlag = false
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
        return inputPromptText.map { it.getPromptText() }.joinToString(",")
    }

    fun getNegativePrompt(): String {
        return (inputNegativePromptText.map { it.getPromptText() } + embeddingList.map { it.getPromptText() }).joinToString(
            ","
        )
    }

    fun getLoraPrompt(): String {
        return inputLoraList.joinToString(",") { it.getPromptText().joinToString(",") }
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


        inputUpscaler = history.hrParam.hrUpscaler
        inputHrScale = history.hrParam.hrScale
        inputHrDenoisingStrength = history.hrParam.hrDenosingStrength
        inputEnableHiresFix = history.hrParam.enableScale

        history.img2imgParam?.let { img2imgParam: Img2imgParam ->
            inputImg2ImgWidth = img2imgParam.width.toFloat()
            inputImg2ImgHeight = img2imgParam.height.toFloat()
            inputImg2ImgCfgScale = img2imgParam.cfgScale
            inputImg2ImgDenoisingStrength = img2imgParam.denoisingStrength
            inputImg2ImgResizeMode = img2imgParam.resizeMode
            inputImg2ImgScaleBy = img2imgParam.scaleBy
            inputImg2ImgImgFilename = img2imgParam.path
            inputImg2ImgImgBase64 = Util.readImageWithPathToBase64(img2imgParam.path)
        }

        history.controlNetParam?.let {
            inputControlNetEnable = it.enabled
            inputControlNetGuidanceStart = it.guidanceStart
            inputControlNetGuidanceEnd = it.guidanceEnd
            inputControlNetControlMode = it.controlMode
            inputControlNetControlWeight = it.weight
            inputControlNetControlModel = it.model
            inputContentNetImageBase64 = it.inputImage
            inputContentNetImagePath = it.inputImagePath
        }
        // for lora
        inputLoraList = history.loraPrompt
    }

    fun applyControlNetParams(context: Context, history: SaveControlNet) {
        val imageBase64 = Util.readImageWithPathToBase64(history.path)
        inputContentNetImageBase64 = imageBase64
        inputContentNetImagePath = history.path
        // find history use
        val saveControlNetHistory =
            HistoryStore.findLatestControlNetUse(context = context, controlNetId = history.id)
        saveControlNetHistory?.let {
            inputControlNetControlMode = it.controlMode
            inputControlNetControlWeight = it.weight
            inputControlNetGuidanceStart = it.guidanceStart
            inputControlNetGuidanceEnd = it.guidanceEnd
            inputControlNetEnable = true
            inputControlNetControlModel = it.model
        }
    }

    suspend fun text2Image(
        prompt: String,
        negativePrompt: String,
        width: Int,
        height: Int,
        steps: Int,
        samplerName: String,
        nIter: Int,
        cfgScale: Float,
        seed: Int,
        enableScale: Boolean = false,
        hrScale: Float = 2f,
        hrDenosingStrength: Float = 0.7f,
        hrUpscaler: String? = "None",
        controlNetParam: ControlNetParam? = null,
    ): String? {
        var alwaysonScripts: AlwaysonScripts? = null
        if (controlNetParam != null) {
            alwaysonScripts = AlwaysonScripts(
                controlNet = ControlNetWrapper(
                    args = listOf(controlNetParam)
                )
            )
        }
        val request = Txt2ImgRequest(
            prompt = prompt,
            negative_prompt = negativePrompt,
            width = width,
            height = height,
            steps = steps,
            sampler_name = samplerName,
            n_iter = 1,
            cfg_scale = cfgScale.toInt(),
            seed = seed,
            enable_hr = enableScale,
            hr_scale = hrScale,
            denoising_strength = hrDenosingStrength,
            hr_upscaler = hrUpscaler ?: "None",
            alwayson_scripts = alwaysonScripts
        )
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

    suspend fun img2Image(
        prompt: String,
        negativePrompt: String,
        width: Int,
        height: Int,
        steps: Int,
        samplerName: String,
        cfgScale: Float,
        seed: Int,
        imgBase64: String?,
        resizeMode: Int?,
        denoisingStrength: Float?,
        scaleBy: Float?,

        ): String? {
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
            notificationCompat.apply {
//                if (ActivityCompat.checkSelfPermission(
//                        context,
//                        Manifest.permission.POST_NOTIFICATIONS
//                    ) == PackageManager.PERMISSION_GRANTED
//                ) {
//                    notify(1, builder.build())
//                }
            }
            try {
                val genTotalCount = refreshIndex.let {
                    if (it == null) {
                        inputNiter.toInt()
                    } else {
                        1
                    }

                }
                if (refreshIndex == null) {
                    startGenerating(genTotalCount)
                }
                val saveImagePaths = emptyList<ImageHistory>().toMutableList()
                var controlNetParam: ControlNetParam? = null
                if (enableControlNetFeat && inputControlNetEnable) {
                    controlNetParam = ControlNetParam(
                        enabled = inputControlNetEnable,
                        guidanceStart = inputControlNetGuidanceStart,
                        guidanceEnd = inputControlNetGuidanceEnd,
                        controlMode = inputControlNetControlMode,
                        weight = inputControlNetControlWeight,
                        inputImage = inputContentNetImageBase64 ?: "",
                        model = inputControlNetControlModel ?: "",
                        inputImagePath = inputContentNetImagePath
                    )
                }
                for (genIndex in 0 until genTotalCount) {


                    val seed = inputSeed.let {
                        if (refreshIndex != null) {
                            return@let genItemList[refreshIndex].seed!!
                        }
                        if (it == -1) {
                            (0..100000000).random()
                        } else {
                            it
                        }
                    }
                    if (refreshIndex != null) {
                        currentGenIndex = refreshIndex
                    } else {
                        currentGenIndex = genIndex
                    }
                    updateGenItemByIndex(currentGenIndex) {
                        it.copy(seed = seed)
                    }
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
                            text2Image(
                                prompt = listOf(
                                    getPositivePrompt(),
                                    getLoraPrompt()
                                ).joinToString(","),
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
                                controlNetParam = controlNetParam
                            )?.let {
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
                            img2Image(
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
                            )?.let {
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
                    }


                }
//                var controlNetParam: ControlNetParam? = null
//                if (controlNetParam != null) {
////                    ControlNetStore.addNet(controlNetParam)
////                    ControlNetStore.saveData(context)
//                }
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
                    controlNetParam = controlNetParam
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
            PromptStore.refresh(context, true)
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
}