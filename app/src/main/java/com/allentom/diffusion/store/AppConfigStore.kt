package com.allentom.diffusion.store

import android.content.Context
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImageFilter
import com.allentom.diffusion.ui.screens.extra.ExtraImageParam
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class AppConfig(
    val sdwUrl: String? = null,
    val saveUrls: List<String> = emptyList(),
    val extraImageHistory: ExtraImageParam? = null,
    val isInitPrompt: Boolean = true,
    val saveCivitaiImageFilter: CivitaiImageFilter? = null,
    val enablePlugin: Boolean = false
)

object AppConfigStore {
    lateinit var config: AppConfig
    private val STORE_KEY = "APP_CONFIG"
    fun refresh(context: Context) {
        val sharedPreferences = context.getSharedPreferences(this.STORE_KEY, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("config", null)

        if (json != null) {
            val type = object : TypeToken<AppConfig>() {}.type
            config = Gson().fromJson(json, type)
        } else {
            config = AppConfig()
        }
    }

    fun saveData(context: Context) {
        val sharedPreferences = context.getSharedPreferences(this.STORE_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val json = Gson().toJson(config)

        editor.putString("config", json)
        editor.apply()
    }

    fun updateCivitaiImageFilter(context: Context, filter: CivitaiImageFilter) {
        config = config.copy(saveCivitaiImageFilter = filter)
        saveData(context)
    }
}