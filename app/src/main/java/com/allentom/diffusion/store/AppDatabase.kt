package com.allentom.diffusion.store

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.allentom.diffusion.store.history.AdetailerDao
import com.allentom.diffusion.store.history.AdetailerEntity
import com.allentom.diffusion.store.history.ControlNetHistoryDao
import com.allentom.diffusion.store.history.ControlNetHistoryEntity
import com.allentom.diffusion.store.history.EmbeddingHistoryCrossRef
import com.allentom.diffusion.store.history.EmbeddingHistoryDao
import com.allentom.diffusion.store.history.HistoryDao
import com.allentom.diffusion.store.history.HistoryEntity
import com.allentom.diffusion.store.history.HrHistoryDao
import com.allentom.diffusion.store.history.HrHistoryEntity
import com.allentom.diffusion.store.history.ImageHistoryDao
import com.allentom.diffusion.store.history.ImageHistoryEntity
import com.allentom.diffusion.store.history.Img2ImgDao
import com.allentom.diffusion.store.history.Img2ImgEntity
import com.allentom.diffusion.store.history.LoraPromptHistoryCrossRef
import com.allentom.diffusion.store.history.LoraPromptHistoryDao
import com.allentom.diffusion.store.history.NegativePromptHistoryCrossRef
import com.allentom.diffusion.store.history.NegativePromptHistoryDao
import com.allentom.diffusion.store.history.PromptExtraDao
import com.allentom.diffusion.store.history.PromptExtraEntity
import com.allentom.diffusion.store.history.PromptHistoryCrossRef
import com.allentom.diffusion.store.history.PromptHistoryDao
import com.allentom.diffusion.store.history.XYZDao
import com.allentom.diffusion.store.history.XYZHistoryEntity
import com.allentom.diffusion.store.prompt.EmbeddingDao
import com.allentom.diffusion.store.prompt.EmbeddingEntity
import com.allentom.diffusion.store.prompt.LoraPromptDao
import com.allentom.diffusion.store.prompt.LoraPromptEntity
import com.allentom.diffusion.store.prompt.LoraTriggerCrossRef
import com.allentom.diffusion.store.prompt.LoraTriggerDao
import com.allentom.diffusion.store.prompt.PromptDao
import com.allentom.diffusion.store.prompt.SavePrompt
import com.allentom.diffusion.store.prompt.StyleDao
import com.allentom.diffusion.store.prompt.StyleEntity
import com.allentom.diffusion.store.prompt.StylePromptCrossRef
import com.allentom.diffusion.store.prompt.StylePromptDao

@Database(
    entities = [
        SavePrompt::class,
        HistoryEntity::class,
        HrHistoryEntity::class,
        ImageHistoryEntity::class,
        LoraPromptEntity::class,
        EmbeddingEntity::class,
        Img2ImgEntity::class,
        PromptExtraEntity::class,
        ControlNetHistoryEntity::class,
        PromptHistoryCrossRef::class,
        NegativePromptHistoryCrossRef::class,
        LoraPromptHistoryCrossRef::class,
        EmbeddingHistoryCrossRef::class,
        ControlNetEntity::class,
        ModelEntity::class,
        LoraTriggerCrossRef::class,
        AdetailerEntity::class,
        XYZHistoryEntity::class,
        StyleEntity::class,
        StylePromptCrossRef::class,
    ], version = 15, exportSchema = true, autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15),
    ]
)


abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao
    abstract fun loraPromptDao(): LoraPromptDao
    abstract fun hrHistoryDao(): HrHistoryDao
    abstract fun imageHistoryDao(): ImageHistoryDao
    abstract fun img2ImgDao(): Img2ImgDao
    abstract fun historyDao(): HistoryDao
    abstract fun loraPromptHistoryDao(): LoraPromptHistoryDao
    abstract fun promptHistoryDao(): PromptHistoryDao
    abstract fun negativePromptHistoryDao(): NegativePromptHistoryDao
    abstract fun promptExtraDao(): PromptExtraDao
    abstract fun embeddingDao(): EmbeddingDao
    abstract fun embeddingHistoryDao(): EmbeddingHistoryDao
    abstract fun controlNetHistoryDao(): ControlNetHistoryDao
    abstract fun controlNetDao(): ControlNetDao
    abstract fun modelDao(): ModelDao
    abstract fun loraTriggerDao(): LoraTriggerDao
    abstract fun adetailerDao(): AdetailerDao
    abstract fun xyzDao(): XYZDao
    abstract fun styleDao(): StyleDao
    abstract fun stylePromptDao(): StylePromptDao

}

object AppDatabaseHelper {
    private var database: AppDatabase? = null
    fun getDatabase(context: Context): AppDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "diffusion"
            ).build()
        }
        return database!!
    }

}