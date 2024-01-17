package com.allentom.diffusion.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

object HomeViewModel:ViewModel() {
    var selectedIndex by mutableIntStateOf(0)
    var galleryItemImageFit by mutableIntStateOf(0)
}