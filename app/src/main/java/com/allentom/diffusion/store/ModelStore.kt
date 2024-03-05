package com.allentom.diffusion.store

import android.content.Context
import android.util.Log
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
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@Entity(tableName = "model")
data class ModelEntity(
    @PrimaryKey(autoGenerate = true)
    val modelId: Long = 0,
    val title: String? = null,
    val name: String,
    val coverPath: String? = null,
    val civitaiApiId: Long? = null,
    val time: Long = System.currentTimeMillis(),
)

@Dao
interface ModelDao {
    @Insert
    fun insert(model: ModelEntity): Long

    @Query("SELECT * FROM model")
    fun getAll(): List<ModelEntity>

    @Query("SELECT * FROM model WHERE name = :name")
    fun getByName(name: String): ModelEntity?

    @Query("SELECT * FROM model WHERE modelId = :id")
    fun getByID(id: Long): ModelEntity?

    @Update
    fun update(model: ModelEntity)

    @Insert
    fun insertMany(models: List<ModelEntity>)
}

object ModelStore {
    fun insert(context: Context, model: ModelEntity): Long {
        val id = AppDatabaseHelper.getDatabase(context).modelDao().insert(model)
        return id
    }

    fun getAll(context: Context): List<ModelEntity> {
        return AppDatabaseHelper.getDatabase(context).modelDao().getAll()
    }

    fun getByName(context: Context, name: String): ModelEntity? {
        return AppDatabaseHelper.getDatabase(context).modelDao().getByName(name)
    }

    fun getByID(context: Context, id: Long): ModelEntity? {
        return AppDatabaseHelper.getDatabase(context).modelDao().getByID(id)
    }

    fun insertAndGet(context: Context, model: ModelEntity): ModelEntity {
        val db = AppDatabaseHelper.getDatabase(context)
        val id = db.modelDao().insert(model)
        return db.modelDao().getByID(id)!!
    }

    fun getOrCreate(context: Context, name: String): ModelEntity {
        val db = AppDatabaseHelper.getDatabase(context)
        val existModel = db.modelDao().getByName(name)
        return existModel ?: insertAndGet(context, ModelEntity(name = name))
    }

    fun update(context: Context, model: ModelEntity) {
        AppDatabaseHelper.getDatabase(context).modelDao().update(model)
    }

    fun insertNameIfNotExist(context: Context, modelName: String): Long {
        val db = AppDatabaseHelper.getDatabase(context)
        val existModel = db.modelDao().getByName(modelName)
        return existModel?.modelId ?: db.modelDao().insert(ModelEntity(name = modelName))
    }

    fun insertNameIfNotExistMany(context: Context, models: List<String>) {
        models.forEach {
            insertNameIfNotExist(context, it)
        }
    }

    suspend fun matchModelByModelId(context: Context, modelId: Long) {
        val db = AppDatabaseHelper.getDatabase(context)
        val model = db.modelDao().getByID(modelId) ?: return
        val sdwModel = DrawViewModel.models.find { it.modelName == model.name }
        val sdwModelHash =
            sdwModel?.sha256 ?: getApiClient().getHash("ckp", model.name).body()?.hash
        sdwModelHash?.let {
            linkCivitaiModelByHash(context, modelId, it)
            return
        }
    }

    suspend fun linkCivitaiModelByHash(context: Context, modelId: Long, modelHash: String) {
        val result = getCivitaiApiClient().getModelVersionByHash(modelHash)
        result.body()?.let { model ->
            linkCivitaiModel(context, model, modelId)
        }
    }

    fun linkCivitaiModel(context: Context, civitaiModelVersion: CivitaiModelVersion, modelId: Long) {
        val db = AppDatabaseHelper.getDatabase(context)
        var modelEntity = db.modelDao().getByID(modelId) ?: return
        modelEntity = modelEntity.copy(
            title = civitaiModelVersion.model.name,
            civitaiApiId = civitaiModelVersion.id
        )
        civitaiModelVersion.images.firstOrNull()?.let { previewImage ->
            Util.saveLoraImagesFromUrls(context, listOf(previewImage.url)).firstOrNull()?.let {
                modelEntity = modelEntity.copy(coverPath = it)
            }
        }
        db.modelDao().update(modelEntity)
    }
}