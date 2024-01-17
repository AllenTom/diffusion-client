package com.allentom.diffusion.ui.screens.promptdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

object PromptDetailViewModel:ViewModel() {
    var promptId by mutableLongStateOf(0L)
}