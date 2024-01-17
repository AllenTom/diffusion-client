package com.allentom.diffusion.ui.screens.civitai.images

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.allentom.diffusion.api.civitai.entities.CivitaiImageItem

object  CivitaiImageListViewModel {
    var imageList by mutableStateOf<List<CivitaiImageItem>>(emptyList())
}