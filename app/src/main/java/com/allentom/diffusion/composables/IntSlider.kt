package com.allentom.diffusion.composables

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun IntSlider(
    value: Int,
    range: IntRange,
    modifier: Modifier,
    onValueChange: (Int) -> Unit
) {
    Slider(
        modifier = modifier,
        value = value.toFloat(),
        onValueChange = { onValueChange(it.toInt()) },
        valueRange = range.first.toFloat()..range.last.toFloat()
    )
}