package com.allentom.diffusion.store.civitai

import android.content.Context
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.civitai.entities.CivitaiImageItem
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.api.entity.Model
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.store.prompt.PromptStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

data class CivitaiImage(
    val raw: CivitaiImageItem,
    var width: Int? = null,
    var height: Int? = null,
    var resources: List<ImportResource> = emptyList(),
    var promptList: List<Prompt> = emptyList(),
    var negativePromptList: List<Prompt> = emptyList()
) {

    companion object {
        fun fromCivitaiImageItem(context: Context, data: CivitaiImageItem): CivitaiImage {
            var civitaiImage = CivitaiImage(data)
            data.meta?.let { meta ->
                // parse size
                meta.size?.split("x")?.let {
                    civitaiImage.width = it[0].toInt()
                    civitaiImage.height = it[1].toInt()
                }
                // parse resource
                meta.resources?.forEach {
                    when (it.type) {
                        "model" -> {
                            val model = it.hash?.let { hash ->
                                DrawViewModel.models.find { model ->
                                    model.sha256?.startsWith(hash) ?: false
                                }
                            }
                            civitaiImage.resources += ImportResource(
                                type = it.type,
                                name = it.name,
                                hash = it.hash,
                                weight = it.weight,
                                model = model
                            )
                        }

                        "lora" -> {
                            val lora = DrawViewModel.loraList.find { lora ->
                                lora.name == it.name
                            }
                            civitaiImage.resources += ImportResource(
                                type = it.type,
                                name = it.name,
                                hash = it.hash,
                                weight = it.weight,
                                lora = lora
                            )
                        }
                    }
                }
                // parse prompt
                civitaiImage.promptList = meta.prompt?.split(",")?.filter { it.isNotEmpty() }?.map {
                    Util.parsePrompt(it)
                }?.map {
                    val savedPrompt =
                        PromptStore.getPromptByName(context, it.text)?.toPrompt()
                    if (savedPrompt != null) {
                        return@map savedPrompt
                    }
                    return@map it
                } ?: emptyList()

                civitaiImage.negativePromptList =
                    meta.negativePrompt?.split(",")?.filter { it.isNotEmpty() }
                        ?.map {
                            Util.parsePrompt(it)
                        }?.map {
                            val savedPrompt =
                                PromptStore.getPromptByName(context, it.text)?.toPrompt()
                            if (savedPrompt != null) {
                                return@map savedPrompt
                            }
                            return@map it
                        } ?: emptyList()
            }
            return civitaiImage
        }
    }
}

data class ImportResource(
    val id: String = Util.randomString(6),
    val type: String,
    val name: String,
    val hash: String?,
    val weight: Float? = null,
    val model: Model? = null,
    val lora: Lora? = null
)