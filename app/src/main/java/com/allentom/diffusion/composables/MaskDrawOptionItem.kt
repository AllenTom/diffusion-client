package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.extension.toBase64
import com.allentom.diffusion.store.LoraPrompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image

@Composable
fun MaskDrawOptionItem(
    label: String,
    value: String?,
    title: String = label,
    backgroundImageBase64: String? = null,
    onValueChange: (String) -> Unit = {},
) {
    var showDialog by remember { mutableStateOf(false) }
    fun onConfirm(imageBitmap: ImageBitmap) {
        // cast to base64
        val base64 = imageBitmap.toBase64()
        onValueChange(base64)
    }
    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = title) },
        supportingContent = {
            value?.let {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                ) {
                    DisplayBase64Image(base64String = value)
                }
            }
        }
    )
    if (showDialog) {
        InpaintDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false;
                onConfirm(it)
            },
            backgroundImageBase64 = backgroundImageBase64,
        )
    }
}