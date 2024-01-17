package com.allentom.diffusion.ui.screens.prompt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.composables.PromptCart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptCartModal(
    isPromptCartOpen: Boolean,
    onDismissRequest: () -> Unit
) {
    if (isPromptCartOpen) {
        ModalBottomSheet(onDismissRequest = {
            onDismissRequest()
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                PromptCart()
            }

        }
    }

}