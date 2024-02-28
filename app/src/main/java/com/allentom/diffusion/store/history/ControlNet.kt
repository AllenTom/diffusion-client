package com.allentom.diffusion.store.history

import android.content.Context
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Update
import com.allentom.diffusion.Util
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.ControlNetEntity
import com.allentom.diffusion.ui.screens.home.tabs.draw.ControlNetParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.ControlNetSlot

@Entity(tableName = "control_net_history")
data class ControlNetHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val controlNetHistoryId: Long = 0,
    val controlNetId: Long,
    val historyId: Long,
    val processorRes: Float,
    val thresholdA: Float,
    val thresholdB: Float,
    val guidanceStart: Float,
    val guidanceEnd: Float,
    val controlMode: Int,
    val weight: Float,
    val model: String,
    val active: Boolean? = false,
    @ColumnInfo(defaultValue = "All")
    val controlType: String = "All",
    @ColumnInfo(defaultValue = "none")
    val preprocessor:String = "none",
    @ColumnInfo(defaultValue = "1")
    val resizeMode: Int = 1,
) {
    fun toControlNetSlot(): ControlNetSlot {
        return ControlNetSlot(
            controlNetHistoryId = controlNetHistoryId,
            controlNetId = controlNetId,
            historyId = historyId,
            guidanceStart = guidanceStart,
            guidanceEnd = guidanceEnd,
            controlMode = controlMode,
            weight = weight,
            model = model,
            enabled = active ?: false,
            controlType = controlType,
            preprocessor = preprocessor,
            processorRes = processorRes,
            thresholdA = thresholdA,
            thresholdB = thresholdB,
            resizeMode = resizeMode
        )
    }
}

@Dao
interface ControlNetHistoryDao {
    @Insert
    fun insert(controlNetHistoryEntity: ControlNetHistoryEntity)

    @Update
    fun update(controlNetHistoryEntity: ControlNetHistoryEntity)

    @Query("SELECT * FROM control_net_history WHERE historyId = :historyId")
    fun getControlNetHistory(historyId: Long): ControlNetHistoryEntity?

    @Query("SELECT * FROM control_net_history WHERE controlNetId = :controlNetId")
    fun getControlNetHistoryWithControlNetByControlNetId(controlNetId: Long): ControlNetHistoryWithRelation?
}

data class ControlNetHistoryWithRelation(
    @Embedded val controlNetHistoryEntity: ControlNetHistoryEntity,
    @Relation(
        parentColumn = "controlNetId",
        entityColumn = "controlNetId",
    )
    val controlNetEntity: ControlNetEntity,
)

fun SaveHistory.saveControlNet(context: Context) {
    val database = AppDatabaseHelper.getDatabase(context)
    controlNetParam?.let { controlNetParam ->
        if (controlNetParam.slots.all { !it.enabled }) {
            return@let
        }
        controlNetParam.slots.forEach { slot ->
            val md5 = Util.getMd5FromImageBase64(slot.inputImage!!)
            val db = AppDatabaseHelper.getDatabase(context)
            val controlNetEntity = db.controlNetDao().getByMd5(md5)
            val savedControlNetEntity = controlNetEntity
                ?: db.controlNetDao().insert(
                    ControlNetEntity(
                        path = Util.saveControlNetToAppData(
                            context,
                            Uri.parse(slot.inputImagePath!!),
                        ),
                        md5 = md5,
                        time = System.currentTimeMillis()
                    )
                ).let {
                    db.controlNetDao().getById(it)
                }
            savedControlNetEntity?.let {
                val controlNetId = savedControlNetEntity.controlNetId
                imagePaths.firstOrNull()?.let { imgHistory ->
                    controlNetEntity?.let {
                        if (it.previewPath.isNotEmpty()) {
                            return@let
                        }
                        val previewPath = Util.saveControlNetPreviewToAppData(
                            context,
                            imgHistory.path,
                            md5
                        )
                        database.controlNetDao().update(
                            it.copy(
                                previewPath = previewPath
                            )
                        )
                    }

                }
                db.controlNetHistoryDao().insert(
                    ControlNetHistoryEntity(
                        controlNetId = controlNetId,
                        historyId = id,
                        guidanceStart = slot.guidanceStart,
                        guidanceEnd = slot.guidanceEnd,
                        controlMode = slot.controlMode,
                        weight = slot.weight,
                        model = slot.model!!,
                        processorRes = slot.processorRes,
                        thresholdA = slot.thresholdA,
                        thresholdB = slot.thresholdB,
                        active = slot.enabled,
                        controlType = slot.controlType,
                        preprocessor = slot.preprocessor,
                        resizeMode = slot.resizeMode
                    )
                )
            }
        }

    }
}

fun HistoryWithRelation.toControlNetParam(): ControlNetParam {
    return ControlNetParam(
        slots = controlNetHistoryEntity.map {
            it.toControlNetSlot()
        },
    )
}