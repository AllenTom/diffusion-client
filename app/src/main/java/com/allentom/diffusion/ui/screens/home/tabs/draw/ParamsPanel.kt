package com.allentom.diffusion.ui.screens.home.tabs.draw

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.EmbeddingSelectOptionItem
import com.allentom.diffusion.composables.ImageBase64PickupOptionItem
import com.allentom.diffusion.composables.LoraSelectOptionItem
import com.allentom.diffusion.composables.PromptSelectOptionItem
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamsModalBottomSheet(
    onDismissRequest: () -> Unit,
    onSwitchModel: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = {
        onDismissRequest()
    }) {
        ParamsPanel(
            onSwitchModel = onSwitchModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamsPanel(
    onSwitchModel: (String) -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    Column {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            LazyRow {
                item {
                    FilterChip(onClick = {
                        tabIndex = 0
                    }, label = {
                        Text(stringResource(id = R.string.param_base))
                    }, selected = tabIndex == 0)
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(onClick = {
                        tabIndex = 1
                    }, label = {
                        Text(stringResource(id = R.string.param_hires_fix))
                    }, selected = tabIndex == 1)
                    if (DrawViewModel.enableControlNetFeat) {
                        Spacer(modifier = Modifier.width(16.dp))
                        FilterChip(onClick = {
                            tabIndex = 2
                        }, label = {
                            Text(stringResource(id = R.string.param_control_net))
                        }, selected = tabIndex == 2)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(onClick = {
                        tabIndex = 3
                    }, label = {
                        Text(stringResource(id = R.string.param_img2Img))
                    }, selected = tabIndex == 3)
                }
            }
        }
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        when (tabIndex) {
            0 -> {
                BaseInfoPanel(onSwitchModel)
            }

            1 -> {
                HiresFixPanel()
            }

            2 -> {
                ControlNetPanel()
            }

            3 -> {
                Img2ImgPanel()
            }
        }
    }
}

@Composable
fun BaseInfoPanel(
    onSwitchModel: (String) -> Unit
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
            value = DrawViewModel.inputPromptText
        ) {
            DrawViewModel.inputPromptText = it
        }

        PromptSelectOptionItem(
            label = stringResource(id = R.string.param_negative_prompt),
            value = DrawViewModel.inputNegativePromptText
        ) {
            DrawViewModel.inputNegativePromptText = it
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
    }
}

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
            onValueChangeFloat = {
                DrawViewModel.inputHrDenoisingStrength = it
            })
    }
}

@Composable
fun ControlNetPanel() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        SwitchOptionItem(
            label = stringResource(R.string.enable),
            value = DrawViewModel.inputControlNetEnable
        ) {
            DrawViewModel.inputControlNetEnable = it
        }
        SliderOptionItem(
            label = stringResource(R.string.param_guidance_start),
            value = DrawViewModel.inputControlNetGuidanceStart,
            valueRange = 0f..1f,
            onValueChangeFloat = {
                DrawViewModel.inputControlNetGuidanceStart = it
            })
        SliderOptionItem(
            label = stringResource(R.string.param_guidance_end),
            value = DrawViewModel.inputControlNetGuidanceEnd,
            valueRange = 0f..1f,
            onValueChangeFloat = {
                DrawViewModel.inputControlNetGuidanceEnd = it
            })
        TextPickUpItem(
            label = stringResource(R.string.param_control_mode),
            value = DrawViewModel.ControlNetModeList[DrawViewModel.inputControlNetControlMode],
            options = DrawViewModel.ControlNetModeList
        ) {
            DrawViewModel.inputControlNetControlMode =
                DrawViewModel.ControlNetModeList.indexOf(it)
        }
        SliderOptionItem(
            label = stringResource(R.string.param_control_weight),
            value = DrawViewModel.inputControlNetControlWeight,
            valueRange = 0f..2f,
            onValueChangeFloat = {
                DrawViewModel.inputControlNetControlWeight = it
            })
        TextPickUpItem(
            label = stringResource(R.string.param_control_model),
            value = DrawViewModel.inputControlNetControlModel,
            options = DrawViewModel.controlNetModelList
        ) {
            DrawViewModel.inputControlNetControlModel = it
        }
        ImageBase64PickupOptionItem(
            label = stringResource(R.string.param_control_image),
            value = DrawViewModel.inputContentNetImageBase64
        ) { uri, it, _, _, _ ->
            DrawViewModel.inputContentNetImageBase64 = it
            DrawViewModel.inputContentNetImagePath = uri.toString()
        }
    }
}
@Composable
fun Img2ImgPanel() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        SwitchOptionItem(
            label = stringResource(id = R.string.enable),
            value = DrawViewModel.generateMode == "img2img"
        ) {
            if (it) {
                DrawViewModel.generateMode = "img2img"
            } else {
                DrawViewModel.generateMode = "text2img"
            }
        }
        ImageBase64PickupOptionItem(
            label = stringResource(R.string.img2img_source_image),
            value = DrawViewModel.inputImg2ImgImgBase64,
            onValueChange = { _, it, filePath, width, height ->
                DrawViewModel.inputImg2ImgImgBase64 = it
                DrawViewModel.inputImg2ImgWidth = width.toFloat()
                DrawViewModel.inputImg2ImgHeight = height.toFloat()
                DrawViewModel.inputImg2ImgImgFilename = filePath
            }
        )
        SliderOptionItem(label = stringResource(R.string.param_denoising_strength),
            value = DrawViewModel.inputImg2ImgDenoisingStrength, valueRange = 0f..1f,
            onValueChangeFloat = {
                DrawViewModel.inputImg2ImgDenoisingStrength = it
            })
        SliderOptionItem(label = stringResource(R.string.param_scale_by),
            value = DrawViewModel.inputImg2ImgCfgScale, valueRange = 1f..30f,
            useInt = true,
            onValueChangeInt = {
                DrawViewModel.inputImg2ImgCfgScale = it.toFloat()
            })
        TextPickUpItem(
            label = stringResource(R.string.param_resize_mode),
            value = DrawViewModel.inputImg2ImgResizeModeList[DrawViewModel.inputImg2ImgResizeMode],
            options = DrawViewModel.inputImg2ImgResizeModeList
        )
        SliderOptionItem(label = stringResource(id = R.string.param_scale_by),
            value = DrawViewModel.inputImg2ImgScaleBy,
            valueRange = 0.05f..4f,
            onValueChangeFloat = {
                DrawViewModel.inputImg2ImgScaleBy = it
            })
        SliderOptionItem(label = stringResource(R.string.param_width),
            value = DrawViewModel.inputImg2ImgWidth,
            valueRange = 64f..2048f,
            steps = (2048 - 64) / 8,
            useInt = true,
            onValueChangeInt = {
                DrawViewModel.inputImg2ImgWidth = it.toFloat()
            })
        SliderOptionItem(label = stringResource(R.string.param_height),
            value = DrawViewModel.inputImg2ImgHeight,
            valueRange = 64f..2048f,
            steps = (2048 - 64) / 8,
            useInt = true,
            onValueChangeInt = {
                DrawViewModel.inputImg2ImgHeight = it.toFloat()
            })
    }
}