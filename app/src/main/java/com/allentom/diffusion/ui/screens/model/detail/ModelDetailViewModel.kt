package com.allentom.diffusion.ui.screens.model.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.allentom.diffusion.api.civitai.entities.CivitaiImageItem
import com.allentom.diffusion.api.civitai.entities.CivitaiModel
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.ModelEntity
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImageFilter

object ModelDetailViewModel {
    var civitaiImageList by mutableStateOf<List<CivitaiImageItem>>(emptyList())

    var page by mutableStateOf(1)

    var pageSize by mutableStateOf(20)

    var isLoading by mutableStateOf(false)

    var filter by mutableStateOf(CivitaiImageFilter())

    var model by mutableStateOf<ModelEntity?>(null)

    var civitaiModel by mutableStateOf<CivitaiModel?>(null)

    var selectedTabIndex by mutableStateOf(0)

    fun asNew() {
        civitaiImageList = emptyList()
        page = 1
        isLoading = false
        filter = AppConfigStore.config.saveCivitaiImageFilter ?:CivitaiImageFilter()
        model = null
        civitaiModel = null
        selectedTabIndex = 0
    }
}