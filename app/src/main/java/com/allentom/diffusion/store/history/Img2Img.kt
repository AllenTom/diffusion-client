package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.allentom.diffusion.Util
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.ui.screens.home.tabs.draw.Img2ImgParam
import java.io.Serializable
import java.util.UUID

data class SavedImg2imgParam(
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
) : Serializable {
    companion object {
        fun fromImg2ImgParam(img2ImgParam: Img2ImgParam, savePath: String): SavedImg2imgParam {
            return SavedImg2imgParam(
                denoisingStrength = img2ImgParam.denoisingStrength,
                resizeMode = img2ImgParam.resizeMode,
                scaleBy = img2ImgParam.scaleBy,
                width = img2ImgParam.width,
                height = img2ImgParam.height,
                cfgScale = img2ImgParam.cfgScale,
                path = savePath,
                historyId = 0
            )
        }
        fun create(context:Context,img2ImgParam: Img2ImgParam): SavedImg2imgParam? {
            var savedImg2ImgParam: SavedImg2imgParam? = null
            val saveFilename = img2ImgParam.imgFilename ?: "${UUID.randomUUID()}.png"
            img2ImgParam.imgBase64?.let { inputImageBase64 ->
                val savePath = Util.saveImg2ImgFile(
                    context,
                    inputImageBase64,
                    saveFilename
                )
                savedImg2ImgParam = fromImg2ImgParam(img2ImgParam, savePath)
                if (img2ImgParam.inpaint && img2ImgParam.mask != null) {
                    val maskFilename = "${UUID.randomUUID()}.png"
                    val maskPath = Util.saveImg2ImgMaskFile(
                        context,
                        img2ImgParam.mask,
                        maskFilename
                    )
                    savedImg2ImgParam = savedImg2ImgParam?.copy(
                        maskPath = maskPath,
                        inpaint = true,
                        maskBlur = img2ImgParam.maskBlur,
                        maskInvert = img2ImgParam.inpaintingMaskInvert,
                        inpaintingFill = img2ImgParam.inpaintingFill,
                        inpaintingFullRes = img2ImgParam.inpaintingFullRes,
                        inpaintingFullResPadding = img2ImgParam.inpaintingFullResPadding
                    )
                }
            }
            return savedImg2ImgParam
        }
    }
}

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
        fun fromImg2imgParam(savedImg2ImgParam: SavedImg2imgParam, historyId: Long): Img2ImgEntity {
            return Img2ImgEntity(
                denoisingStrength = savedImg2ImgParam.denoisingStrength,
                resizeMode = savedImg2ImgParam.resizeMode,
                scaleBy = savedImg2ImgParam.scaleBy,
                width = savedImg2ImgParam.width,
                height = savedImg2ImgParam.height,
                cfgScale = savedImg2ImgParam.cfgScale,
                path = savedImg2ImgParam.path,
                historyId = historyId,
                maskPath = savedImg2ImgParam.maskPath,
                inpaint = savedImg2ImgParam.inpaint,
                maskBlur = savedImg2ImgParam.maskBlur,
                maskInvert = savedImg2ImgParam.maskInvert,
                inpaintingFill = savedImg2ImgParam.inpaintingFill,
                inpaintingFullRes = savedImg2ImgParam.inpaintingFullRes,
            )
        }
    }

    fun toImg2imgParam(): SavedImg2imgParam {
        return SavedImg2imgParam(
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
    savedImg2ImgParam?.let {
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