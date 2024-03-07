package com.allentom.diffusion.store

import android.content.Context
import com.allentom.diffusion.api.translate.TranslateLanguages
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImageFilter
import com.allentom.diffusion.ui.screens.extra.ExtraImageParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorParam
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class AppConfig(
    val sdwUrl: String? = null,
    val saveUrls: List<String> = emptyList(),
    val saveAuths: Map<String, String> = emptyMap(),
    val extraImageHistory: ExtraImageParam? = null,
    val reactorImageHistory: ReactorParam? = null,
    val isInitPrompt: Boolean = true,
    val saveCivitaiImageFilter: CivitaiImageFilter? = null,
    val modelViewDisplayMode: String = "Crop",
    val loraViewDisplayMode: String = "Crop",
    val promptDetailViewDisplayMode: String = "Crop",
    val disbaleSSLVerification: Boolean = false,
    var translateEngine: String = "Google",
    var baiduTranslateAppId: String = "",
    var baiduTranslateSecretKey: String = "",
    var onlyDisplayTranslateOnPromptSelectDialog: Boolean = true,
    val preferredLanguage: TranslateLanguages = TranslateLanguages.English,
    val importCivitaiDialogTranslate:Boolean = true,
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

    fun updateAndSave(context: Context, block: (AppConfig) -> AppConfig) {
        config = block(config)
        saveData(context)
    }

    fun updateCivitaiImageFilter(context: Context, filter: CivitaiImageFilter) {
        config = config.copy(saveCivitaiImageFilter = filter)
        saveData(context)
    }
}