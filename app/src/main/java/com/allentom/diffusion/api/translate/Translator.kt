package com.allentom.diffusion.api.translate

import com.allentom.diffusion.api.translate.baidutranslate.BaiduTranslator
import com.allentom.diffusion.api.translate.googletranslate.GoogleTranslator
import com.allentom.diffusion.store.AppConfigStore


data class TranslateResult(
    val sentences: List<String>,
)

interface Translator {
    suspend fun translate(
        text: String, source: TranslateLanguages = TranslateLanguages.Auto,
        to: TranslateLanguages = TranslateLanguages.English
    ): TranslateResult

    fun init()

    fun supportFromLanguage(): List<TranslateLanguages> {
        return listOf(TranslateLanguages.Auto)
    }

    fun supportToLanguage(): List<TranslateLanguages> {
        return listOf(TranslateLanguages.English)
    }
}

enum class TranslateLanguages {
    Auto,
    Chinese,
    English,
    Korean,
    Japanese,
    French,
    Spanish,
    Thai,
    Arabic,
    Russian,
    Portuguese,
    German,
    Italian,
    Greek,
    Dutch,
    Polish,
    Bulgarian,
    Estonian,
    Danish,
    Finnish,
    Czech,
    Romanian,
    Slovenian,
    Swedish,
    Hungarian,
    TraditionalChinese,
    Vietnamese
}

class TranslateHelper {
    companion object {
        lateinit var currentTranslator: Translator
        fun initTranslator() {
            when (AppConfigStore.config.translateEngine) {
                "Google" -> {
                    currentTranslator = GoogleTranslator()
                }

                "Baidu" -> {
                    currentTranslator = BaiduTranslator()
                }
            }
            currentTranslator.init()
        }

        fun getTranslator(): Translator {
            return this.currentTranslator
        }

        suspend fun translateText(
            text: String,
            source: TranslateLanguages = TranslateLanguages.Auto,
            to: TranslateLanguages = TranslateLanguages.English
        ): TranslateResult {
            return getTranslator().translate(text, source, to)
        }

        fun getFromLanguage(): List<TranslateLanguages> {
            return getTranslator().supportFromLanguage()
        }

        fun getToLanguage(): List<TranslateLanguages> {
            return getTranslator().supportToLanguage()
        }
    }
}
