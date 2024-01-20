package com.allentom.diffusion.extension

import androidx.compose.ui.Modifier

fun Modifier.thenIf(condition: Boolean, other: Modifier): Modifier =
    if (condition) this.then(other) else this