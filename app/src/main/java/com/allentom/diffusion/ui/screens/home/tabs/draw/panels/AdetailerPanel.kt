package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
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
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextAreaOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.ui.screens.home.tabs.draw.AdetailerParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.AdetailerSlot
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

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