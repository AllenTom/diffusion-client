package com.allentom.diffusion.ui.screens.civitai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.allentom.diffusion.api.civitai.entities.CivitaiImageItem
import com.allentom.diffusion.api.civitai.entities.Image
import com.allentom.diffusion.ui.screens.reactor.ImageItem

object CivitaiImageViewModel {
    var image by mutableStateOf<CivitaiImageItem?>(null)
}