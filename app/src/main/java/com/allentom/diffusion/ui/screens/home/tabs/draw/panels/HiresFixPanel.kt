package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.store.history.SaveHrParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@Composable
fun HiresFixPanel(
    onValueChange: (SaveHrParam) -> Unit,
    param: SaveHrParam
) {
    val scaleMode = listOf("scale", "resize")
    with(param) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            SwitchOptionItem(
                label = stringResource(R.string.param_enable_hires_fix),
                value = enableScale
            ) {
                onValueChange(param.copy(enableScale = it))
            }
            TextPickUpItem(
                label = stringResource(R.string.param_upscaler),
                value = hrUpscaler,
                options = DrawViewModel.upscalers.map { it.name }) {
                onValueChange(param.copy(hrUpscaler = it))
            }
            TextPickUpItem(
                label = stringResource(R.string.mode),
                value = hrMode,
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
                    onValueChangeFloat = {
                        onValueChange(param.copy(hrResizeWidth = it.toLong()))
                    }
                )
                SliderOptionItem(
                    label = stringResource(R.string.param_height),
                    value = hrResizeHeight.toFloat(),
                    valueRange = 0f..2048f,
                    baseFloat = 8f,
                    onValueChangeFloat = {
                        onValueChange(param.copy(hrResizeHeight = it.toLong()))
                    }
                )
            }
            SliderOptionItem(label = stringResource(R.string.param_hires_steps),
                value = hrSteps.toFloat(),
                valueRange = 1f..150f,
                useInt = true,
                onValueChangeInt = {
                    onValueChange(param.copy(hrSteps = it.toLong()))
                }
            )
            SliderOptionItem(label = stringResource(R.string.param_denoising_strength),
                value = hrDenosingStrength, valueRange = 0f..1f,
                baseFloat = 0.01f,
                onValueChangeFloat = {
                    onValueChange(param.copy(hrDenosingStrength = it))
                }
            )
        }
    }

}