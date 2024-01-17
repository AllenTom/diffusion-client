package com.allentom.diffusion.ui.screens.historydetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

object HistoryDetailViewModel: ViewModel() {
    var historyId by mutableLongStateOf(0L)

}