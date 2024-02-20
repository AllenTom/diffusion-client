package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import com.allentom.diffusion.composables.TemplateItem
import com.allentom.diffusion.store.AppDatabaseHelper

@Entity(tableName = "template_item")
data class TemplateItemEntity(
    @PrimaryKey(autoGenerate = true)
    var templateItemId: Long = 0,
    var templateId: Long,
    var text: String,
    var promptId: Long? = null,
    var templateSlot: String? = null,
    var templateCategory: String? = null

)

@Dao
interface TemplateItemDao {
    @Insert
    fun insertTemplateItem(templateItem: TemplateItemEntity): Long
}

@Entity(tableName = "template")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    var templateId: Int = 0,
    var createAt: Long = System.currentTimeMillis()
)

@Dao
interface TemplateDao {
    @Insert
    fun insertTemplate(template: TemplateEntity): Long

    @Query("SELECT * FROM template order by createAt desc")
    fun getAllTemplates(): List<TemplateWithItems>
}

data class TemplateWithItems(
    @PrimaryKey(autoGenerate = true)
    val templateId: Long = 0,
    @Relation(
        parentColumn = "templateId",
        entityColumn = "templateId"
    )
    val items: List<TemplateItemEntity>
)

suspend fun HistoryStore.saveTemplate(context: Context, template: List<TemplateItem>) {
    val database = AppDatabaseHelper.getDatabase(context)
    val savedTemplateId = database.templateDao().insertTemplate(TemplateEntity())
    template.forEach {
        database.templateItemDao().insertTemplateItem(
            TemplateItemEntity(
                templateId = savedTemplateId,
                text = it.displayText,
                promptId = it.prompt?.promptId,
                templateSlot = it.slot,
                templateCategory = it.category
            )
        )
    }
}

suspend fun HistoryStore.getAllTemplates(context: Context): List<TemplateWithItems> {
    val database = AppDatabaseHelper.getDatabase(context)
    return database.templateDao().getAllTemplates()
}