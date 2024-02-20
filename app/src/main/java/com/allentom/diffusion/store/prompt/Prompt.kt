package com.allentom.diffusion.store.prompt

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.civitai.entities.CivitaiModelVersion
import com.allentom.diffusion.api.civitai.getCivitaiApiClient
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.TemplateItem
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.history.HistoryWithRelation
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlMap
import com.charleskorn.kaml.yamlScalar
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

data class Prompt(
    var text: String,
    var piority: Int,
    var promptId: Long? = null,
    var translation: String? = null,
    var regionIndex: Int = 0,
    var category: String = "default",
    var templateSlot: String? = null,
    var randomId: String = Util.randomString(8),
    var slotOrder: Int = 0,
    var categoryOrder: Int = 0,
    var promptOrder: Int = 0,
    var generateLock: Boolean = false,
    var generateItem :TemplateItem? = null
) : Serializable {
    fun getPromptText(): String {
        if (piority == 0) {
            return text
        }
        var curText = text
        for (i in 0 until piority) {
            curText = "($curText)"
        }
        return curText
    }

    fun getTranslationText(): String {
        if (translation == null) {
            return text
        }
        return translation!!
    }
}


@Entity(tableName = "prompt")
data class SavePrompt(
    @PrimaryKey(autoGenerate = true)
    val promptId: Long = 0,
    var text: String,
    var nameCn: String,
    var time: Long,
    var count: Int,
    var category: String = "default",
    var templateSlot: String? = null,
    @ColumnInfo(defaultValue = "0")
    var categoryOrder: Int = 0,
    @ColumnInfo(defaultValue = "0")
    var slotOrder: Int = 0,
    @ColumnInfo(defaultValue = "0")
    var promptOrder : Int = 0
) : Serializable {
    companion object {
        fun fromPrompt(prompt: Prompt): SavePrompt {
            return SavePrompt(
                text = prompt.text,
                nameCn = prompt.text,
                time = System.currentTimeMillis(),
                count = 0,
                category = "User"
            )
        }
    }

    fun toPrompt(): Prompt {
        return Prompt(
            text = text,
            piority = 0,
            promptId = promptId,
            translation = nameCn,
            category = category,
            templateSlot = templateSlot,
            categoryOrder = categoryOrder,
            slotOrder = slotOrder,
            promptOrder = promptOrder
        )
    }

    fun getTranslationText(): String {
        if (nameCn.isBlank() || nameCn == text) {
            return text
        }
        return nameCn
    }
}

@Dao
interface PromptDao {
    @Insert
    fun insert(prompt: SavePrompt): Long

    @Update
    fun update(prompt: SavePrompt)

    @Query("SELECT * FROM prompt")
    fun getAllNotes(): Flow<List<SavePrompt>>

    @Query("SELECT * FROM prompt WHERE text = :text limit 1")
    fun getLibraryPrompt(text: String): SavePrompt?

    @Query("SELECT * FROM prompt WHERE text = :text limit 1")
    fun getPrompt(text: String): SavePrompt?

    @Query("SELECT * FROM prompt WHERE text  not in (:exclude) limit :n")
    fun getTopNPrompt(
        n: Int,
        exclude: List<String> = emptyList()
    ): List<SavePrompt>

    @Query("SELECT * FROM prompt WHERE text like '%' || :text || '%' or nameCn like '%' || :text || '%' order by count desc limit 40")
    fun searchPrompt(
        text: String,
    ): List<SavePrompt>

    @Query("SELECT * FROM prompt WHERE promptId = :id limit 1")
    fun getPromptById(id: Long): SavePrompt?

    @Query("SELECT category FROM prompt group by category ")
    fun getAllCategory(): List<String>

    @Query("SELECT * FROM prompt WHERE category = :category")
    fun getPromptByCategory(category: String): List<SavePrompt>

    @Query("SELECT distinct templateSlot FROM prompt")
    fun getTemplateSlotDistinct(): List<String?>

    @Query("SELECT * FROM prompt WHERE templateSlot = :slot")
    fun getPromptByTemplateSlot(slot: String): List<SavePrompt>

    @Query("SELECT * FROM prompt WHERE templateSlot = :slot and category = :category")
    fun getPromptByTemplateSlotAndCategory(slot: String, category: String): List<SavePrompt>


}

object PromptStore {
    fun refresh(context: Context) {

    }

    fun updatePrompt(context: Context, promptList: List<String>) {
        promptList.forEach { promptText ->
            AppDatabaseHelper.getDatabase(context).promptDao().getPrompt(promptText)?.let {
                it.count += 1
                it.time = System.currentTimeMillis()
                AppDatabaseHelper.getDatabase(context).promptDao().update(it)
            } ?: run {
                AppDatabaseHelper.getDatabase(context).promptDao().insert(
                    SavePrompt(
                        text = promptText,
                        nameCn = promptText,
                        time = System.currentTimeMillis(),
                        count = 1,
                        category = "User"
                    )
                )
            }
        }
    }

    fun getTopNPrompt(
        context: Context,
        n: Int,
        exclude: List<String> = emptyList()
    ): List<SavePrompt> {
        return AppDatabaseHelper.getDatabase(context).promptDao().getTopNPrompt(n, exclude)
    }

    fun searchPrompt(
        context: Context,
        text: String,
        exclude: List<String> = emptyList()
    ): List<SavePrompt> {
        val result = AppDatabaseHelper.getDatabase(context).promptDao().searchPrompt(text)
        return result
    }

    private fun getYamlFiles(context: Context, path: String): List<String> {
        val yamlFiles = ArrayList<String>()
        val list = context.assets.list(path) ?: return yamlFiles

        for (item in list) {
            val isDirectory = context.assets.list("$path/$item")?.isNotEmpty() ?: false

            if (isDirectory) {
                yamlFiles.addAll(getYamlFiles(context, "$path/$item"))
            } else if (item.endsWith(".yaml")) {
                yamlFiles.add("$path/$item")
            }
        }

        return yamlFiles
    }

    fun getPromptById(context: Context, id: Long): SavePrompt? {
        return AppDatabaseHelper.getDatabase(context).promptDao().getPromptById(id)
    }

    fun getPromptHistory(context: Context, prompt: SavePrompt): List<HistoryWithRelation> {
        val db = AppDatabaseHelper.getDatabase(context)
        val images = db.promptHistoryDao().getPromptHistory(prompt.promptId).let { historyList ->
            db.historyDao().getHistoryByIds(historyList.map { it.historyId })
        }
        return images
    }

    fun getAllCategory(context: Context): List<String> {
        return AppDatabaseHelper.getDatabase(context).promptDao().getAllCategory()
    }

    fun getAllTemplateSlot(context: Context): List<String> {
        return AppDatabaseHelper.getDatabase(context).promptDao().getTemplateSlotDistinct().filterNotNull()
    }

    fun getPromptByTemplateSlot(context: Context, slot: String): List<SavePrompt> {
        return AppDatabaseHelper.getDatabase(context).promptDao().getPromptByTemplateSlot(slot)
    }

    fun getPromptByTemplateSlotAndCategory(context: Context, slot: String, category: String): List<SavePrompt> {
        return AppDatabaseHelper.getDatabase(context).promptDao().getPromptByTemplateSlotAndCategory(slot, category)
    }

    fun getPromptByCategory(context: Context, category: String): List<SavePrompt> {
        return AppDatabaseHelper.getDatabase(context).promptDao().getPromptByCategory(category)
    }

    fun getOrCreateLoraPromptByName(context: Context, name: String): LoraPromptEntity {
        val db = AppDatabaseHelper.getDatabase(context)
        val prompt = db.loraPromptDao().getPrompt(name)
        return if (prompt == null) {
            val promptEntity = LoraPromptEntity(
                name = name,
                weight = 1f,
                previewPath = null,
                hash = null,
            )
            val id = db.loraPromptDao().insert(promptEntity)
            db.loraPromptDao().getPrompt(id)!!
        } else {
            prompt
        }
    }

    fun getOrCreatePromptByName(context: Context, name: String): SavePrompt {
        val db = AppDatabaseHelper.getDatabase(context)
        val prompt = db.promptDao().getPrompt(name)
        return if (prompt == null) {
            val promptEntity = SavePrompt(
                text = name,
                nameCn = name,
                time = System.currentTimeMillis(),
                count = 0,
                category = "User",
            )
            val id = db.promptDao().insert(promptEntity)
            db.promptDao().getPromptById(id)!!
        } else {
            prompt
        }
    }

    fun newPromptByName(context: Context, newPrompt: SavePrompt) {
        val db = AppDatabaseHelper.getDatabase(context)
        val prompt = db.promptDao().getPrompt(newPrompt.text)
        if (prompt == null) {
            val promptEntity = newPrompt
            db.promptDao().insert(promptEntity)
        } else {
            val promptToSave = prompt.copy(
                nameCn = newPrompt.nameCn,
                category = newPrompt.category,
                text = newPrompt.text,
                templateSlot = newPrompt.templateSlot
            )
            db.promptDao().update(promptToSave)
        }
    }

    private fun loadLibrary(context: Context) {
        val files = getYamlFiles(context, "prompt")
        for (item in files) {
            val input =
                context.assets.open(item).bufferedReader().use { it.readText() }

            val raw = Yaml.default.parseToYamlNode(input)
            val name = raw.yamlMap.get<YamlNode>("name")?.yamlScalar?.content
            if (name == null) {
                continue
            }
            val contentMap = raw.yamlMap.get<YamlMap>("content") ?: continue
            contentMap.entries.forEach { ent ->
                val promptName = ent.key.yamlScalar.content
                val nameCn = ent.value.yamlMap.get<YamlScalar>("name")?.content
                AppDatabaseHelper.getDatabase(context).promptDao()
                    .getLibraryPrompt(promptName).let {
                        if (it == null) {
                            AppDatabaseHelper.getDatabase(context).promptDao().insert(
                                SavePrompt(
                                    text = promptName,
                                    nameCn = nameCn ?: promptName,
                                    time = System.currentTimeMillis(),
                                    count = 0,
                                    category = name,
                                )
                            )
                        }
                    }
            }
        }
    }

    fun getAllLoraPrompt(context: Context): List<LoraPromptEntity> {
        return AppDatabaseHelper.getDatabase(context).loraPromptDao().getAll()
    }

    fun getLoraPromptWithRelate(context: Context, id: Long): LoraPromptWithRelation? {
        return AppDatabaseHelper.getDatabase(context).loraPromptDao().getPromptWithRelate(id)
    }

    suspend fun matchLoraByModelId(context: Context, modelId: Long) {
        val db = AppDatabaseHelper.getDatabase(context)
        val loraPrompt = db.loraPromptDao().getPrompt(modelId) ?: return
        val hashResult = getApiClient().getHash("lora", loraPrompt.name)
        hashResult.body()?.let { modelHash ->
            linkCivitaiModelByHash(context, modelId, modelHash.hash)
        }
    }


    suspend fun linkCivitaiModelByHash(context: Context, modelId: Long, modelHash: String) {
        val result = getCivitaiApiClient().getModelVersionByHash(modelHash)
        result.body()?.let { model ->
            linkCivitaiModel(context, model, modelId)
        }
    }


    suspend fun linkCivitaiModelById(
        context: Context,
        modelId: Long,
        civitaiModelVersionId: String
    ) {
        val result = getCivitaiApiClient().getModelVersionById(civitaiModelVersionId)
        result.body()?.let { model ->
            linkCivitaiModel(context, model, modelId)
        }
    }

    suspend fun linkCivitaiModel(
        context: Context,
        civitaiModelVersion: CivitaiModelVersion,
        modelId: Long
    ) {
        val db = AppDatabaseHelper.getDatabase(context)
        var loraPrompt = db.loraPromptDao().getPrompt(modelId) ?: return
        loraPrompt = loraPrompt.copy(
            title = civitaiModelVersion.model.name,
            civitaiId = civitaiModelVersion.id
        )
        // save trigger prompts
        civitaiModelVersion.trainedWords.let {
            it.forEach { trigger ->
                val prompts = trigger.split(",").map { it.trim() }
                prompts.forEach {
                    val prompt = getOrCreatePromptByName(context, it)
                    val existRel = db.loraTriggerDao().selectByPromptIdAndLoraPromptId(
                        prompt.promptId,
                        loraPrompt.loraPromptId
                    )
                    if (existRel == null) {
                        db.loraTriggerDao().insert(
                            LoraTriggerCrossRef(
                                prompt.promptId,
                                loraPrompt.loraPromptId
                            )
                        )
                    }
                }

            }
        }
        civitaiModelVersion.images.firstOrNull()?.let { previewImage ->
            Util.saveLoraImagesFromUrls(context, listOf(previewImage.url)).firstOrNull()?.let {
                loraPrompt = loraPrompt.copy(previewPath = it, lockPreview = true)
            }
        }
        db.loraPromptDao().update(loraPrompt)
    }
}

