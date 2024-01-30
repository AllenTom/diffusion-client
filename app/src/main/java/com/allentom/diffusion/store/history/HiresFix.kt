package com.allentom.diffusion.store.history

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.allentom.diffusion.store.AppDatabaseHelper
import com.allentom.diffusion.store.Prompt
import java.io.Serializable

class SaveHrParam(
    val enableScale: Boolean,
    val hrScale: Float,
    val hrDenosingStrength: Float,
    val hrUpscaler: String,
) : Serializable {
    fun toEntity(): HrHistoryEntity {
        return HrHistoryEntity(
            enableScale = enableScale,
            hrScale = hrScale,
            hrDenosingStrength = hrDenosingStrength,
            hrUpscaler = hrUpscaler,
            historyId = 0,
        )
    }
}

@Entity(tableName = "hr_history")
data class HrHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val hrHistoryId: Long = 0,
    val enableScale: Boolean,
    val hrScale: Float,
    val hrDenosingStrength: Float,
    val hrUpscaler: String,
    val historyId: Long,
) {
    companion object {
        fun fromHrParam(hrParam: SaveHrParam, historyId: Long): HrHistoryEntity {
            return HrHistoryEntity(
                enableScale = hrParam.enableScale,
                hrScale = hrParam.hrScale,
                hrDenosingStrength = hrParam.hrDenosingStrength,
                hrUpscaler = hrParam.hrUpscaler,
                historyId = historyId,
            )
        }

    }

    fun toSaveHrParam(): SaveHrParam {
        return SaveHrParam(
            enableScale = enableScale,
            hrScale = hrScale,
            hrDenosingStrength = hrDenosingStrength,
            hrUpscaler = hrUpscaler,
        )
    }
}

@Dao
interface HrHistoryDao {
    @Insert
    fun insert(hrHistoryEntity: HrHistoryEntity)

    @Update
    fun update(hrHistoryEntity: HrHistoryEntity)

    @Query("SELECT * FROM hr_history WHERE historyId = :historyId limit 1")
    fun getHrHistory(historyId: Long): HrHistoryEntity?
}

fun SaveHistory.saveHiresFix(context: Context) {
    val database = AppDatabaseHelper.getDatabase(context)
    hrParam.let { hrParam ->
        database.hrHistoryDao().insert(
            HrHistoryEntity.fromHrParam(hrParam, id)
        )
    }
}

fun HistoryWithRelation.toHiresFixParam(): SaveHrParam {
    return hrParamEntity?.toSaveHrParam() ?: SaveHrParam(
        enableScale = false,
        hrScale = 1.0f,
        hrDenosingStrength = 0.0f,
        hrUpscaler = "None",
    )
}