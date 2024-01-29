package com.allentom.diffusion.ui.screens.home.tabs.draw

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.composables.EmbeddingSelectOptionItem
import com.allentom.diffusion.composables.ImageBase64PickupOptionItem
import com.allentom.diffusion.composables.LoraSelectOptionItem
import com.allentom.diffusion.composables.MaskDrawOptionItem
import com.allentom.diffusion.composables.PromptSelectOptionItem
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextAreaOptionItem
import com.allentom.diffusion.composables.TextPickUpItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamsModalBottomSheet(
    onDismissRequest: () -> Unit,
    onSwitchModel: (String) -> Unit,
    onSwitchVae: (String) -> Unit,
    state: SheetState? = null
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = state ?: rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        ParamsPanel(
            onSwitchModel = onSwitchModel,
            onSwitchVae = onSwitchVae
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamsPanel(
    onSwitchModel: (String) -> Unit,
    onSwitchVae: (String) -> Unit
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
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(onClick = {
                        tabIndex = 4
                    }, label = {
                        Text(stringResource(R.string.reactor))
                    }, selected = tabIndex == 4)
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(onClick = {
                        tabIndex = 5
                    }, label = {
                        Text(stringResource(R.string.adetailer))
                    }, selected = tabIndex == 5)
                }
            }
        }
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        when (tabIndex) {
            0 -> {
                BaseInfoPanel(onSwitchModel, onSwitchVae)
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

            4 -> {
                ReactorPanel(
                    onValueChange = {
                        DrawViewModel.reactorParam = it
                    },
                    reactorParam = DrawViewModel.reactorParam,
                    showEnableOption = true
                )
            }

            5 -> {
                AdetailerPanel(
                    onValueChange = {
                        DrawViewModel.adetailerParam = it
                    },
                    adetailerParam = DrawViewModel.adetailerParam
                )
            }
        }
    }
}

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
            baseFloat = 0.01f,
            onValueChangeFloat = {
                DrawViewModel.inputControlNetGuidanceStart = it
            })
        SliderOptionItem(
            label = stringResource(R.string.param_guidance_end),
            value = DrawViewModel.inputControlNetGuidanceEnd,
            valueRange = 0f..1f,
            baseFloat = 0.01f,
            onValueChangeFloat = {
                DrawViewModel.inputControlNetGuidanceEnd = it
            })
        TextPickUpItem(
            label = stringResource(R.string.param_control_mode),
            value = ConstValues.ControlNetModeList[DrawViewModel.inputControlNetControlMode],
            options = ConstValues.ControlNetModeList
        ) {
            DrawViewModel.inputControlNetControlMode =
                ConstValues.ControlNetModeList.indexOf(it)
        }
        SliderOptionItem(
            label = stringResource(R.string.param_control_weight),
            value = DrawViewModel.inputControlNetControlWeight,
            valueRange = 0f..2f,
            baseFloat = 0.05f,
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
        if (DrawViewModel.inputImg2ImgImgBase64 != null) {
            SwitchOptionItem(
                label = stringResource(id = R.string.inpaint_dialog_title),
                value = DrawViewModel.inputImg2ImgInpaint
            ) {
                DrawViewModel.inputImg2ImgInpaint = it
            }
        }
        if (DrawViewModel.inputImg2ImgImgBase64 != null && DrawViewModel.inputImg2ImgInpaint) {
            MaskDrawOptionItem(
                label = stringResource(R.string.inpaint_mask),
                value = DrawViewModel.inputImg2ImgMaskPreview,
                backgroundImageBase64 = DrawViewModel.inputImg2ImgImgBase64
            ) {
                DrawViewModel.inputImg2ImgMask = it
                DrawViewModel.inputImg2ImgMaskPreview = Util.combineBase64Images(
                    DrawViewModel.inputImg2ImgImgBase64!!,
                    it
                )
            }
            SliderOptionItem(label = stringResource(id = R.string.mask_blur),
                value = DrawViewModel.inputImg2ImgMaskBlur,
                valueRange = 0f..64f,
                useInt = true,
                onValueChangeInt = {
                    DrawViewModel.inputImg2ImgMaskBlur = it.toFloat()
                }

            )
            TextPickUpItem(
                label = stringResource(id = R.string.mask_mode),
                value = ConstValues.MaskInvertOptions[DrawViewModel.inputImg2ImgInpaintingMaskInvert],
                options = ConstValues.MaskInvertOptions
            ) {
                DrawViewModel.inputImg2ImgInpaintingMaskInvert =
                    ConstValues.MaskInvertOptions.indexOf(it)
            }
            TextPickUpItem(
                label = stringResource(id = R.string.masked_content),
                value = ConstValues.InpaintingFillOptions[DrawViewModel.inputImg2ImgInpaintingFill],
                options = ConstValues.InpaintingFillOptions
            ) {
                DrawViewModel.inputImg2ImgInpaintingFill =
                    ConstValues.InpaintingFillOptions.indexOf(it)
            }
            TextPickUpItem(
                label = stringResource(id = R.string.inpaint_area),
                value = ConstValues.InpaintingFullResOptions[DrawViewModel.inputImg2ImgInpaintingFullRes],
                options = ConstValues.InpaintingFullResOptions
            ) {
                DrawViewModel.inputImg2ImgInpaintingFullRes =
                    ConstValues.InpaintingFullResOptions.indexOf(it)
            }
            SliderOptionItem(label = stringResource(id = R.string.only_masked_padding_pixels),
                value = DrawViewModel.inputImg2ImgInpaintingFullResPadding.toFloat(),
                valueRange = 0f..256f,
                useInt = true,
                onValueChangeInt = {
                    DrawViewModel.inputImg2ImgInpaintingFullResPadding = it
                }
            )

        }


        SliderOptionItem(label = stringResource(R.string.param_denoising_strength),
            value = DrawViewModel.inputImg2ImgDenoisingStrength, valueRange = 0f..1f,
            baseFloat = 0.01f,
            onValueChangeFloat = {
                DrawViewModel.inputImg2ImgDenoisingStrength = it
            })
        SliderOptionItem(label = stringResource(R.string.param_cfg_scale),
            value = DrawViewModel.inputImg2ImgCfgScale, valueRange = 1f..30f,
            useInt = true,
            baseFloat = 0.5f,
            onValueChangeInt = {
                DrawViewModel.inputImg2ImgCfgScale = it.toFloat()
            })
        TextPickUpItem(
            label = stringResource(R.string.param_resize_mode),
            value = ConstValues.Img2ImgResizeModeList[DrawViewModel.inputImg2ImgResizeMode],
            options = ConstValues.Img2ImgResizeModeList
        )
        SliderOptionItem(label = stringResource(id = R.string.param_scale_by),
            value = DrawViewModel.inputImg2ImgScaleBy,
            valueRange = 0.05f..4f,
            baseFloat = 0.05f,
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

@Composable
fun ReactorPanel(
    onValueChange: (ReactorParam) -> Unit = {},
    showEnableOption: Boolean = true,
    reactorParam: ReactorParam
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        if (showEnableOption) {
            SwitchOptionItem(
                label = stringResource(R.string.enable),
                value = reactorParam.enabled
            ) {
                onValueChange(reactorParam.copy(enabled = it))
            }
        }

        ImageBase64PickupOptionItem(
            label = stringResource(R.string.reactor_single_source_image),
            value = reactorParam.singleImageResult
        ) { fileUri, imgBase64, filename, width, height ->
            onValueChange(
                reactorParam.copy(
                    singleImageResult = imgBase64,
                    singleImageResultFilename = filename,
                )
            )
        }
        TextPickUpItem(
            label = stringResource(R.string.reactor_gender_detection_source),
            value = ConstValues.ReactorGenderDetectionOptions[reactorParam.genderDetectionSource],
            options = ConstValues.ReactorGenderDetectionOptions
        ) {
            onValueChange(
                reactorParam.copy(
                    genderDetectionSource = ConstValues.ReactorGenderDetectionOptions.indexOf(it)
                )
            )
        }
        TextPickUpItem(
            label = stringResource(R.string.reactor_gender_detection_target),
            value = ConstValues.ReactorGenderDetectionOptions[reactorParam.genderDetectionTarget],
            options = ConstValues.ReactorGenderDetectionOptions
        ) {
            onValueChange(
                reactorParam.copy(
                    genderDetectionTarget = ConstValues.ReactorGenderDetectionOptions.indexOf(it)
                )
            )
        }
        TextPickUpItem(
            label = stringResource(R.string.reactor_restore_face),
            value = reactorParam.restoreFace,
            options = ConstValues.ReactorRestoreFaceOption
        ) {
            onValueChange(
                reactorParam.copy(
                    restoreFace = it
                )
            )
        }
        SliderOptionItem(label = stringResource(R.string.reactor_restore_face_visibility),
            value = reactorParam.restoreFaceVisibility,
            valueRange = 0f..1f,
            baseFloat = 0.1f,
            onValueChangeFloat = {
                onValueChange(
                    reactorParam.copy(
                        restoreFaceVisibility = it
                    )
                )
            }
        )
        SliderOptionItem(label = stringResource(R.string.reactor_codeformer_weight_fidelity),
            value = reactorParam.codeFormerWeightFidelity,
            valueRange = 0f..1f,
            baseFloat = 0.1f,
            onValueChangeFloat = {
                onValueChange(
                    reactorParam.copy(
                        codeFormerWeightFidelity = it
                    )
                )
            }
        )
        SwitchOptionItem(
            label = stringResource(R.string.reactor_postprocessing_order),
            value = reactorParam.postprocessingOrder
        ) {
            onValueChange(
                reactorParam.copy(
                    postprocessingOrder = it
                )
            )
        }
        TextPickUpItem(
            label = stringResource(R.string.reactor_upscaler),
            value = reactorParam.upscaler,
            options = DrawViewModel.reactorUpscalerList
        ) {
            onValueChange(
                reactorParam.copy(
                    upscaler = it
                )
            )
        }
        SliderOptionItem(label = stringResource(R.string.reactor_scale_by),
            value = reactorParam.scaleBy,
            valueRange = 1f..8f,
            baseFloat = 0.1f,
            onValueChangeFloat = {
                onValueChange(
                    reactorParam.copy(
                        scaleBy = it
                    )
                )
            }
        )
        SliderOptionItem(label = stringResource(R.string.reactor_upscaler_visibility_if_scale_1),
            value = reactorParam.upscalerVisibility,
            valueRange = 0f..1f,
            baseFloat = 0.1f,
            onValueChangeFloat = {
                onValueChange(
                    reactorParam.copy(
                        upscalerVisibility = it
                    )
                )
            }
        )
        TextPickUpItem(
            label = stringResource(R.string.model),
            value = reactorParam.model,
            options = DrawViewModel.reactorModelList
        ) {
            onValueChange(
                reactorParam.copy(
                    model = it
                )
            )
        }

    }
}

//{
//    "ad_model": "face_yolov8n.pt",
//    "ad_prompt": "",
//    "ad_negative_prompt": "",
//    "ad_confidence": 0.3,
//    "ad_mask_k_largest": 0,
//    "ad_mask_min_ratio": 0.0,
//    "ad_mask_max_ratio": 1.0,
//    "ad_dilate_erode": 32,
//    "ad_x_offset": 0,
//    "ad_y_offset": 0,
//    "ad_mask_merge_invert": "None",
//    "ad_mask_blur": 4,
//    "ad_denoising_strength": 0.4,
//    "ad_inpaint_only_masked": true,
//    "ad_inpaint_only_masked_padding": 0,
//    "ad_use_inpaint_width_height": false,
//    "ad_inpaint_width": 512,
//    "ad_inpaint_height": 512,
//    "ad_use_steps": true,
//    "ad_steps": 28,
//    "ad_use_cfg_scale": false,
//    "ad_cfg_scale": 7.0,
//    "ad_use_checkpoint": false,
//    "ad_checkpoint": "Use same checkpoint",
//    "ad_use_vae": false,
//    "ad_vae": "Use same VAE",
//    "ad_use_sampler": false,
//    "ad_sampler": "DPM++ 2M Karras",
//    "ad_use_noise_multiplier": false,
//    "ad_noise_multiplier": 1.0,
//    "ad_use_clip_skip": false,
//    "ad_clip_skip": 1,
//    "ad_restore_face": false,
//    "ad_controlnet_model": "None",
//    "ad_controlnet_module": "None",
//    "ad_controlnet_weight": 1.0,
//    "ad_controlnet_guidance_start": 0.0,
//    "ad_controlnet_guidance_end": 1.0
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable()
fun AdetailerPanel(
    onValueChange: (AdetailerParam) -> Unit = {},
    adetailerParam: AdetailerParam
) {
    var selectedSlot by remember { mutableIntStateOf(0) }
    var isEditMode by remember { mutableStateOf(false) }
    fun onIndexUpdate(slot: AdetailerSlot) {
        onValueChange(
            adetailerParam.copy(
                slots = adetailerParam.slots.mapIndexed { index, adetailerSlot ->
                    if (index == selectedSlot) {
                        slot
                    } else {
                        adetailerSlot
                    }
                }
            )
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SwitchOptionItem(
            label = stringResource(id = R.string.enable),
            value = adetailerParam.enabled
        ) {
            onValueChange(
                adetailerParam.copy(
                    enabled = it
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                for (i in 0..<adetailerParam.slots.size) {
                    FilterChip(
                        selected = i == selectedSlot,
                        onClick = {
                            selectedSlot = i
                        },
                        label = {
                            Text(text = stringResource(id = R.string.slot, i + 1))
                        },
                        trailingIcon = {
                            if (isEditMode && adetailerParam.slots.size > 1) {
                                IconButton(onClick = {
                                    onValueChange(
                                        adetailerParam.copy(
                                            slots = adetailerParam.slots.filterIndexed { index, _ ->
                                                index != i
                                            }
                                        )
                                    )
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (isEditMode && adetailerParam.slots.size < 2) {
                    IconButton(onClick = {
                        onValueChange(
                            adetailerParam.copy(
                                slots = adetailerParam.slots + AdetailerSlot()
                            )
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = {
                isEditMode = !isEditMode
            }) {
                Icon(Icons.Default.Edit, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            val currentParam = adetailerParam.slots.getOrNull(selectedSlot)
            currentParam?.let {
                Spacer(modifier = Modifier.height(16.dp))
                with(it) {
                    TextPickUpItem(
                        label = stringResource(id = R.string.model),
                        value = adModel,
                        options = DrawViewModel.adetailerModelList
                    ) {
                        onIndexUpdate(copy(adModel = it))
                    }
                    TextAreaOptionItem(
                        label = stringResource(id = R.string.param_prompt),
                        value = adPrompt
                    ) {
                        onIndexUpdate(copy(adPrompt = it))
                    }
                    TextAreaOptionItem(
                        label = stringResource(id = R.string.param_negative_prompt),
                        value = adNegativePrompt
                    ) {
                        onIndexUpdate(copy(adNegativePrompt = it))
                    }
                    Divider()
                    SliderOptionItem(
                        label = stringResource(R.string.detection_model_confidence_threshold),
                        value = adConfidence,
                        valueRange = 0f..1f,
                        baseFloat = 0.01f,
                        onValueChangeFloat = {
                            onIndexUpdate(
                                copy(
                                    adConfidence = it
                                )
                            )
                        }
                    )
                    SliderOptionItem(
                        label = stringResource(R.string.mask_only_the_top_k_largest_0_to_disable),
                        value = adMaskKLargest.toFloat(),
                        valueRange = 0f..10f,
                        onValueChangeInt = {
                            onIndexUpdate(
                                copy(
                                    adMaskKLargest = it.toLong()
                                )
                            )
                        }
                    )
                    SliderOptionItem(
                        label = stringResource(R.string.mask_min_area_ratio),
                        value = adMaskMinRatio,
                        valueRange = 0f..1f,
                        baseFloat = 0.001f,
                        onValueChangeFloat = {
                            onIndexUpdate(
                                copy(
                                    adMaskMinRatio = it
                                )
                            )
                        }
                    )
                    SliderOptionItem(
                        label = stringResource(R.string.mask_max_area_ratio),
                        value = adMaskMaxRatio,
                        valueRange = 0f..1f,
                        baseFloat = 0.001f,
                        onValueChangeFloat = {
                            onIndexUpdate(
                                copy(
                                    adMaskMaxRatio = it
                                )
                            )
                        }
                    )
                    Divider()
                    SliderOptionItem(
                        label = stringResource(R.string.mask_x_offset),
                        value = adXOffset.toFloat(),
                        valueRange = -200f..200f,
                        useInt = true,
                        onValueChangeInt = {
                            onIndexUpdate(
                                copy(
                                    adXOffset = it.toLong()
                                )
                            )
                        }
                    )
                    SliderOptionItem(
                        label = stringResource(R.string.mask_y_offset),
                        value = adYOffset.toFloat(),
                        valueRange = -200f..200f,
                        useInt = true,
                        onValueChangeInt = {
                            onIndexUpdate(
                                copy(
                                    adYOffset = it.toLong()
                                )
                            )
                        }
                    )
                    SliderOptionItem(label = stringResource(R.string.mask_erosion_dilation),
                        value = adDilateErode.toFloat(),
                        valueRange = -128f..128f,
                        useInt = true,
                        onValueChangeInt = {
                            onIndexUpdate(
                                copy(
                                    adDilateErode = it.toLong()
                                )
                            )
                        }
                    )
                    TextPickUpItem(
                        label = stringResource(R.string.mask_merge_mode),
                        value = adMaskMergeInvert,
                        options = ConstValues.MaskInvertOptions
                    ) {
                        onIndexUpdate(
                            copy(
                                adMaskMergeInvert = it
                            )
                        )
                    }
                    Divider()
                    SliderOptionItem(label = stringResource(R.string.inpaint_mask_blur),
                        value = adMaskBlur.toFloat(),
                        valueRange = 0f..64f,
                        useInt = true,
                        onValueChangeInt = {
                            onIndexUpdate(
                                copy(
                                    adMaskBlur = it.toLong()
                                )
                            )
                        }
                    )
                    SliderOptionItem(label = stringResource(R.string.inpaint_denoising_strength),
                        value = adDenoisingStrength,
                        valueRange = 0f..1f,
                        baseFloat = 0.01f,
                        onValueChangeFloat = {
                            onIndexUpdate(
                                copy(
                                    adDenoisingStrength = it
                                )
                            )
                        }
                    )
                    SwitchOptionItem(
                        label = stringResource(R.string.inpaint_only_masked),
                        value = adInpaintOnlyMasked
                    ) {
                        onIndexUpdate(
                            copy(
                                adInpaintOnlyMasked = it
                            )
                        )
                    }
                    SliderOptionItem(label = stringResource(R.string.inpaint_only_masked_padding_pixels),
                        value = adInpaintOnlyMaskedPadding.toFloat(),
                        valueRange = 0f..256f,
                        useInt = true,
                        onValueChangeInt = {
                            onIndexUpdate(
                                copy(
                                    adInpaintOnlyMaskedPadding = it.toLong()
                                )
                            )
                        }
                    )
                    SwitchOptionItem(
                        label = stringResource(R.string.use_separate_width_height),
                        value = adUseInpaintWidthHeight
                    ) {
                        onIndexUpdate(
                            copy(
                                adUseInpaintWidthHeight = it
                            )
                        )
                    }
                    if (adUseInpaintWidthHeight) {
                        SliderOptionItem(label = stringResource(R.string.inpaint_width),
                            value = adInpaintWidth.toFloat(),
                            valueRange = 64f..2048f,
                            steps = (2048 - 64) / 8,
                            useInt = true,
                            onValueChangeInt = {
                                onIndexUpdate(
                                    copy(
                                        adInpaintWidth = it.toLong()
                                    )
                                )
                            }
                        )

                        SliderOptionItem(label = stringResource(R.string.inpaint_height),
                            value = adInpaintHeight.toFloat(),
                            valueRange = 64f..2048f,
                            steps = (2048 - 64) / 8,
                            useInt = true,
                            onValueChangeInt = {
                                onIndexUpdate(
                                    copy(
                                        adInpaintHeight = it.toLong()
                                    )
                                )
                            }
                        )
                    }
                    SwitchOptionItem(
                        label = stringResource(R.string.use_steps),
                        value = adUseSteps
                    ) {
                        onIndexUpdate(
                            copy(
                                adUseSteps = it
                            )
                        )
                    }
                    if (adUseSteps) {
                        SliderOptionItem(label = stringResource(id = R.string.param_steps),
                            value = adSteps.toFloat(),
                            valueRange = 1f..150f,
                            useInt = true,
                            onValueChangeInt = {
                                onIndexUpdate(
                                    copy(
                                        adSteps = it.toLong()
                                    )
                                )
                            }
                        )
                    }
                    SwitchOptionItem(
                        label = stringResource(R.string.use_cfg_scale),
                        value = adUseCfgScale
                    ) {
                        onIndexUpdate(
                            copy(
                                adUseCfgScale = it
                            )
                        )
                    }
                    if (adUseCfgScale) {
                        SliderOptionItem(
                            label = stringResource(id = R.string.param_cfg_scale),
                            value = adCfgScale,
                            valueRange = 1f..30f,
                            baseFloat = 0.5f,
                            onValueChangeFloat = {
                                onIndexUpdate(
                                    copy(
                                        adCfgScale = it
                                    )
                                )
                            },
                        )
                    }
                    SwitchOptionItem(
                        label = stringResource(R.string.use_checkpoint),
                        value = adUseCheckpoint
                    ) {
                        onIndexUpdate(
                            copy(
                                adUseCheckpoint = it
                            )
                        )
                    }
                    if (adUseCheckpoint) {
                        TextPickUpItem(label = stringResource(id = R.string.model),
                            value = adCheckpoint,
                            options = DrawViewModel.models.map { it.title }
                        ) {
                            onIndexUpdate(
                                copy(
                                    adCheckpoint = it
                                )
                            )
                        }
                    }
                    SwitchOptionItem(
                        label = stringResource(R.string.use_vae),
                        value = adUseVae
                    ) {
                        onIndexUpdate(
                            copy(
                                adUseVae = it
                            )
                        )
                    }
                    if (adUseVae) {
                        TextPickUpItem(label = stringResource(R.string.vae),
                            value = adVae,
                            options = DrawViewModel.vaeList.map { it.modelName } + listOf(
                                "None",
                                "Automatic"
                            )
                        ) {
                            onIndexUpdate(
                                copy(
                                    adVae = it
                                )
                            )
                        }
                    }

                    SwitchOptionItem(
                        label = stringResource(R.string.use_sampler),
                        value = adUseSampler
                    ) {
                        onIndexUpdate(
                            copy(
                                adUseSampler = it
                            )
                        )
                    }
                    if (adUseSampler) {
                        TextPickUpItem(label = stringResource(id = R.string.param_sampler),
                            value = adSampler,
                            options = DrawViewModel.samplerList.map { it.name }
                        ) {
                            onIndexUpdate(
                                copy(
                                    adSampler = it
                                )
                            )
                        }
                    }
                    SwitchOptionItem(
                        label = stringResource(R.string.use_noise_multiplier),
                        value = adUseNoiseMultiplier
                    ) {
                        onIndexUpdate(
                            copy(
                                adUseNoiseMultiplier = it
                            )
                        )
                    }
                    if (adUseNoiseMultiplier) {
                        SliderOptionItem(label = stringResource(R.string.noise_multiplier),
                            value = adNoiseMultiplier,
                            valueRange = 0.5f..1.5f,
                            baseFloat = 0.01f,
                            onValueChangeFloat = {
                                onIndexUpdate(
                                    copy(
                                        adNoiseMultiplier = it
                                    )
                                )
                            }
                        )
                    }
                    SwitchOptionItem(
                        label = stringResource(R.string.use_clip_skip),
                        value = adUseClipSkip
                    ) {
                        onIndexUpdate(
                            copy(
                                adUseClipSkip = it
                            )
                        )
                    }
                    if (adUseClipSkip) {
                        SliderOptionItem(label = stringResource(R.string.clip_skip),
                            value = adClipSkip.toFloat(),
                            valueRange = 1f..12f,
                            useInt = true,
                            onValueChangeInt = {
                                onIndexUpdate(
                                    copy(
                                        adClipSkip = it.toLong()
                                    )
                                )
                            }
                        )
                    }
                    SwitchOptionItem(
                        label = stringResource(R.string.restore_face),
                        value = adRestoreFace
                    ) {
                        onIndexUpdate(
                            copy(
                                adRestoreFace = it
                            )
                        )
                    }
                    Divider()
                    TextPickUpItem(
                        label = stringResource(R.string.controlnet_model),
                        value = adControlnetModel,
                        options = DrawViewModel.controlNetModelList
                    ) {
                        onIndexUpdate(
                            copy(
                                adControlnetModel = it
                            )
                        )
                    }
                    SliderOptionItem(label = stringResource(R.string.controlnet_weight),
                        value = adControlnetWeight,
                        valueRange = 0f..1f,
                        baseFloat = 0.01f,
                        onValueChangeFloat = {
                            onIndexUpdate(
                                copy(
                                    adControlnetWeight = it
                                )
                            )
                        }
                    )
                    SliderOptionItem(label = stringResource(R.string.controlnet_guidance_start),
                        value = adControlnetGuidanceStart,
                        valueRange = 0f..1f,
                        baseFloat = 0.01f,
                        onValueChangeFloat = {
                            onIndexUpdate(
                                copy(
                                    adControlnetGuidanceStart = it
                                )
                            )
                        }
                    )
                    SliderOptionItem(label = stringResource(R.string.controlnet_guidance_end),
                        value = adControlnetGuidanceEnd,
                        valueRange = 0f..1f,
                        baseFloat = 0.01f,
                        onValueChangeFloat = {
                            onIndexUpdate(
                                copy(
                                    adControlnetGuidanceEnd = it
                                )
                            )
                        }
                    )
                }
            }
        }
    }

}