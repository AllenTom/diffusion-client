package com.allentom.diffusion.ui.screens.lora.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.allentom.diffusion.api.civitai.entities.CivitaiImageItem
import com.allentom.diffusion.api.civitai.entities.CivitaiModel
import com.allentom.diffusion.api.civitai.entities.CivitaiModelVersion
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.prompt.LoraPromptWithRelation
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImageFilter
import com.allentom.diffusion.ui.screens.model.detail.ModelDetailViewModel

object LoraDetailViewModel {
    var civitaiImageList by mutableStateOf<List<CivitaiImageItem>>(emptyList())

    var page by mutableStateOf(1)

    var pageSize by mutableStateOf(20)

    var isLoading by mutableStateOf(false)

    var filter by mutableStateOf(CivitaiImageFilter())

    var loraModel by mutableStateOf<LoraPromptWithRelation?>(null)

    var civitaiModelVersion by mutableStateOf<CivitaiModelVersion?>(null)
    var civitaiModel by mutableStateOf<CivitaiModel?>(null)


    var isCivitaiModelLoading by mutableStateOf(false)

    var selectedTabIndex by mutableStateOf(0)

    fun asNew() {
        civitaiImageList = emptyList()
        page = 1
        isLoading = false
        ModelDetailViewModel.filter = AppConfigStore.config.saveCivitaiImageFilter ?:CivitaiImageFilter()
        loraModel = null
        civitaiModelVersion = null
        selectedTabIndex = 0
    }
}