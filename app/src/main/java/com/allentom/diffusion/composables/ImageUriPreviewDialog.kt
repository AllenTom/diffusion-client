package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage

class ImagePreviewDialogState {
    var isOpen by mutableStateOf(false)
    var imageUri by mutableStateOf<Any?>(null)
    fun openPreview(imageUri: Any?) {
        this.imageUri = imageUri
        isOpen = true
    }
}
@Composable
fun rememberImagePreviewDialogState(): ImagePreviewDialogState {
    return remember {
        ImagePreviewDialogState()
    }
}
@Composable
fun ImageUriPreviewDialogWrapper(
    state: ImagePreviewDialogState,
) {
    if (state.isOpen) {
        ImageUriPreviewDialog(imageUri = state.imageUri, onDismissRequest = {
            state.isOpen = false
        })
    }
}
@Composable
fun ImageUriPreviewDialog(
    imageUri: Any?,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = {
        onDismissRequest()
    }) {
        // This Box represents the full screen dialog
        AsyncImage(model = imageUri, contentDescription = "Image", modifier = Modifier.fillMaxSize())
    }
}