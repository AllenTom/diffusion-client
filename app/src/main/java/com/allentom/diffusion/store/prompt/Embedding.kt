package com.allentom.diffusion.store.prompt

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import java.io.Serializable

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