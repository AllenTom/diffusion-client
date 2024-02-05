package com.allentom.diffusion.ui.screens.style

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.allentom.diffusion.store.prompt.PromptStyle

object StylesViewModel:ViewModel() {
    var styles by mutableStateOf<List<PromptStyle>>(listOf())
}