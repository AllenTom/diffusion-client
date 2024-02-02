package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.ui.screens.home.tabs.draw.GenModifier
import com.allentom.diffusion.ui.screens.home.tabs.draw.ModifierLibrary
import com.allentom.diffusion.ui.screens.home.tabs.draw.XYZParam

@Entity(tableName = "xyzhistory")
data class XYZHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val xyzId: Long = 0,
    val xAxis: String?,
    val xAxisValue: String?,
    val yAxis: String?,
    val yAxisValue: String?,
    val historyId: Long,
)

@Dao
interface XYZDao {
    @androidx.room.Insert
    fun insert(xyzHistoryEntity: XYZHistoryEntity)

    @androidx.room.Update
    fun update(xyzHistoryEntity: XYZHistoryEntity)

    @androidx.room.Query("SELECT * FROM xyzhistory WHERE historyId = :historyId")
    fun getXYZHistory(historyId: Long): XYZHistoryEntity?
}

fun SaveHistory.saveXYZParam(context: Context) {
    val database = AppDatabaseHelper.getDatabase(context)
    val xyzDao = database.xyzDao()
    xyzParam?.let {
        with(it) {
            xyzDao.insert(
                XYZHistoryEntity(
                    xAxis = xAxis?.getKey(),
                    xAxisValue = xAxis?.toSaveData(),
                    yAxis = yAxis?.getKey(),
                    yAxisValue = yAxis?.toSaveData(),
                    historyId = id
                )
            )
        }
    }
}

fun HistoryWithRelation.toSaveXYZParam(): XYZParam {
    var xMod: GenModifier? = null
    xyzHistoryEntity?.xAxis?.let {
        xMod = ModifierLibrary.getModifierByName(it)
        xyzHistoryEntity.xAxisValue?.let {
            xMod?.fromSaveData(it)
        }
    }
    var yMod: GenModifier? = null
    xyzHistoryEntity?.yAxis?.let {
        yMod = ModifierLibrary.getModifierByName(it)
        xyzHistoryEntity.yAxisValue?.let {
            yMod?.fromSaveData(it)
        }
    }
    return XYZParam(
        xAxis = xMod,
        yAxis = yMod,
    )
}