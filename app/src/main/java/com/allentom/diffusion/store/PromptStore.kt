package com.allentom.diffusion.store

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Update
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.civitai.entities.CivitaiModelVersion
import com.allentom.diffusion.api.civitai.getCivitaiApiClient
import com.allentom.diffusion.api.getApiClient
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlMap
import com.charleskorn.kaml.yamlScalar
import kotlinx.coroutines.flow.Flow
import java.io.Serializable
import kotlin.random.Random

class EmbeddingPrompt(
    var text: String,
    var piority: Int,
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
}

@Entity(tableName = "embedding")
data class EmbeddingEntity(
    @PrimaryKey(autoGenerate = true)
    val embeddingId: Long = 0,
    val name: String,
    val priority: Int,
) {
    companion object {
        fun fromPrompt(prompt: EmbeddingPrompt): EmbeddingEntity {
            return EmbeddingEntity(
                name = prompt.text,
                priority = prompt.piority
            )
        }
    }

    fun toPrompt(): EmbeddingPrompt {
        return EmbeddingPrompt(
            text = name,
            piority = priority
        )
    }
}

@Dao
interface EmbeddingDao {
    @Insert
    fun insert(prompt: EmbeddingEntity): Long

    @Query("SELECT * FROM embedding WHERE name = :name limit 1")
    fun getPrompt(name: String): EmbeddingEntity?


}

data class Prompt(
    var text: String,
    var piority: Int,
    var promptId: Long? = null,
    var translation: String? = null,
    var regionIndex: Int = 0,
    var randomId: String = Util.randomString(8),
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
}

@Entity(primaryKeys = ["promptId", "loraPromptId"], tableName = "lora_trigger")
data class LoraTriggerCrossRef(
    val promptId: Long,
    val loraPromptId: Long
)

@Dao
interface LoraTriggerDao {
    @Insert
    fun insert(crossRef: LoraTriggerCrossRef): Long

    @Query("SELECT * FROM lora_trigger WHERE promptId = :promptId")
    fun getTriggerByPromptId(promptId: Long): List<LoraTriggerCrossRef>

    @Query("SELECT * FROM lora_trigger WHERE loraPromptId = :loraPromptId")
    fun getTriggerByLoraPromptId(loraPromptId: Long): List<LoraTriggerCrossRef>

    @Query("DELETE FROM lora_trigger WHERE promptId = :promptId")
    fun deleteByPromptId(promptId: Long)

    @Query("DELETE FROM lora_trigger WHERE loraPromptId = :loraPromptId")
    fun deleteByLoraPromptId(loraPromptId: Long)

    @Query("SELECT * FROM lora_trigger WHERE promptId = :promptId and loraPromptId = :loraPromptId limit 1")
    fun selectByPromptIdAndLoraPromptId(promptId: Long, loraPromptId: Long): LoraTriggerCrossRef?

}

class LoraPromptWithRelation(
    @Embedded
    val loraPrompt: LoraPromptEntity,
    @Relation(
        parentColumn = "loraPromptId",
        entityColumn = "promptId",
        associateBy = Junction(LoraTriggerCrossRef::class)
    )
    val triggerText: List<SavePrompt>
) : Serializable

@Entity(tableName = "lora")
data class LoraPromptEntity(
    @ColumnInfo(defaultValue = "")
    val title: String = "",
    @PrimaryKey(autoGenerate = true)
    val loraPromptId: Long = 0,
    val name: String,
    val weight: Float,
    val previewPath: String? = null,
    val hash: String? = null,
    val civitaiId: Long? = null,
    val lockPreview: Boolean = false,
) {
    companion object {
        fun fromPrompt(prompt: LoraPrompt): LoraPromptEntity {
            return LoraPromptEntity(
                name = prompt.name,
                weight = prompt.weight,
                previewPath = prompt.previewPath,
                hash = prompt.hash,
            )
        }
    }

    fun toPrompt(): LoraPrompt {
        return LoraPrompt(
            id = loraPromptId,
            name = name,
            weight = weight,
            previewPath = previewPath,
            hash = hash,
            title = title,
        )
    }
}

@Dao
interface LoraPromptDao {
    @Insert
    fun insert(prompt: LoraPromptEntity): Long

    @Query("SELECT * FROM lora WHERE name = :name limit 1")
    fun getPrompt(name: String): LoraPromptEntity?

    @Query("SELECT * FROM lora WHERE loraPromptId = :id limit 1")
    fun getPrompt(id: Long): LoraPromptEntity?

    @Update
    fun update(prompt: LoraPromptEntity)

    @Query("SELECT * FROM lora")
    fun getAll(): List<LoraPromptEntity>

    @Query("SELECT * FROM lora WHERE loraPromptId = :id limit 1")
    fun getPromptWithRelate(id: Long): LoraPromptWithRelation?
}

data class LoraPrompt(
    val id: Long = 0,
    val name: String,
    var weight: Float,
    val previewPath: String? = null,
    val hash: String? = null,
    val title: String = "",
    val prompts: List<Prompt> = emptyList(),
    val triggerText: List<Prompt> = emptyList(),
) : Serializable {
    fun getPromptText(): List<String> {
        return listOf("<lora:${name}:${weight}>") + prompts.map { it.getPromptText() }
    }

    fun isTriggered(prompt: Prompt): Boolean {
        return prompts.any { it.text == prompt.text }
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
        )
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

    @Query("SELECT * FROM prompt WHERE text like '%' || :text || '%' or nameCn like '%' || :text || '%' order by count desc limit 20")
    fun searchPrompt(
        text: String,
    ): List<SavePrompt>

    @Query("SELECT * FROM prompt WHERE promptId = :id limit 1")
    fun getPromptById(id: Long): SavePrompt?

    @Query("SELECT category FROM prompt group by category ")
    fun getAllCategory(): List<String>

    @Query("SELECT * FROM prompt WHERE category = :category")
    fun getPromptByCategory(category: String): List<SavePrompt>
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
    fun newPromptByName(context: Context,newPrompt:SavePrompt) {
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

