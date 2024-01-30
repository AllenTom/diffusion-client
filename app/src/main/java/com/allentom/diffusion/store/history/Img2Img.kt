package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.allentom.diffusion.store.AppDatabaseHelper
import java.io.Serializable

data class Img2imgParam(
    val denoisingStrength: Float,
    val resizeMode: Int,
    val scaleBy: Float,
    val width: Int,
    val height: Int,
    val cfgScale: Float,
    val path: String,
    val historyId: Long,
    val maskPath: String? = null,
    val inpaint: Boolean? = false,
    val maskBlur: Float? = null,
    val maskInvert: Int? = null,
    val inpaintingFill: Int? = null,
    val inpaintingFullRes: Int? = null,
    val inpaintingFullResPadding: Int? = null,
) : Serializable

@Entity(tableName = "img2img")
data class Img2ImgEntity(
    @PrimaryKey(autoGenerate = true)
    val img2ImgId: Long = 0,
    val denoisingStrength: Float,
    val resizeMode: Int,
    val scaleBy: Float,
    val width: Int,
    val height: Int,
    val cfgScale: Float,
    val path: String,
    val historyId: Long,
    // inpaint
    val maskPath: String? = null,
    val inpaint: Boolean? = false,
    val maskBlur: Float? = null,
    val maskInvert: Int? = null,
    val inpaintingFill: Int? = null,
    val inpaintingFullRes: Int? = null,
    val inpaintingFullResPadding: Int? = null,

    ) {
    companion object {
        fun fromImg2imgParam(img2imgParam: Img2imgParam, historyId: Long): Img2ImgEntity {
            return Img2ImgEntity(
                denoisingStrength = img2imgParam.denoisingStrength,
                resizeMode = img2imgParam.resizeMode,
                scaleBy = img2imgParam.scaleBy,
                width = img2imgParam.width,
                height = img2imgParam.height,
                cfgScale = img2imgParam.cfgScale,
                path = img2imgParam.path,
                historyId = historyId,
                maskPath = img2imgParam.maskPath,
                inpaint = img2imgParam.inpaint,
                maskBlur = img2imgParam.maskBlur,
                maskInvert = img2imgParam.maskInvert,
                inpaintingFill = img2imgParam.inpaintingFill,
                inpaintingFullRes = img2imgParam.inpaintingFullRes,
            )
        }
    }

    fun toImg2imgParam(): Img2imgParam {
        return Img2imgParam(
            denoisingStrength = denoisingStrength,
            resizeMode = resizeMode,
            scaleBy = scaleBy,
            width = width,
            height = height,
            cfgScale = cfgScale,
            path = path,
            historyId = historyId,
            maskPath = maskPath,
            inpaint = inpaint,
            maskBlur = maskBlur,
            maskInvert = maskInvert,
            inpaintingFill = inpaintingFill,
            inpaintingFullRes = inpaintingFullRes,
        )
    }
}

@Dao
interface Img2ImgDao {
    @Insert
    fun insert(img2ImgEntity: Img2ImgEntity)

    @Update
    fun update(img2ImgEntity: Img2ImgEntity)

    @Query("SELECT * FROM img2img WHERE historyId = :historyId")
    fun getImg2ImgParam(historyId: Long): Img2ImgEntity?
}

fun SaveHistory.saveImg2Img(context: Context) {
    val database = AppDatabaseHelper.getDatabase(context)
    img2imgParam?.let {
        var ent = Img2ImgEntity(
            denoisingStrength = it.denoisingStrength,
            resizeMode = it.resizeMode,
            scaleBy = it.scaleBy,
            width = it.width,
            height = it.height,
            cfgScale = it.cfgScale,
            path = it.path,
            historyId = id,
        )
        if (it.inpaint == true) {
            ent = ent.copy(
                maskPath = it.maskPath,
                inpaint = it.inpaint,
                maskBlur = it.maskBlur,
                maskInvert = it.maskInvert,
                inpaintingFill = it.inpaintingFill,
                inpaintingFullRes = it.inpaintingFullRes,
                inpaintingFullResPadding = it.inpaintingFullResPadding,
            )
        }
        database.img2ImgDao().insert(
            ent
        )
    }
}