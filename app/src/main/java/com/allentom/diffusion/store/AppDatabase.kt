package com.allentom.diffusion.store

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
    ], version = 1, exportSchema = true, autoMigrations = [

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
    abstract fun promptExtraDao(): PromptExtraDao
    abstract fun negativePromptHistoryDao(): NegativePromptHistoryDao
    abstract fun embeddingDao(): EmbeddingDao
    abstract fun embeddingHistoryDao(): EmbeddingHistoryDao
    abstract fun controlNetHistoryDao(): ControlNetHistoryDao
    abstract fun controlNetDao(): ControlNetDao

    abstract fun modelDao(): ModelDao

    abstract fun loraTriggerDao(): LoraTriggerDao

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