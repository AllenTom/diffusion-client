package com.allentom.diffusion.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun gridCountForDeviceWidth(itemWidth: Int): Int {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columns = screenWidthDp / itemWidth
    return columns
}