package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@Composable
fun HiresFixPanel() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        SwitchOptionItem(
            label = stringResource(R.string.param_enable_hires_fix),
            value = DrawViewModel.inputEnableHiresFix
        ) {
            DrawViewModel.inputEnableHiresFix = it
        }
        TextPickUpItem(
            label = stringResource(R.string.param_upscaler),
            value = DrawViewModel.inputUpscaler,
            options = DrawViewModel.upscalers.map { it.name }) {
            DrawViewModel.inputUpscaler = it
        }
        SliderOptionItem(
            label = stringResource(R.string.param_scale_by),
            value = DrawViewModel.inputHrScale,
            valueRange = 1f..4f,
            baseFloat = 0.05f,
            onValueChangeFloat = {
                DrawViewModel.inputHrScale = it
            })
        SliderOptionItem(label = stringResource(R.string.param_hires_steps),
            value = DrawViewModel.inputHrSteps, valueRange = 1f..150f,
            useInt = true,
            onValueChangeInt = {
                DrawViewModel.inputHrSteps = it.toFloat()
            })
        SliderOptionItem(label = stringResource(R.string.param_denoising_strength),
            value = DrawViewModel.inputHrDenoisingStrength, valueRange = 0f..1f,
            baseFloat = 0.01f,
            onValueChangeFloat = {
                DrawViewModel.inputHrDenoisingStrength = it
            })
    }
}