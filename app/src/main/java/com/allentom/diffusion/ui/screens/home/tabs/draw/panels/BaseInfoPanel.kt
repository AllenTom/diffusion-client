package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.EmbeddingSelectOptionItem
import com.allentom.diffusion.composables.LoraSelectOptionItem
import com.allentom.diffusion.composables.PromptSelectOptionItem
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@Composable
fun BaseInfoPanel(
    onSwitchModel: (String) -> Unit,
    onSwitchVae: (String) -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        TextPickUpItem(
            label = stringResource(R.string.param_model),
            value = DrawViewModel.useModelName,
            options = DrawViewModel.models.map { it.title }) {
            onSwitchModel(it)
        }
        PromptSelectOptionItem(
            label = stringResource(id = R.string.param_prompt),
            value = DrawViewModel.inputPromptText,
            regionPromptParam = DrawViewModel.regionPromptParam
        ) { prompts, region ->
            DrawViewModel.inputPromptText = prompts
            region?.let {
                DrawViewModel.regionPromptParam = it
            }
        }

        PromptSelectOptionItem(
            label = stringResource(id = R.string.param_negative_prompt),
            value = DrawViewModel.inputNegativePromptText
        ) { prompts, _ ->
            DrawViewModel.inputNegativePromptText = prompts
        }
        LoraSelectOptionItem(
            label = stringResource(R.string.param_lora),
            value = DrawViewModel.inputLoraList,
            loraList = DrawViewModel.loraList
        ) {
            DrawViewModel.inputLoraList = it
        }
        EmbeddingSelectOptionItem(
            label = stringResource(R.string.param_embedding),
            value = DrawViewModel.embeddingList,
            embeddingList = DrawViewModel.embeddingModels
        ) {
            DrawViewModel.embeddingList = it
        }
        SliderOptionItem(
            label = stringResource(R.string.param_width),
            value = DrawViewModel.inputWidth,
            valueRange = 64f..2048f,
            steps = (2048 - 64) / 8,
            useInt = true,
            onValueChangeInt = { DrawViewModel.inputWidth = it.toFloat() }
        )
        SliderOptionItem(
            label = stringResource(R.string.param_height),
            value = DrawViewModel.inputHeight,
            valueRange = 64f..2048f,
            steps = (2048 - 64) / 8,
            useInt = true,
            onValueChangeInt = { DrawViewModel.inputHeight = it.toFloat() }
        )
        TextPickUpItem(
            label = stringResource(R.string.param_sampler),
            value = DrawViewModel.inputSamplerName,
            options = DrawViewModel.samplerList.map { it.name }) {
            DrawViewModel.inputSamplerName = it
        }
        SliderOptionItem(
            label = stringResource(R.string.param_steps),
            value = DrawViewModel.inputSteps,
            valueRange = 1f..100f,
            steps = 100 - 1,
            useInt = true,
            onValueChangeInt = { DrawViewModel.inputSteps = it.toFloat() }
        )
        SliderOptionItem(
            label = stringResource(R.string.param_seed),
            value = DrawViewModel.inputSeed.toFloat(),
            valueRange = -1f..1.0E8f,
            useInt = true,
            onValueChangeInt = { DrawViewModel.inputSeed = it }
        )
        SliderOptionItem(
            label = stringResource(R.string.param_cfg_scale),
            value = DrawViewModel.inputCfgScale,
            valueRange = 1f..30f,
            useInt = true,
            onValueChangeInt = { DrawViewModel.inputCfgScale = it.toFloat() }
        )
        SliderOptionItem(
            label = stringResource(R.string.param_iter),
            value = DrawViewModel.inputNiter,
            valueRange = 1f..20f,
            steps = 20,
            useInt = true,
            onValueChangeInt = { DrawViewModel.inputNiter = it.toFloat() }
        )
        TextPickUpItem(
            label = "Vae",
            value = DrawViewModel.useVae,
            options = DrawViewModel.vaeList.map { it.modelName } + listOf("None", "Automatic")) {
            onSwitchVae(it)
        }
        SwitchOptionItem(
            label = stringResource(R.string.refiner),
            value = DrawViewModel.enableRefiner
        ) {
            DrawViewModel.enableRefiner = it
        }
        if (DrawViewModel.enableRefiner) {
            TextPickUpItem(label = stringResource(id = R.string.refiner_model),
                value = DrawViewModel.refinerModel,
                options = DrawViewModel.models.map { it.title }) {
                DrawViewModel.refinerModel = it
            }
            SliderOptionItem(label = stringResource(id = R.string.refiner_switch_at),
                value = DrawViewModel.refinerSwitchAt,
                valueRange = 0f..1f,
                baseFloat = 0.01f,
                onValueChangeFloat = { DrawViewModel.refinerSwitchAt = it }
            )
        }


    }
}