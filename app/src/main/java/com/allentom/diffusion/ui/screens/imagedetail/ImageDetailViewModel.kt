package com.allentom.diffusion.ui.screens.imagedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

object ImageDetailViewModel:ViewModel() {
    var imageName by mutableStateOf("")
}