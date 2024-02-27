package com.allentom.diffusion.composables

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun IsWideWindow(activity: Activity = LocalContext.current as Activity): Boolean {
    val windowSizeClass = calculateWindowSizeClass(activity = activity)
    val isWideDisplay: Boolean by remember {
        derivedStateOf {
            windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
        }
    }
    return isWideDisplay
}

enum class DeviceType(name: String) {
    Phone("Phone"),
    Foldable("Foldable"),
    Tablet("Tablet"),
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun DetectDeviceType(activity: Activity = LocalContext.current as Activity): DeviceType {
    val windowSizeClass = calculateWindowSizeClass(activity = activity)
    val isWideDisplay: String by remember {
        derivedStateOf {
            if (
                windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
            ) {
                "Tablet"
            } else if (
                windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
            ) {
                "Foldable"
            } else {
                "Phone"
            }
        }
    }
    return DeviceType.valueOf(isWideDisplay)
}
