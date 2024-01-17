package com.allentom.diffusion.store

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.allentom.diffusion.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

data class ControlNetMetaItem(
    val filename: String,
    val preview: String? = null,
)

data class ControlNetMeta(
    val name: String,
    val list: List<ControlNetMetaItem>
)

data class SaveControlNet(
    val id: Long,
    val time: Long,
    val path: String,
    val previewPath: String = "",
    val md5: String,
)

@Entity(tableName = "control_net")
data class ControlNetEntity(
    @PrimaryKey(autoGenerate = true)
    val controlNetId: Long = 0,
    val path: String,
    val md5: String,
    val previewPath: String = "",
    val time: Long,
) {
    companion object {
        fun fromSaveControlNet(prompt: SaveControlNet): ControlNetEntity {
            return ControlNetEntity(
                path = prompt.path,
                md5 = prompt.md5,
                time = prompt.time
            )
        }
    }

    fun toControlNetEntity(): SaveControlNet {
        return SaveControlNet(
            id = controlNetId,
            time = time,
            path = path,
            md5 = md5,
            previewPath = previewPath
        )
    }
}

@Dao
interface ControlNetDao {
    @Insert
    fun insert(controlNet: ControlNetEntity): Long

    @Delete
    fun delete(controlNet: ControlNetEntity)

    @Query("SELECT * FROM control_net ORDER BY time DESC")
    fun getAll(): List<ControlNetEntity>

    @Query("SELECT * FROM control_net WHERE md5 = :md5")
    fun getByMd5(md5: String): ControlNetEntity?

    @Query("SELECT * FROM control_net WHERE controlNetId = :id")
    fun getById(id: Long): ControlNetEntity?

    @Update
    fun update(controlNet: ControlNetEntity)
}

object ControlNetStore {
    var items: List<SaveControlNet> = emptyList()
    private val STORE_KEY = "SAVE_CONTROL_NET"

    fun refresh(context: Context) {
        val sharedPreferences = context.getSharedPreferences(this.STORE_KEY, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("items", null)

        if (json != null) {
            val type = object : TypeToken<List<SaveControlNet>>() {}.type
            items = Gson().fromJson(json, type)
        } else {
            items = emptyList()
        }
    }

    fun getAll(context: Context): List<SaveControlNet> {
        val database = AppDatabaseHelper.getDatabase(context)
        val controlNetList = database.controlNetDao().getAll()
        return controlNetList.map { it.toControlNetEntity() }
    }

    fun addControlNet(context: Context, uri: Uri,previewUri:Uri? = null) {
        val db = AppDatabaseHelper.getDatabase(context)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.let {
            val imageBytes = inputStream.readBytes()
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(imageBytes)
            val imageMd5 = digest.joinToString("") { "%02x".format(it) }
            val existControlNet = db.controlNetDao().getByMd5(imageMd5)
            if (existControlNet != null) {
                return
            }
            val savePath = Util.saveControlNetToAppData(context, uri)
            var previewPath = ""
            if (previewUri != null) {
                previewPath = Util.saveControlNetPreviewToAppData(context,previewUri!!,imageMd5)
            }
            val controlNetId = db.controlNetDao().insert(
                ControlNetEntity(
                    path = savePath,
                    md5 = imageMd5,
                    time = System.currentTimeMillis(),
                    previewPath = previewPath
                )
            )
        }
    }

    fun removeControlNet(context: Context, controlNet: SaveControlNet) {
        val db = AppDatabaseHelper.getDatabase(context)

        val controlNetEntity = db.controlNetDao().getByMd5(controlNet.md5)

        controlNetEntity?.let {
            // Delete the file associated with the ControlNetEntity
            val file = File(it.path)
            if (file.exists()) {
                file.delete()
            }

            // Delete the ControlNetEntity from the database
            db.controlNetDao().delete(it)
        }
    }

}