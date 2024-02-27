package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.DetectDeviceType
import com.allentom.diffusion.composables.DeviceType
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.store.history.SaveHrParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HiresFixPanel(
    onValueChange: (SaveHrParam) -> Unit,
    param: SaveHrParam
) {
    val scaleMode = listOf("scale", "resize")
    with(param) {
        val deviceType = DetectDeviceType()
        val fullWidth = deviceType != DeviceType.Tablet
        FlowRow(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(if (fullWidth)16.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(if (fullWidth)0.dp else 0.dp)
        ) {
            SwitchOptionItem(
                label = stringResource(R.string.param_enable_hires_fix),
                value = enableScale,
                fullWidth = fullWidth
            ) {
                onValueChange(param.copy(enableScale = it))
            }
            TextPickUpItem(
                label = stringResource(R.string.param_upscaler),
                value = hrUpscaler,
                fullWidth = fullWidth,
                options = DrawViewModel.upscalers.map { it.name }) {
                onValueChange(param.copy(hrUpscaler = it))
            }
            TextPickUpItem(
                label = stringResource(R.string.mode),
                value = hrMode,
                fullWidth = true,
                onGetDisplayValue = { index, value ->
                    ConstValues.HiresFixModeList[index]
                },
                options = ConstValues.HiresFixModeList
            ) {
                scaleMode.getOrNull(ConstValues.HiresFixModeList.indexOf(it))?.let {
                    onValueChange(param.copy(hrMode = it))
                }
            }
            if (hrMode == "scale") {
                SliderOptionItem(
                    label = stringResource(R.string.param_scale_by),
                    value = hrScale,
                    fullWidth = fullWidth,
                    valueRange = 1f..4f,
                    baseFloat = 0.05f,
                    onValueChangeFloat = {
                        onValueChange(param.copy(hrScale = it))
                    }
                )
            }
            if (hrMode == "resize") {
                SliderOptionItem(
                    label = stringResource(R.string.param_width),
                    value = hrResizeWidth.toFloat(),
                    valueRange = 0f..2048f,
                    baseFloat = 8f,
                    fullWidth = fullWidth,
                    onValueChangeFloat = {
                        onValueChange(param.copy(hrResizeWidth = it.toLong()))
                    }
                )
                SliderOptionItem(
                    label = stringResource(R.string.param_height),
                    value = hrResizeHeight.toFloat(),
                    valueRange = 0f..2048f,
                    baseFloat = 8f,
                    fullWidth = fullWidth,
                    onValueChangeFloat = {
                        onValueChange(param.copy(hrResizeHeight = it.toLong()))
                    }
                )
            }
            SliderOptionItem(label = stringResource(R.string.param_hires_steps),
                value = hrSteps.toFloat(),
                valueRange = 1f..150f,
                useInt = true,
                fullWidth = fullWidth,
                onValueChangeInt = {
                    onValueChange(param.copy(hrSteps = it.toLong()))
                }
            )
            SliderOptionItem(label = stringResource(R.string.param_denoising_strength),
                value = hrDenosingStrength, valueRange = 0f..1f,
                baseFloat = 0.01f,
                fullWidth = fullWidth,
                onValueChangeFloat = {
                    onValueChange(param.copy(hrDenosingStrength = it))
                }
            )
        }
    }

}