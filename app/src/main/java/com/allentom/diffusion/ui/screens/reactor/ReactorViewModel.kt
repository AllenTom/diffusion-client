package com.allentom.diffusion.ui.screens.reactor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorParam

object ReactorViewModel : ViewModel() {
    var param by mutableStateOf(ReactorParam())
    var targetImage by mutableStateOf(null as String?)
    var targetImageFileName by mutableStateOf(null as String?)
    var resultImage by mutableStateOf(null as String?)
}