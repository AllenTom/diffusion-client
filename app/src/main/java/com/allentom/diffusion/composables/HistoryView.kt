package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.Util
import com.allentom.diffusion.store.history.SaveHistory
import com.allentom.diffusion.ui.screens.historydetail.ParamItem
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image
import com.allentom.diffusion.ui.screens.model.detail.ModelDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryView(
    currentHistory: SaveHistory,
    navController: NavController
) {
    var isImage2ImageInputPreviewOpen by remember {
        mutableStateOf(false)
    }
    var maskPreview by remember {
        mutableStateOf(null as String?)
    }
    val scope = rememberCoroutineScope()

    var promptActionState = rememberPromptActionState()
    var adetailerCurrentSlot by remember {
        mutableStateOf(0)
    }
    var controlCurrentSlotIndex by remember {
        mutableStateOf(0)
    }

    if (isImage2ImageInputPreviewOpen) {
        currentHistory.img2imgParam?.let {
            ImageUriPreviewDialog(
                imageUri = it.path,
                onDismissRequest = {
                    isImage2ImageInputPreviewOpen = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        val sourceImagePath = currentHistory.img2imgParam?.path
        val sourceMaskPath = currentHistory.img2imgParam?.maskPath

        if (sourceImagePath != null && sourceMaskPath != null) {
            scope.launch(Dispatchers.IO) {
                val preview = Util.combineImagePaths(sourceImagePath, sourceMaskPath)
                maskPreview = preview
            }
        }
    }

    PromptAction(promptActionState)

    Column {
        if (currentHistory.regionEnable == true) {
            Text(
                text = stringResource(id = R.string.regional),
                fontWeight = FontWeight.W500,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ParamItem(label = stringResource(R.string.region_divider_ratio),
                    value = { Text(text = currentHistory.regionRatio.toString()) })
            }
            var regionCount = currentHistory.regionCount ?: 0
            if (currentHistory.regionUseCommon == true) {
                regionCount += 1
            }
            for (i in 0 until regionCount) {
                PromptDisplayView(
                    promptList = currentHistory.prompt.filter { it.regionIndex == i },
                    titleComponent = {
                        if (currentHistory.regionUseCommon == true && i == 0) {
                            SectionTitle(title = stringResource(id = R.string.common_region))
                        } else {
                            SectionTitle(title = stringResource(id = R.string.region, i.toString()))
                        }
                    },
                    onClickPrompt = {
                        navController.navigate(
                            Screens.PromptDetail.route.replace(
                                "{promptId}",
                                it.promptId.toString()
                            )
                        )
                    },
                    canScroll = false
                ) {
                    promptActionState.onOpenActionBottomSheet(it, "prompt")
                }
            }
        } else {
            currentHistory.prompt.takeIf { it.isNotEmpty() }?.let {
                PromptDisplayView(
                    promptList = currentHistory.prompt,
                    titleComponent = {
                        SectionTitle(title = stringResource(R.string.param_prompt))
                    },
                    onClickPrompt = {
                        navController.navigate(
                            Screens.PromptDetail.route.replace(
                                "{promptId}",
                                it.promptId.toString()
                            )
                        )
                    },
                    canScroll = false
                ) {
                    promptActionState.onOpenActionBottomSheet(it, "prompt")
                }
            }
        }
        currentHistory.negativePrompt.takeIf { it.isNotEmpty() }?.let {
            PromptDisplayView(
                promptList = currentHistory.negativePrompt,
                titleComponent = {
                    SectionTitle(title = stringResource(R.string.param_negative_prompt))
                },
                onClickPrompt = {
                    navController.navigate(
                        Screens.PromptDetail.route.replace(
                            "{promptId}",
                            it.promptId.toString()
                        )
                    )
                },
                canScroll = false
            ) {
                promptActionState.onOpenActionBottomSheet(it, "negativePrompt")
            }
        }
        currentHistory.loraPrompt.takeIf { it.isNotEmpty() }?.let {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = stringResource(R.string.param_lora))
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Column {
                    currentHistory.loraPrompt.forEach {
                        ListItem(headlineContent = {
                            Row {
                                Text(text = String.format("%.1f", it.weight))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = it.name)
                            }
                        }, supportingContent = {
                            PromptFlowRow(promptList = it.prompts)
                        })
                    }
                }
            }
        }
        currentHistory.embeddingPrompt.takeIf { it.isNotEmpty() }?.let {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = stringResource(R.string.embedding))
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                currentHistory.embeddingPrompt.forEach {
                    AssistChip(
                        onClick = { },
                        label = { Text(text = it.text) }
                    )
                }
            }
        }
        currentHistory.model?.let { modelEntity ->
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = stringResource(R.string.model))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        ModelDetailViewModel.asNew()
                        navController.navigate(
                            Screens.ModelDetailScreen.route.replace(
                                "{modelId}",
                                modelEntity.modelId.toString()
                            )
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    modelEntity.title?.let {
                        Text(text = it)
                    }
                    modelEntity.name.let {
                        Text(text = it)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .width(120.dp)
                ) {
                    modelEntity.coverPath?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        SectionTitle(title = stringResource(R.string.param))
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ParamItem(
                    label = stringResource(id = R.string.param_width),
                    value = { Text(text = currentHistory.width.toString()) }
                )
                ParamItem(
                    label = stringResource(id = R.string.param_height),
                    value = { Text(text = currentHistory.height.toString()) })
                ParamItem(label = stringResource(id = R.string.param_steps),
                    value = { Text(text = currentHistory.steps.toString()) })
                ParamItem(label = stringResource(id = R.string.param_sampler),
                    value = { Text(text = currentHistory.samplerName) })
                ParamItem(label = stringResource(id = R.string.param_cfg_scale),
                    value = { Text(text = currentHistory.cfgScale.toString()) })
                ParamItem(label = "Vae", value = {
                    Text(text = currentHistory.vaeName.toString())
                })
                if (currentHistory.enableRefiner) {
                    ParamItem(label = stringResource(R.string.refiner_model),
                        value = { Text(text = currentHistory.refinerModelName.toString()) }
                    )
                    ParamItem(label = stringResource(R.string.refiner_switch_at),
                        value = { Text(text = currentHistory.refinerSwitchAt.toString()) }
                    )
                }

            }
        }
        currentHistory.img2imgParam?.let { img2imgParam ->
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = stringResource(R.string.image_to_image))
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = img2imgParam.path,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                isImage2ImageInputPreviewOpen = true
                            }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ParamItem(label = stringResource(id = R.string.param_width),
                        value = { Text(text = img2imgParam.width.toString()) })
                    ParamItem(label = stringResource(id = R.string.param_height),
                        value = { Text(text = img2imgParam.height.toString()) })
                    ParamItem(label = stringResource(id = R.string.param_denoising_strength),
                        value = { Text(text = img2imgParam.denoisingStrength.toString()) })
                    ParamItem(label = stringResource(id = R.string.param_resize_mode),
                        value = { Text(text = ConstValues.Img2ImgResizeModeList[img2imgParam.resizeMode]) })
                    ParamItem(label = stringResource(id = R.string.param_scale_by)) {
                        Text(text = img2imgParam.scaleBy.toString())
                    }
                }
            }
            if (img2imgParam.inpaint == true) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(title = stringResource(R.string.inpaint_dialog_title))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    img2imgParam.maskPath?.let {
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .width(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AsyncImage(
                                model = img2imgParam.maskPath,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color.Black),
                                contentDescription = null,
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    maskPreview?.let {
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .width(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            DisplayBase64Image(base64String = it)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ParamItem(label = stringResource(R.string.mask_blur),
                        value = { Text(text = img2imgParam.maskBlur.toString()) })
                    ParamItem(label = stringResource(R.string.mask_mode),
                        value = {
                            Text(
                                text = ConstValues.MaskInvertOptions.get(
                                    img2imgParam.maskInvert ?: 0
                                )
                            )
                        })
                    ParamItem(label = stringResource(R.string.masked_content),
                        value = {
                            Text(
                                text = ConstValues.InpaintingFillOptions.get(
                                    img2imgParam.inpaintingFill ?: 0
                                )
                            )
                        })
                    ParamItem(label = stringResource(R.string.inpaint_area),
                        value = {
                            Text(
                                text = ConstValues.InpaintingFullResOptions.get(
                                    img2imgParam.inpaintingFullRes ?: 0
                                )
                            )
                        })
                    ParamItem(label = stringResource(R.string.only_masked_padding_pixels)) {
                        Text(text = img2imgParam.inpaintingFullResPadding.toString())
                    }
                }
            }

        }
        currentHistory.hrParam.takeIf { it.enableScale }?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = stringResource(id = R.string.param_hires_fix))
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ParamItem(label = stringResource(id = R.string.param_scale_by),
                    value = { Text(text = it.hrScale.toString()) })
                ParamItem(label = stringResource(id = R.string.param_denoising_strength),
                    value = { Text(text = it.hrDenosingStrength.toString()) })
                ParamItem(label = stringResource(id = R.string.param_upscaler),
                    value = { Text(text = it.hrUpscaler) })
            }
        }
        val controlNetSlots = currentHistory.controlNetParam?.slots
        if (!controlNetSlots.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = stringResource(id = R.string.param_control_net))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                for (i in controlNetSlots.indices) {
                    FilterChip(
                        selected = i == controlCurrentSlotIndex,
                        onClick = {
                            controlCurrentSlotIndex = i
                        },
                        label = {
                            Text(text = stringResource(R.string.slot, i + 1))
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val currentSlot = controlNetSlots.getOrNull(controlCurrentSlotIndex)
            currentSlot?.let {
                Column {
                    Box(
                        modifier = Modifier
                            .height(120.dp)
                            .fillMaxWidth(),
                    ) {
                        AsyncImage(
                            model = it.inputImagePath,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ParamItem(label = stringResource(id = R.string.param_guidance_start),
                            value = { Text(text = it.guidanceStart.toString()) })
                        ParamItem(label = stringResource(id = R.string.param_guidance_end),
                            value = { Text(text = it.guidanceEnd.toString()) })
                        ParamItem(label = stringResource(id = R.string.param_control_mode),
                            value = { Text(text = ConstValues.ControlNetModeList[it.controlMode]) })
                        ParamItem(
                            label = stringResource(id = R.string.param_control_weight),
                            value = { Text(text = it.weight.toString()) })
                        ParamItem(label = stringResource(id = R.string.param_control_model),
                            value = { Text(text = it.model.toString()) })
                    }
                }
            }

        }
        currentHistory.reactorParam?.let { reactorParam ->
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = stringResource(id = R.string.reactor))
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                reactorParam.singleImageResult?.let {
                    Box(
                        modifier = Modifier
                            .height(120.dp)
                            .fillMaxWidth(),
                    ) {
                        DisplayBase64Image(base64String = reactorParam.singleImageResult)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ParamItem(label = stringResource(id = R.string.reactor_gender_detection_source),
                        value = {
                            Text(text = ConstValues.ReactorGenderDetectionOptions.get(reactorParam.genderDetectionSource))
                        }
                    )
                    ParamItem(label = stringResource(id = R.string.reactor_gender_detection_target),
                        value = {
                            Text(text = ConstValues.ReactorGenderDetectionOptions.get(reactorParam.genderDetectionSource))
                        }
                    )
                    ParamItem(label = stringResource(id = R.string.reactor_restore_face),
                        value = {
                            Text(text = reactorParam.restoreFace)
                        }
                    )
                    ParamItem(
                        label = stringResource(id = R.string.reactor_restore_face_visibility),
                        value = { Text(text = reactorParam.restoreFaceVisibility.toString()) })
                    ParamItem(
                        label = stringResource(id = R.string.reactor_codeformer_weight_fidelity),
                        value = { Text(text = reactorParam.codeFormerWeightFidelity.toString()) })
                    ParamItem(
                        label = stringResource(id = R.string.reactor_postprocessing_order),
                        value = {
                            Text(
                                text = if (reactorParam.postprocessingOrder) stringResource(
                                    id = R.string.yes
                                ) else stringResource(
                                    id = R.string.no
                                )
                            )
                        })
                    ParamItem(
                        label = stringResource(id = R.string.reactor_upscaler),
                        value = { Text(text = reactorParam.upscaler) })
                    ParamItem(
                        label = stringResource(id = R.string.reactor_scale_by),
                        value = { Text(text = reactorParam.scaleBy.toString()) })
                    ParamItem(
                        label = stringResource(id = R.string.reactor_upscaler_visibility_if_scale_1),
                        value = { Text(text = reactorParam.upscalerVisibility.toString()) })
                }
            }

        }
        currentHistory.adetailerParam?.let {
            if (it.enabled) {
                with(it) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(title = stringResource(id = R.string.adetailer))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        for (i in slots.indices) {
                            FilterChip(
                                selected = adetailerCurrentSlot == i,
                                onClick = {
                                    adetailerCurrentSlot = i
                                },
                                label = {
                                    Text(text = stringResource(R.string.slot, i + 1))
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    it.slots.getOrNull(adetailerCurrentSlot)?.let {
                        Column {
                            with(it) {
                                FlowRow(
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ParamItem(label = stringResource(id = R.string.model),
                                        value = {
                                            Text(text = adModel)
                                        }
                                    )
                                    ParamItem(label = stringResource(id = R.string.prompts),
                                        value = {
                                            Text(text = adPrompt)
                                        }
                                    )
                                    ParamItem(label = stringResource(id = R.string.param_negative_prompt),
                                        value = {
                                            Text(text = adNegativePrompt)
                                        }
                                    )
                                    ParamItem(label = stringResource(id = R.string.detection_model_confidence_threshold),
                                        value = {
                                            Text(text = adConfidence.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(id = R.string.mask_only_the_top_k_largest_0_to_disable),
                                        value = {
                                            Text(text = adMaskKLargest.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.mask_min_area_ratio),
                                        value = {
                                            Text(text = adMaskMinRatio.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.mask_max_area_ratio),
                                        value = {
                                            Text(text = adMaskMaxRatio.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.mask_x_offset),
                                        value = {
                                            Text(text = adXOffset.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.mask_y_offset),
                                        value = {
                                            Text(text = adYOffset.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.mask_erosion_dilation),
                                        value = {
                                            Text(text = adDilateErode.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.mask_merge_mode),
                                        value = {
                                            Text(text = adMaskMergeInvert)
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.inpaint_mask_blur),
                                        value = {
                                            Text(text = adMaskBlur.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.inpaint_denoising_strength),
                                        value = {
                                            Text(text = adDenoisingStrength.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.inpaint_only_masked),
                                        value = {
                                            Text(text = adInpaintOnlyMasked.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.inpaint_only_masked_padding_pixels),
                                        value = {
                                            Text(text = adInpaintOnlyMaskedPadding.toString())
                                        }
                                    )
                                    if (adUseInpaintWidthHeight) {
                                        ParamItem(label = stringResource(R.string.inpaint_width),
                                            value = {
                                                Text(text = adInpaintWidth.toString())
                                            }
                                        )
                                        ParamItem(label = stringResource(R.string.inpaint_height),
                                            value = {
                                                Text(text = adInpaintHeight.toString())
                                            }
                                        )
                                    }
                                    if (adUseSteps) {
                                        ParamItem(label = stringResource(R.string.param_steps),
                                            value = {
                                                Text(text = adSteps.toString())
                                            }
                                        )
                                    }
                                    if (adUseCfgScale) {
                                        ParamItem(label = stringResource(R.string.param_cfg_scale),
                                            value = {
                                                Text(text = adCfgScale.toString())
                                            }
                                        )
                                    }

                                    if (adUseCheckpoint) {
                                        ParamItem(label = stringResource(R.string.model),
                                            value = {
                                                Text(text = adCheckpoint)
                                            }
                                        )
                                    }
                                    if (adUseVae) {
                                        ParamItem(label = stringResource(R.string.vae),
                                            value = {
                                                Text(text = adVae)
                                            }
                                        )
                                    }
                                    if (adUseSampler) {
                                        ParamItem(label = stringResource(R.string.param_sampler),
                                            value = {
                                                Text(text = adSampler)
                                            }
                                        )
                                    }
                                    if (adUseNoiseMultiplier) {
                                        ParamItem(label = stringResource(R.string.noise_multiplier),
                                            value = {
                                                Text(text = adNoiseMultiplier.toString())
                                            }
                                        )
                                    }
                                    if (adUseClipSkip) {
                                        ParamItem(label = stringResource(R.string.clip_skip),
                                            value = {
                                                Text(text = adClipSkip.toString())
                                            }
                                        )
                                    }
                                    ParamItem(label = stringResource(R.string.restore_face),
                                        value = {
                                            Text(
                                                text = if (adRestoreFace) stringResource(R.string.yes) else stringResource(
                                                    R.string.no
                                                )
                                            )
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.controlnet_model),
                                        value = {
                                            Text(text = adControlnetModel)
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.controlnet_weight),
                                        value = {
                                            Text(text = adControlnetWeight.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.controlnet_guidance_start),
                                        value = {
                                            Text(text = adControlnetGuidanceStart.toString())
                                        }
                                    )
                                    ParamItem(label = stringResource(R.string.controlnet_guidance_end),
                                        value = {
                                            Text(text = adControlnetGuidanceEnd.toString())
                                        }
                                    )

                                }
                            }

                        }
                    }

                }
            }


        }
        currentHistory.xyzParam?.let {
            if (it.enable) {
                with(it) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(title = "XYZ")
                    Spacer(modifier = Modifier.height(8.dp))
                    xAxis?.let {
                        ParamItem(label = "X Axis",
                            value = {
                                Text(text = it.getKey())
                            }
                        )
                        ParamItem(label = "X Axis Value",
                            value = {
                                Text(text = it.getStringValue())
                            }
                        )
                    }
                    yAxis?.let {
                        ParamItem(label = "Y Axis",
                            value = {
                                Text(text = it.getKey())
                            }
                        )
                        ParamItem(label = "Y Axis Value",
                            value = {
                                Text(text = it.getStringValue())
                            }
                        )
                    }
                }
            }
        }
    }


}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.W500,
        fontSize = 18.sp
    )
}