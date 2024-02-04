package com.allentom.diffusion.ui.screens.reactor

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorParam
data class ImageItem(
    val uri: Uri,
    val name: String,
    val resultImage: String? = null,
    val isGenerating: Boolean = false,
    val isExport: Boolean = false,
)
object ReactorViewModel : ViewModel() {
    var param by mutableStateOf(ReactorParam())
    var images by mutableStateOf(emptyList<ImageItem>())
    var isProcessing by mutableStateOf(false)
    var stopFlag by mutableStateOf(false)
    fun onStartGenerating(onlyIndex: Int? = null) {
        images = images.mapIndexed { index, inputImageItem ->
            if (onlyIndex != null) {
                if (index != onlyIndex) {
                    return@mapIndexed inputImageItem
                }
                return@mapIndexed inputImageItem.copy(isGenerating = true, resultImage = null)
            }
            inputImageItem.copy(isGenerating = true, resultImage = null)
        }
    }
    fun addToReactorImages(uri: Uri, name: String) {
        images = listOf(ImageItem(uri, name))
        param = param.copy(singleImageResult = null)
    }
}