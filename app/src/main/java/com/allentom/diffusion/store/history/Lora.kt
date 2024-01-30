package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Update
import com.allentom.diffusion.Util
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.LoraPrompt
import com.allentom.diffusion.store.Prompt
import com.allentom.diffusion.store.PromptStore

@Entity(primaryKeys = ["loraPromptId", "historyId"], tableName = "lora_prompt_history")
data class LoraPromptHistoryCrossRef(
    val loraPromptId: Long,
    val historyId: Long
)

@Dao
interface LoraPromptHistoryDao {
    @Insert
    fun insert(loraPromptHistoryCrossRef: LoraPromptHistoryCrossRef)

    @Update
    fun update(loraPromptHistoryCrossRef: LoraPromptHistoryCrossRef)
}

fun SaveHistory.saveLora(context: Context) {
    val database = AppDatabaseHelper.getDatabase(context)
    loraPrompt.forEach { prompt ->
        val promptEntity = PromptStore.getOrCreateLoraPromptByName(context, prompt.name)
        val promptId = promptEntity.loraPromptId

        database.loraPromptHistoryDao().insert(
            LoraPromptHistoryCrossRef(
                loraPromptId = promptId,
                historyId = id
            )
        )
        database.promptExtraDao().insert(
            PromptExtraEntity(
                promptId = promptId,
                priority = 0,
                weight = prompt.weight,
                historyId = id,
                promptType = PromptType.LoraPrompt.value,
            )
        )

        prompt.prompts.forEach { loraPrompt ->
            val textPrompt = PromptStore.getOrCreatePromptByName(context, loraPrompt.text)
            val textPromptId = textPrompt.promptId
            database.promptExtraDao().insert(
                PromptExtraEntity(
                    promptId = textPromptId,
                    priority = loraPrompt.piority,
                    historyId = id,
                    promptType = PromptType.LoraTrigger.value,
                    loraPromptId = promptId
                )
            )

        }
        // save lora preview
        imagePaths.firstOrNull()?.let { imgHistory ->
            if (!promptEntity.lockPreview) {
                val previewPath = Util.saveLoraPreviewToAppData(
                    context,
                    imgHistory.path,
                    promptId
                )
                database.loraPromptDao().update(
                    promptEntity.copy(
                        previewPath = previewPath
                    )
                )
            }

        }
    }

}

fun HistoryWithRelation.toLoraPrompt(): List<LoraPrompt> {
    return loraPrompts.map {
        val obj = it.toPrompt()
        promptExtraEntity.find { promptExtra ->
            promptExtra.promptId == it.loraPromptId && promptExtra.promptType == PromptType.LoraPrompt.value
        }?.let { extra ->
            obj.weight = extra.weight
        }
        obj
    }
}