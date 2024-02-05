package com.allentom.diffusion.store.prompt

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Update
import com.allentom.diffusion.store.AppDatabaseHelper
data class PromptStyle(
    val styleId: Long = 0,
    val name: String,
    val prompts: List<Prompt>
)

@Entity(tableName = "style_prompt")
data class StyleEntity(
    @PrimaryKey(autoGenerate = true)
    val styleId: Long = 0,
    val name: String
)

@Dao
interface StyleDao {
    @Query("SELECT * FROM style_prompt order by styleId desc")
    fun getAll(): List<StyleWithPrompt>

    @Query("SELECT * FROM style_prompt WHERE name LIKE '%' || :name || '%'")
    fun searchStyleByName(name: String): List<StyleWithPrompt>

    @Insert
    fun insert(style: StyleEntity): Long

    @Query("SELECT * FROM style_prompt WHERE styleId = :id")
    fun getById(id: Long): StyleWithPrompt?

    @Delete
    fun delete(style: StyleEntity)

    @Update
    fun update(style: StyleEntity)
}

@Entity(primaryKeys = ["styleId", "promptId"], tableName = "style_prompt_ref")
data class StylePromptCrossRef(
    val styleId: Long,
    val promptId: Long
)

@Dao
interface StylePromptDao {
    @Insert
    fun insert(crossRef: StylePromptCrossRef): Long

    @Delete
    fun delete(crossRef: StylePromptCrossRef)
}

data class StyleWithPrompt(
    @Embedded
    val style: StyleEntity,
    @Relation(
        parentColumn = "styleId",
        entityColumn = "promptId",
        associateBy = Junction(StylePromptCrossRef::class)
    )
    val prompts: List<SavePrompt>
) {
    fun toStylePrompt(): PromptStyle {
        return PromptStyle(
            styleId = style.styleId,
            name = style.name,
            prompts = prompts.map { it.toPrompt() }
        )
    }
}

object StyleStore {
    fun getAllStyles(context: Context): List<PromptStyle> {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.styleDao().getAll().map { it.toStylePrompt() }
    }

    fun newStyle(context: Context, name: String, prompts: List<Prompt>) {
        val database = AppDatabaseHelper.getDatabase(context)
        val style = StyleEntity(name = name)
        prompts.forEach { prompt ->
            if (prompt.promptId == null || prompt.promptId == 0L) {
                val savedPrompt = PromptStore.getOrCreatePromptByName(context, prompt.text)
                prompt.promptId = savedPrompt.promptId
            }
        }
        val styleId = database.styleDao().insert(style)
        prompts.forEach { prompt ->
            val crossRef = StylePromptCrossRef(styleId, prompt.promptId!!)
            database.stylePromptDao().insert(crossRef)
        }
    }

    fun searchStyleByName(context: Context, name: String): List<PromptStyle> {
        val database = AppDatabaseHelper.getDatabase(context)
        return database.styleDao().searchStyleByName(name).map { it.toStylePrompt() }
    }

    fun deleteStyleById(context: Context, styleId: Long) {
        val database = AppDatabaseHelper.getDatabase(context)
        val style = database.styleDao().getById(styleId) ?: return
        style.prompts.forEach { prompt ->
            val promptId = prompt.promptId ?: return@forEach
            val crossRef = StylePromptCrossRef(styleId, promptId)
            database.stylePromptDao().delete(crossRef)
        }
        database.styleDao().delete(style.style)
    }

    fun updateStyleById(context: Context, styleId: Long, name: String, prompts: List<Prompt>) {
        val database = AppDatabaseHelper.getDatabase(context)
        var style = database.styleDao().getById(styleId) ?: return
        style.prompts.forEach { prompt ->
            val promptId = prompt.promptId ?: return@forEach
            val crossRef = StylePromptCrossRef(styleId, promptId)
            database.stylePromptDao().delete(crossRef)
        }
        prompts.forEach { prompt ->
            if (prompt.promptId == null || prompt.promptId == 0L) {
                val savedPrompt = PromptStore.getOrCreatePromptByName(context, prompt.text)
                prompt.promptId = savedPrompt.promptId
            }
        }
        style = style.copy(style = style.style.copy(name = name))
        database.styleDao().update(style.style)
        prompts.forEach { prompt ->
            val crossRef = StylePromptCrossRef(styleId, prompt.promptId!!)
            database.stylePromptDao().insert(crossRef)
        }
    }

    fun removePromptFromStyle(context: Context, styleId: Long, promptId: Long) {
        val database = AppDatabaseHelper.getDatabase(context)
        val crossRef = StylePromptCrossRef(styleId, promptId)
        database.stylePromptDao().delete(crossRef)
    }

    fun addPromptToStyle(context: Context, styleId: Long, promptId: Long) {
        val database = AppDatabaseHelper.getDatabase(context)
        val crossRef = StylePromptCrossRef(styleId, promptId)
        database.stylePromptDao().insert(crossRef)
    }
}