package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.store.prompt.SavePrompt

enum class PromptType(val value: Int) {
    Prompt(0),
    NegativePrompt(1),
    LoraPrompt(2),
    LoraTrigger(4),
    EmbeddingPrompt(3),
}

@Entity(primaryKeys = ["promptId", "historyId"], tableName = "prompt_history")
data class PromptHistoryCrossRef(
    val promptId: Long,
    val historyId: Long
)

@Entity
@Dao
interface PromptHistoryDao {
    @Insert
    fun insert(promptHistoryCrossRef: PromptHistoryCrossRef)

    @Update
    fun update(promptHistoryCrossRef: PromptHistoryCrossRef)

    @Query("SELECT * FROM prompt_history WHERE promptId = :promptId")
    fun getPromptHistory(promptId: Long): List<PromptHistoryCrossRef>
}

@Entity(primaryKeys = ["promptId", "historyId"], tableName = "negative_prompt_history")
data class NegativePromptHistoryCrossRef(
    val promptId: Long,
    val historyId: Long
)

@Dao
interface NegativePromptHistoryDao {
    @Insert
    fun insert(negativePromptHistoryCrossRef: NegativePromptHistoryCrossRef)

    @Update
    fun update(negativePromptHistoryCrossRef: NegativePromptHistoryCrossRef)

    @Query("SELECT * FROM negative_prompt_history WHERE promptId = :promptId and historyId = :historyId")
    fun getNegativePromptHistory(promptId: Long, historyId: Long): NegativePromptHistoryCrossRef?


}

@Entity(tableName = "prompt_extra")
data class PromptExtraEntity(
    @PrimaryKey(autoGenerate = true)
    val promptExtraId: Long = 0,
    val promptId: Long,
    val priority: Int,
    val weight: Float = 0f,
    val historyId: Long,
    val promptType: Int,
    val loraPromptId: Long = 0,
    val regionIndex: Int? = 0
)

@Dao
interface PromptExtraDao {
    @Insert
    fun insert(promptExtraEntity: PromptExtraEntity)

    @Update
    fun update(promptExtraEntity: PromptExtraEntity)

    @Query("SELECT * FROM prompt_extra WHERE historyId = :historyId")
    fun getPromptExtra(historyId: Long): List<PromptExtraEntity>
}

fun SaveHistory.saveHistoryPrompt(context: Context, promptType: PromptType) {
    if (promptType == PromptType.Prompt) {
        savePrompt(context, id, prompt, promptType)
    }
    if (promptType == PromptType.NegativePrompt) {
        savePrompt(context, id, negativePrompt, promptType)
    }
}

fun savePrompt(context: Context, historyId: Long, prompts: List<Prompt>, promptType: PromptType) {
    val database = AppDatabaseHelper.getDatabase(context)
    val savedPromptRelList = mutableListOf<SavePrompt>()
    prompts.forEach { prompt ->
        val promptEntity = database.promptDao().getPrompt(prompt.text)
        val promptId = if (promptEntity != null) {
            database.promptDao().update(
                promptEntity.copy(
                    nameCn = prompt.translation ?: promptEntity.nameCn,
                    count = promptEntity.count + 1
                )
            )
            promptEntity.promptId
        } else {
            database.promptDao().insert(SavePrompt.fromPrompt(prompt))
        }
        if (promptEntity == null) {
            return@forEach
        }
        if (savedPromptRelList.none { it.promptId == promptId }) {
            if (promptType == PromptType.NegativePrompt) {
                database.negativePromptHistoryDao().insert(
                    NegativePromptHistoryCrossRef(
                        promptId = promptId,
                        historyId = historyId
                    )
                )
            }
            if (promptType == PromptType.Prompt) {
                database.promptHistoryDao().insert(
                    PromptHistoryCrossRef(
                        promptId = promptId,
                        historyId = historyId
                    )
                )
            }
            savedPromptRelList += promptEntity
        }
        database.promptExtraDao().insert(
            PromptExtraEntity(
                promptId = promptId,
                priority = prompt.piority,
                historyId = historyId,
                promptType = promptType.value,
                regionIndex = prompt.regionIndex
            )
        )
    }
}

fun HistoryWithRelation.toPrompt(): List<Prompt> {
    return promptExtraEntity.mapNotNull { promptExtraEntity ->
        val prompt = prompts.find { it.promptId == promptExtraEntity.promptId }
        prompt?.let {
            val obj = it.toPrompt()
            obj.piority = promptExtraEntity.priority
            obj.regionIndex = promptExtraEntity.regionIndex ?: 0
            obj
        }
    }
}

fun HistoryWithRelation.toNegativePrompt(): List<Prompt> {
    return negativePrompts.map {
        val obj = it.toPrompt()
        promptExtraEntity.find { promptExtra ->
            promptExtra.promptId == it.promptId && promptExtra.promptType == PromptType.NegativePrompt.value
        }?.let { extra ->
            obj.piority = extra.priority
        }
        obj
    }
}
