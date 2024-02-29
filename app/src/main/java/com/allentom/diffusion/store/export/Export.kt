package com.allentom.diffusion.store.export

import com.allentom.diffusion.store.history.SaveHistory
import com.allentom.diffusion.store.prompt.EmbeddingPrompt
import com.allentom.diffusion.store.prompt.LoraPrompt
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.ControlNetParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.ControlNetSlot
import com.google.gson.Gson
import java.io.Serializable

data class ExportLoraPrompt(
    val name: String,
    var weight: Float,
    val hash: String? = null,
    val prompts: List<ExportPrompt> = emptyList(),
    val triggerText: List<ExportPrompt> = emptyList(),
    val civitaiId: Long? = null,
) {
    fun toLoraPrompt(): LoraPrompt {
        return LoraPrompt(
            name = name,
            weight = weight,
            hash = hash,
            prompts = prompts.map { it.toPrompt() },
            triggerText = triggerText.map { it.toPrompt() },
            civitaiId = civitaiId
        )
    }
}

class ExportEmbeddingPrompt(
    var text: String,
    var piority: Int,
) {
    fun toEmbeddingPrompt(): EmbeddingPrompt {
        return EmbeddingPrompt(
            text = text,
            piority = piority
        )
    }
}

data class ExportControlNetParam(
    val slots: List<ExportControlNetSlot> = listOf(ExportControlNetSlot())
) {
    companion object {
        fun fromControlNetParam(param: ControlNetParam): ExportControlNetParam {
            return ExportControlNetParam(
                slots = param.slots.map {
                    ExportControlNetSlot.fromControlNetSlot(it)
                }
            )
        }
    }
}

data class ExportControlNetSlot(
    val enabled: Boolean? = false,
    val guidanceStart: Float? = 0f,
    val guidanceEnd: Float? = 1f,
    val controlMode: Int? = 0,
    val weight: Float? = 1f,
    val model: String? = null,
    val inputImage: String? = null,
    val controlType:String? = null,
    val preprocessor: String? = null,
    val processorRes: Float? = null,
    val thresholdA : Float? = null,
    val thresholdB : Float? = null,
    val resizeMode: Int? = null,
) {
    companion object {
        fun fromControlNetSlot(slot: ControlNetSlot): ExportControlNetSlot {
            return ExportControlNetSlot(
                enabled = slot.enabled,
                guidanceStart = slot.guidanceStart,
                guidanceEnd = slot.guidanceEnd,
                controlMode = slot.controlMode,
                weight = slot.weight,
                model = slot.model,
                inputImage = slot.inputImage,
                controlType = slot.controlType,
                preprocessor = slot.preprocessor,
                processorRes = slot.processorRes,
                thresholdA = slot.thresholdA,
                thresholdB = slot.thresholdB,
                resizeMode = slot.resizeMode
            )
        }
    }
}

data class ExportHistory(
    var version: Int = 1,
    var prompt: List<ExportPrompt>? = null,
    var negativePrompt: List<ExportPrompt>? = null,
    var steps: Int? = null,
    var samplerName: String? = null,
    var width: Int? = null,
    var height: Int? = null,
    var cfgScale: Float? = null,
    var loraPrompt: List<ExportLoraPrompt>? = null,
    var embeddingPrompt: List<ExportEmbeddingPrompt>? = null,
    var controlNetParam: ExportControlNetParam? = null

) : Serializable {
    public fun assignWithHistory(saveHistory: SaveHistory) {
        this.prompt = saveHistory.prompt.map {
            ExportPrompt.fromPrompt(it)
        }
        this.negativePrompt = saveHistory.negativePrompt.map {
            ExportPrompt.fromPrompt(it)
        }
        this.steps = saveHistory.steps
        this.samplerName = saveHistory.samplerName
        this.width = saveHistory.width
        this.height = saveHistory.height
        this.cfgScale = saveHistory.cfgScale
        saveHistory.controlNetParam?.let {
            this.controlNetParam = ExportControlNetParam.fromControlNetParam(it)
        }
        this.loraPrompt = saveHistory.loraPrompt.map {
            ExportLoraPrompt(
                name = it.name,
                weight = it.weight,
                hash = it.hash,
                prompts = it.prompts.map { ExportPrompt.fromPrompt(it) },
                triggerText = it.triggerText.map { ExportPrompt.fromPrompt(it) },
                civitaiId = it.civitaiId
            )
        }
    }

    companion object {
        fun readFromRaw(rawContent: String): ExportHistory {
            val gson = Gson()
            return gson.fromJson(rawContent, ExportHistory::class.java)
        }
    }
}

data class ExportPrompt(
    var text: String,
    var piority: Int,
    var translation: String? = null,
    var regionIndex: Int = 0,
) : Serializable {
    fun toPrompt(): Prompt {
        return Prompt(
            text = text,
            piority = piority,
            translation = translation,
            regionIndex = regionIndex
        )
    }

    companion object {
        fun fromPrompt(prompt: Prompt): ExportPrompt {
            return ExportPrompt(
                text = prompt.text,
                piority = prompt.piority,
                translation = prompt.translation,
                regionIndex = prompt.regionIndex
            )
        }
    }
}