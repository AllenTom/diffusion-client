package com.allentom.diffusion.store.prompt

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
import java.io.Serializable

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
            civitaiId = civitaiId,
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
    val civitaiId: Long? = null,
) : Serializable {
    fun getPromptText(): List<String> {
        return listOf("<lora:${name}:${weight}>") + prompts.map { it.getPromptText() }
    }

    fun isTriggered(prompt: Prompt): Boolean {
        return prompts.any { it.text == prompt.text }
    }
}