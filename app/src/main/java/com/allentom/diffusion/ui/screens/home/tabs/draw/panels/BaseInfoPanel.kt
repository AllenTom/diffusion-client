package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.CanvasSizeItem
import com.allentom.diffusion.composables.DetectDeviceType
import com.allentom.diffusion.composables.DeviceType
import com.allentom.diffusion.composables.EmbeddingSelectOptionItem
import com.allentom.diffusion.composables.LoraSelectOptionItem
import com.allentom.diffusion.composables.ModelSelectOptionItem
import com.allentom.diffusion.composables.PromptSelectOptionItem
import com.allentom.diffusion.composables.SeedOptionItem
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BaseInfoPanel(
    onSwitchModel: (String) -> Unit,
    onSwitchVae: (String) -> Unit
) {
    val deviceType = DetectDeviceType()
    val fullWidth = deviceType != DeviceType.Tablet
    FlowRow(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(if (fullWidth)16.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(if (fullWidth)0.dp else 0.dp)
    ) {
        ModelSelectOptionItem(
            label = stringResource(R.string.param_model),
            value = DrawViewModel.useModelName.toString(),
            modelList = DrawViewModel.models
        ) {
            onSwitchModel(it.title)
        }
//        TextPickUpItem(
//            label = stringResource(R.string.param_model),
//            value = DrawViewModel.useModelName,
//            fullWidth = fullWidth,
//            options = DrawViewModel.models.map { it.title }) {
//
//        }
        PromptSelectOptionItem(
            label = stringResource(id = R.string.param_prompt),
            value = DrawViewModel.baseParam.promptText,
            regionPromptParam = DrawViewModel.regionPromptParam,
            templateParam = DrawViewModel.templateParam
        ) { prompts, region, template ->
            DrawViewModel.baseParam = DrawViewModel.baseParam.copy(promptText = prompts)
            region?.let {
                DrawViewModel.regionPromptParam = it
            }
            template?.let {
                DrawViewModel.templateParam = it
            }
        }
        PromptSelectOptionItem(
            label = stringResource(id = R.string.param_negative_prompt),
            value = DrawViewModel.baseParam.negativePromptText,
            templateParam = DrawViewModel.negativeTemplateParam
        ) { prompts, _, template ->
            DrawViewModel.baseParam = DrawViewModel.baseParam.copy(negativePromptText = prompts)
            template?.let {
                DrawViewModel.negativeTemplateParam = it
            }

        }
        LoraSelectOptionItem(
            label = stringResource(R.string.param_lora),
            value = DrawViewModel.baseParam.loraPrompt,
            loraList = DrawViewModel.loraList
        ) {
            DrawViewModel.baseParam = DrawViewModel.baseParam.copy(loraPrompt = it)
        }
        EmbeddingSelectOptionItem(
            label = stringResource(R.string.param_embedding),
            value = DrawViewModel.baseParam.embeddingPrompt,
            embeddingList = DrawViewModel.embeddingModels
        ) {
            DrawViewModel.baseParam = DrawViewModel.baseParam.copy(embeddingPrompt = it)
        }
        CanvasSizeItem(
            width = DrawViewModel.baseParam.width,
            height = DrawViewModel.baseParam.height,
            fullWidth = fullWidth,
            label = stringResource(
                R.string.size
            )
        ) { width, height ->
            DrawViewModel.baseParam = DrawViewModel.baseParam.copy(width = width, height = height)
        }
        SliderOptionItem(
            label = stringResource(R.string.param_width),
            value = DrawViewModel.baseParam.width.toFloat(),
            valueRange = 64f..2048f,
            steps = (2048 - 64) / 8,
            useInt = true,
            fullWidth = fullWidth,

            onValueChangeInt = {
                DrawViewModel.baseParam = DrawViewModel.baseParam.copy(width = it)
            }
        )
        SliderOptionItem(
            label = stringResource(R.string.param_height),
            value = DrawViewModel.baseParam.height.toFloat(),
            valueRange = 64f..2048f,
            steps = (2048 - 64) / 8,
            useInt = true,
            fullWidth = fullWidth,

            onValueChangeInt = {
                DrawViewModel.baseParam = DrawViewModel.baseParam.copy(height = it)
            }
        )
        TextPickUpItem(
            label = stringResource(R.string.param_sampler),
            value = DrawViewModel.baseParam.samplerName,
            fullWidth = fullWidth,

            options = DrawViewModel.samplerList.map { it.name }) {
            DrawViewModel.baseParam = DrawViewModel.baseParam.copy(samplerName = it)
        }
        SliderOptionItem(
            label = stringResource(R.string.param_steps),
            value = DrawViewModel.baseParam.steps.toFloat(),
            valueRange = 1f..100f,
            steps = 100 - 1,
            useInt = true,
            fullWidth = fullWidth,
            onValueChangeInt = {
                DrawViewModel.baseParam = DrawViewModel.baseParam.copy(steps = it)
            }
        )
        SeedOptionItem(
            label = stringResource(R.string.param_seed),
            value = DrawViewModel.baseParam.seed,
            fullWidth = fullWidth
        ) {
            DrawViewModel.baseParam = DrawViewModel.baseParam.copy(seed = it)
        }
        SliderOptionItem(
            label = stringResource(R.string.param_cfg_scale),
            value = DrawViewModel.baseParam.cfgScale,
            valueRange = 1f..30f,
            useInt = true,
            fullWidth = fullWidth,

            onValueChangeInt = {
                DrawViewModel.baseParam = DrawViewModel.baseParam.copy(cfgScale = it.toFloat())
            }
        )
        SliderOptionItem(
            label = stringResource(R.string.param_iter),
            value = DrawViewModel.baseParam.niter.toFloat(),
            valueRange = 1f..20f,
            steps = 20,
            useInt = true,
            fullWidth = fullWidth,
            onValueChangeInt = {
                DrawViewModel.baseParam = DrawViewModel.baseParam.copy(niter = it)
            }
        )
        TextPickUpItem(
            label = "Vae",
            value = DrawViewModel.useVae,
            fullWidth = fullWidth,
            options = DrawViewModel.vaeList.map { it.modelName } + listOf("None", "Automatic")) {
            onSwitchVae(it)
        }
        SwitchOptionItem(
            label = stringResource(R.string.refiner),
            fullWidth = true,
            value = DrawViewModel.baseParam.enableRefiner
        ) {
            DrawViewModel.baseParam = DrawViewModel.baseParam.copy(enableRefiner = it)
        }
        if (DrawViewModel.baseParam.enableRefiner) {
            TextPickUpItem(label = stringResource(id = R.string.refiner_model),
                value = DrawViewModel.baseParam.refinerModel,
                fullWidth = fullWidth,

                options = DrawViewModel.models.map { it.title }) {
                DrawViewModel.baseParam = DrawViewModel.baseParam.copy(refinerModel = it)
            }
            SliderOptionItem(label = stringResource(id = R.string.refiner_switch_at),
                value = DrawViewModel.baseParam.refinerSwitchAt,
                valueRange = 0f..1f,
                baseFloat = 0.01f,
                fullWidth = fullWidth,
                onValueChangeFloat = {
                    DrawViewModel.baseParam = DrawViewModel.baseParam.copy(refinerSwitchAt = it)
                }
            )
        }


    }
}