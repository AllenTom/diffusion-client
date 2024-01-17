package com.allentom.diffusion.ui.screens.prompt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

object CategoryScreenViewModel:ViewModel() {
    var categoryName by mutableStateOf("")
}