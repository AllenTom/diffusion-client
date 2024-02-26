package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.allentom.diffusion.Util
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.prompt.EmbeddingEntity
import com.allentom.diffusion.store.prompt.EmbeddingPrompt
import com.allentom.diffusion.store.prompt.LoraPrompt
import com.allentom.diffusion.store.prompt.LoraPromptEntity
import com.allentom.diffusion.store.ModelEntity
import com.allentom.diffusion.store.ModelStore
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.store.prompt.SavePrompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.AdetailerParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.BaseParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.ControlNetParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.XYZParam
import java.io.Serializable

class ImageHistory(
    val imageHistoryId: Long = 0,
    val path: String,
    val name: String,
    val seed: Long,
    val favourite: Boolean = false,
    val historyId: Long
) : Serializable

data class SaveHistory(
    val id: Long = 0,
    val prompt: List<Prompt>,
    val negativePrompt: List<Prompt>,
    var loraPrompt: List<LoraPrompt>,
    val embeddingPrompt: List<EmbeddingPrompt> = emptyList(),
    val steps: Int,
    val samplerName: String,
    val sdModelCheckpoint: String,
    val width: Int,
    val height: Int,
    val batchSize: Int,
    val cfgScale: Float,
    val time: Long,
    val imagePaths: List<ImageHistory>,
    val hrParam: SaveHrParam,
    val savedImg2ImgParam: SavedImg2imgParam? = null,
    var controlNetParam: ControlNetParam? = null,
    val model: ModelEntity? = null,
    val regionRatio: String? = "",
    val regionCount: Int? = 0,
    val regionUseCommon: Boolean? = false,
    val regionEnable: Boolean? = false,
    val vaeName: String? = null,
    val enableRefiner: Boolean = false,
    val refinerModelName: String? = null,
    val refinerSwitchAt: Float? = null,
    val reactorParam: ReactorParam? = null,
    val adetailerParam: AdetailerParam? = null,
    val xyzParam: XYZParam? = null
) : Serializable

@Entity(tableName = "image_history")
data class ImageHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val imageHistoryId: Long = 0,
    val name: String,
    val path: String,
    val favourite: Boolean,
    val seed: Long,
    val historyId: Long,
) {
    companion object {
        fun fromImageHistory(imageHistory: ImageHistory, historyId: Long): ImageHistoryEntity {
            return ImageHistoryEntity(
                imageHistoryId = imageHistory.imageHistoryId,
                path = imageHistory.path,
                seed = imageHistory.seed,
                historyId = historyId,
                favourite = imageHistory.favourite,
                name = imageHistory.name,
            )
        }
    }

    fun toImageHistory(): ImageHistory {
        return ImageHistory(
            imageHistoryId = imageHistoryId,
            path = path,
            seed = seed,
            name = name,
            favourite = favourite,
            historyId = historyId,
        )
    }
}

@Dao
interface ImageHistoryDao {
    @Insert
    fun insert(imageHistoryEntity: ImageHistoryEntity)

    @Update
    fun update(imageHistoryEntity: ImageHistoryEntity)

    @Query("SELECT * FROM image_history WHERE name = :name")
    fun getImageHistoryWithName(name: String): ImageHistoryEntity?

    @Query("SELECT * FROM image_history WHERE favourite = 1 ORDER BY imageHistoryId DESC")
    fun getFavouriteImageHistory(): List<ImageHistoryEntity>

}

data class HistoryWithRelation(
    @Embedded val historyEntity: HistoryEntity,
    @Relation(
        parentColumn = "historyId",
        entityColumn = "promptId",
        associateBy = Junction(PromptHistoryCrossRef::class)
    )
    val prompts: List<SavePrompt> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "promptId",
        associateBy = Junction(NegativePromptHistoryCrossRef::class)
    )
    val negativePrompts: List<SavePrompt> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "loraPromptId",
        associateBy = Junction(LoraPromptHistoryCrossRef::class)
    )
    val loraPrompts: List<LoraPromptEntity> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "embeddingId",
        associateBy = Junction(EmbeddingHistoryCrossRef::class)
    )
    val embeddingPrompts: List<EmbeddingEntity> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val imagePaths: List<ImageHistoryEntity> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val hrParamEntity: HrHistoryEntity? = null,
    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val img2imgParam: Img2ImgEntity? = null,

    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val controlNetHistoryEntity: List<ControlNetHistoryEntity> = emptyList(),

    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val promptExtraEntity: List<PromptExtraEntity> = emptyList(),

    @Relation(
        parentColumn = "modelId",
        entityColumn = "modelId",
    )
    val modelEntity: ModelEntity? = null,
    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val adetailerEntityList: List<AdetailerEntity>? = null,

    @Relation(
        parentColumn = "historyId",
        entityColumn = "historyId",
    )
    val xyzHistoryEntity: XYZHistoryEntity? = null,
) {
    fun toSaveHistory(): SaveHistory {
        val result = SaveHistory(
            id = historyEntity.historyId,
            prompt = toPrompt(),
            negativePrompt = toNegativePrompt(),
            loraPrompt = toLoraPrompt(),
            embeddingPrompt = toEmbeddingPrompt(),
            steps = historyEntity.steps,
            samplerName = historyEntity.samplerName,
            sdModelCheckpoint = historyEntity.sdModelCheckpoint,
            width = historyEntity.width,
            height = historyEntity.height,
            batchSize = historyEntity.batchSize,
            cfgScale = historyEntity.cfgScale,
            time = historyEntity.time,
            imagePaths = imagePaths.map { it.toImageHistory() },
            hrParam = toHiresFixParam(),
            savedImg2ImgParam = img2imgParam?.toImg2imgParam(),
            controlNetParam = toControlNetParam(),
            model = modelEntity,
            regionCount = historyEntity.regionCount,
            regionRatio = historyEntity.regionRatio,
            regionUseCommon = historyEntity.regionUseCommon,
            regionEnable = historyEntity.regionEnable,
            vaeName = historyEntity.vaeName,
            enableRefiner = historyEntity.enableRefiner ?: false,
            refinerModelName = historyEntity.refinerModelName,
            refinerSwitchAt = historyEntity.refinerSwitchAt,
            reactorParam = toReactorParam(),
            adetailerParam = toAdetailerParam(),
            xyzParam = toSaveXYZParam()
        )
        return result
    }
}


@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val historyId: Long = 0,
    val steps: Int,
    val samplerName: String,
    val sdModelCheckpoint: String,
    val width: Int,
    val height: Int,
    val batchSize: Int,
    val cfgScale: Float,
    val time: Long,
    var modelId: Long? = null,
    var regionRatio: String? = "",
    var regionCount: Int? = 0,
    var regionUseCommon: Boolean? = false,
    var regionEnable: Boolean? = false,
    val enableRefiner: Boolean? = false,
    val refinerModelName: String? = null,
    val refinerSwitchAt: Float? = null,
    val vaeName: String? = null,

    val reactorEnabled: Boolean? = false,
    val reactorSingleImagePath: String? = null,
    val reactorSingleImageResultFilename: String? = null,
    val reactorGenderDetectionSource: Int? = 0,
    val reactorGenderDetectionTarget: Int? = 0,
    val reactorRestoreFace: String? = "None",
    val reactorRestoreFaceVisibility: Float? = 1f,
    val reactorCodeFormerWeightFidelity: Float? = 0.5f,
    val reactorPostprocessingOrder: Boolean? = true,
    val reactorUpscaler: String? = "None",
    val reactorScaleBy: Float? = 1f,
    val reactorUpscalerVisibility: Float? = 1f,

    ) : Serializable {

    companion object {
        fun fromSaveHistory(saveHistory: SaveHistory): HistoryEntity {
            return HistoryEntity(
                steps = saveHistory.steps,
                samplerName = saveHistory.samplerName,
                sdModelCheckpoint = saveHistory.sdModelCheckpoint,
                width = saveHistory.width,
                height = saveHistory.height,
                batchSize = saveHistory.batchSize,
                cfgScale = saveHistory.cfgScale,
                time = saveHistory.time,
                regionCount = saveHistory.regionCount,
                regionRatio = saveHistory.regionRatio,
                regionUseCommon = saveHistory.regionUseCommon,
                regionEnable = saveHistory.regionEnable,
                enableRefiner = saveHistory.enableRefiner,
                refinerModelName = saveHistory.refinerModelName,
                refinerSwitchAt = saveHistory.refinerSwitchAt,
                vaeName = saveHistory.vaeName,
            )
        }
    }
}

@Dao
interface HistoryDao {
    @Insert
    fun insert(historyEntity: HistoryEntity): Long

    @Transaction
    @Query("SELECT * FROM history ORDER BY historyId DESC")
    fun getAllHistory(): List<HistoryWithRelation>

    @Transaction
    @Query("SELECT * FROM history ORDER BY historyId DESC  limit 1")
    fun getLatestHistory(): HistoryWithRelation?

    @Transaction
    @Query("SELECT * FROM history where historyId = :id ORDER BY historyId DESC  limit 1")
    fun getHistoryById(id: Long): HistoryWithRelation?

    @Transaction
    @Query("SELECT * FROM history where historyId in (:ids) ORDER BY historyId DESC")
    fun getHistoryByIds(ids: List<Long>): List<HistoryWithRelation>
}

object HistoryStore {
    fun saveHistoryToDatabase(context: Context, historyToSave: SaveHistory) {
        var history = historyToSave
        val database = AppDatabaseHelper.getDatabase(context)
        var historyEntity = HistoryEntity.fromSaveHistory(history)
        with(history.sdModelCheckpoint) {
            // save model
            val sdModel = DrawViewModel.models.find { it.title == this }
            sdModel?.let {
                val modelEntity = ModelStore.getOrCreate(context, it.modelName)
                historyEntity.modelId = modelEntity.modelId
                modelEntity.let { saveModelEntity ->
                    history.imagePaths.firstOrNull()?.let { saveImageHistory ->
                        if (modelEntity.civitaiApiId != null) {
                            return@let
                        }
                        val previewPath = Util.saveModelPreviewToAppData(
                            context,
                            saveImageHistory.path,
                            modelEntity.name
                        )
                        ModelStore.update(
                            context,
                            saveModelEntity.copy(
                                coverPath = previewPath
                            )
                        )
                    }
                }
            }
        }

        // save reactor
        if (history.reactorParam?.enabled == true) {
            if (history.reactorParam?.singleImageResult != null && history.reactorParam?.singleImageResultFilename != null) {
                val pair = Util.saveReactorSourceFile(
                    context,
                    history.reactorParam!!.singleImageResult!!,
                    history.reactorParam!!.singleImageResultFilename!!
                )
                historyEntity = historyEntity.copy(
                    reactorSingleImagePath = pair.first,
                    reactorSingleImageResultFilename = pair.second
                )
            }
            historyEntity = historyEntity.copy(
                reactorEnabled = history.reactorParam!!.enabled,
                reactorSingleImageResultFilename = history.reactorParam!!.singleImageResultFilename,
                reactorCodeFormerWeightFidelity = history.reactorParam!!.codeFormerWeightFidelity,
                reactorGenderDetectionSource = history.reactorParam!!.genderDetectionSource,
                reactorGenderDetectionTarget = history.reactorParam!!.genderDetectionTarget,
                reactorPostprocessingOrder = history.reactorParam!!.postprocessingOrder,
                reactorRestoreFace = history.reactorParam!!.restoreFace,
                reactorRestoreFaceVisibility = history.reactorParam!!.restoreFaceVisibility,
                reactorScaleBy = history.reactorParam!!.scaleBy,
                reactorUpscaler = history.reactorParam!!.upscaler,
                reactorUpscalerVisibility = history.reactorParam!!.upscalerVisibility
            )
        }
        val savedHistoryId = database.historyDao().insert(
            historyEntity
        )
        with(history.copy(id = savedHistoryId)) {
            saveHistoryPrompt(context, PromptType.Prompt)
            saveHistoryPrompt(context, PromptType.NegativePrompt)
            saveLora(context)
            saveEmbedding(context)
            saveHiresFix(context)
            saveImg2Img(context)
            saveControlNet(context)
            saveAdetailer(context)
            saveXYZParam(context)
        }
        history.imagePaths.forEach { imageHistory ->
            database.imageHistoryDao().insert(
                ImageHistoryEntity.fromImageHistory(imageHistory, savedHistoryId)
            )
        }
    }

    fun getAllHistory(context: Context): List<SaveHistory> {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.historyDao().getAllHistory().map { it.toSaveHistory() }
    }

    fun getLatestHistory(context: Context): SaveHistory? {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.historyDao().getLatestHistory()?.toSaveHistory()
    }

    fun getFavoriteImageHistory(context: Context): List<ImageHistory> {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.imageHistoryDao().getFavouriteImageHistory().map { it.toImageHistory() }
    }

    fun getImageHistoryWithName(context: Context, name: String): ImageHistory? {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.imageHistoryDao().getImageHistoryWithName(name)?.toImageHistory()
    }

    fun getHistoryById(context: Context, id: Long): SaveHistory? {
        val database = AppDatabaseHelper.getDatabase(context)
        val raw = database.historyDao().getHistoryById(id)
        var result = raw?.toSaveHistory()
        // load control net
        result?.controlNetParam = ControlNetParam(
            slots = result?.controlNetParam?.slots?.map {
                val controlNet = database.controlNetHistoryDao()
                    .getControlNetHistoryWithControlNetByControlNetId(it.controlNetId)
                if (controlNet != null) {
                    return@map it.copy(
                        inputImagePath = controlNet.controlNetEntity.path,
                        inputImage = Util.readImageWithPathToBase64(
                            controlNet.controlNetEntity.path
                        )
                    )
                } else {
                    return@map it
                }

            } ?: emptyList()
        )

        // load lora data
        result?.loraPrompt = raw?.loraPrompts?.map { loraPromptEntity: LoraPromptEntity ->
            val loraWithRelation =
                database.loraPromptDao().getPromptWithRelate(loraPromptEntity.loraPromptId)
            val loraPromptList = raw.promptExtraEntity.filter {
                it.promptType == PromptType.LoraTrigger.value && it.loraPromptId == loraPromptEntity.loraPromptId
            }.mapNotNull {
                loraWithRelation?.triggerText?.find { triggerText ->
                    triggerText.promptId == it.promptId
                }
            }
            val promptObj = loraPromptEntity.toPrompt().copy(
                triggerText = loraWithRelation?.triggerText?.map { it.toPrompt() } ?: emptyList(),
                prompts = loraPromptList.map { it.toPrompt() },
            )
            promptObj
        } ?: emptyList()
        if (result != null) {
            val reactorSourcePath = result.reactorParam?.singleImageResult
            reactorSourcePath?.let {
                result = result!!.copy(
                    reactorParam = result!!.reactorParam?.copy(
                        singleImageResult = Util.readImageWithPathToBase64(
                            it
                        )
                    )
                )
            }

        }

        return result
    }

    fun findLatestControlNetUse(context: Context, controlNetId: Long): ControlNetHistoryEntity? {
        val database = AppDatabaseHelper.getDatabase(context)
        val result = database.controlNetHistoryDao()
            .getControlNetHistoryWithControlNetByControlNetId(controlNetId)
        return result?.controlNetHistoryEntity
    }

}
