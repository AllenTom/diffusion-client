package com.allentom.diffusion.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image

@Composable
fun ImageBase64PreviewDialog(
    imageBase64: String,
    isOpen: Boolean,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = {
        onDismissRequest()
    }) {
        // This Box represents the full screen dialog
        DisplayBase64Image(base64String = imageBase64)
    }
}