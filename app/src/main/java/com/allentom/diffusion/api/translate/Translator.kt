package com.allentom.diffusion.api.translate

import com.allentom.diffusion.api.translate.googletranslate.GoogleTranslator


data class TranslateResult(
    val sentences: List<String>,
)

interface Translator {
    suspend fun translate(text: String): TranslateResult
    fun init()
}



class TranslateHelper {
    companion object {
        lateinit var currentTranslator: Translator
        fun initTranslator() {
            currentTranslator = GoogleTranslator()
            currentTranslator.init()
        }

        fun getTranslator(): Translator {
            return this.currentTranslator
        }
        suspend fun translateText(text: String): TranslateResult {
            return getTranslator().translate(text)
        }
    }
}
