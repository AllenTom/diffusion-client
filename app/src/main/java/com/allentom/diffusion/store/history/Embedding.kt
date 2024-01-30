package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Update
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.EmbeddingEntity
import com.allentom.diffusion.store.EmbeddingPrompt
import com.allentom.diffusion.store.LoraPrompt

@Entity(primaryKeys = ["embeddingId", "historyId"], tableName = "embedding_history")
data class EmbeddingHistoryCrossRef(
    val embeddingId: Long,
    val historyId: Long
)

@Dao
interface EmbeddingHistoryDao {
    @Insert
    fun insert(embeddingHistoryCrossRef: EmbeddingHistoryCrossRef)

    @Update
    fun update(embeddingHistoryCrossRef: EmbeddingHistoryCrossRef)
}

fun SaveHistory.saveEmbedding(context:Context) {
    val database = AppDatabaseHelper.getDatabase(context)
    embeddingPrompt.forEach { prompt ->
        val promptEntity = database.embeddingDao().getPrompt(prompt.text)
        val promptId = promptEntity?.embeddingId ?: database.embeddingDao().insert(
            EmbeddingEntity.fromPrompt(prompt)
        )
        database.embeddingHistoryDao().insert(
            EmbeddingHistoryCrossRef(
                embeddingId = promptId,
                historyId = id
            )
        )
        database.promptExtraDao().insert(
            PromptExtraEntity(
                promptId = promptId,
                priority = prompt.piority,
                historyId = id,
                promptType = PromptType.LoraPrompt.value,
            )
        )
    }
}

fun HistoryWithRelation.toEmbeddingPrompt(): List<EmbeddingPrompt> {
    return embeddingPrompts.map {
        val obj = it.toPrompt()
        promptExtraEntity.find { promptExtra ->
            promptExtra.promptId == it.embeddingId && promptExtra.promptType == PromptType.EmbeddingPrompt.value
        }?.let { extra ->
            obj.piority = extra.priority
        }
        obj
    }
}