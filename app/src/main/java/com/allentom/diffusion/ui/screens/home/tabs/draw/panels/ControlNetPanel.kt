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
import com.allentom.diffusion.api.entity.ControlType
import com.allentom.diffusion.composables.ImageBase64PickupOptionItem
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.ui.screens.home.tabs.draw.ControlNetParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.ControlNetSlot
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlNetPanel(
    onValueChange: (ControlNetParam) -> Unit,
    controlNetParam: ControlNetParam
) {
    var selectedSlot by remember { mutableIntStateOf(0) }
    var isEditMode by remember { mutableStateOf(false) }
    fun onIndexUpdate(slot: ControlNetSlot) {
        onValueChange(
            controlNetParam.copy(
                slots = controlNetParam.slots.mapIndexed { index, controlNetSlot ->
                    if (index == selectedSlot) {
                        slot
                    } else {
                        controlNetSlot
                    }
                }
            )
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                for (i in 0..<controlNetParam.slots.size) {
                    FilterChip(
                        selected = i == selectedSlot,
                        onClick = {
                            selectedSlot = i
                        },
                        label = {
                            Text(text = stringResource(id = R.string.slot, i + 1))
                        },
                        trailingIcon = {
                            if (isEditMode && controlNetParam.slots.size > 1) {
                                IconButton(onClick = {
                                    onValueChange(
                                        controlNetParam.copy(
                                            slots = controlNetParam.slots.filterIndexed { index, _ ->
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
                if (isEditMode && controlNetParam.slots.size < 3) {
                    IconButton(onClick = {
                        onValueChange(
                            controlNetParam.copy(
                                slots = controlNetParam.slots + ControlNetSlot()
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
            val currentParam = controlNetParam.slots.getOrNull(selectedSlot)
            currentParam?.let { slot ->
                SwitchOptionItem(
                    label = stringResource(R.string.enable),
                    value = slot.enabled
                ) {
                    onIndexUpdate(slot.copy(enabled = it))
                }
                ImageBase64PickupOptionItem(
                    label = stringResource(R.string.param_control_image),
                    value = slot.inputImage
                ) { uri, it, _, _, _ ->
                    onIndexUpdate(slot.copy(inputImage = it, inputImagePath = uri.toString()))
                }
                TextPickUpItem(
                    label = stringResource(R.string.controlnet_controltype),
                    value = slot.controlType,
                    options = DrawViewModel.controlNetTypes.keys.toList()
                ) {
                    onIndexUpdate(
                        slot.copy(
                            controlType = it,
                            model = DrawViewModel.controlNetTypes[it]?.defaultModel ?: ""
                        )
                    )
                }
                DrawViewModel.controlNetTypes.get(slot.controlType)?.let { controlType ->
                    TextPickUpItem(
                        label = stringResource(R.string.controlnet_preprocessor),
                        value = slot.preprocessor,
                        options = controlType.moduleList
                    ) {
                        onIndexUpdate(slot.copy(preprocessor = it))
                    }
                    controlType.modelList.let { modelList ->
                        TextPickUpItem(
                            label = stringResource(R.string.param_control_model),
                            value = slot.model,
                            options = modelList
                        ) {
                            onIndexUpdate(slot.copy(model = it))
                        }
                    }
                }
                DrawViewModel.modulesDetailList.get(slot.preprocessor)?.let { moduleDetail ->
                    moduleDetail.sliders.forEachIndexed { idx, sl ->
                        if (sl == null) {
                            return@forEachIndexed
                        }
                        when (idx) {
                            0 -> {
                                SliderOptionItem(
                                    value = slot.processorRes,
                                    onValueChangeFloat = {
                                        onIndexUpdate(slot.copy(processorRes = it))
                                    },
                                    valueRange = sl.min..sl.max,
                                    label = sl.name,
                                    steps = sl.step.let {
                                        if (it == null) {
                                            return@let 0
                                        }
                                        return@let ((sl.max - sl.min) / it).toInt()
                                    }
                                )
                            }

                            1 -> {
                                SliderOptionItem(
                                    value = slot.thresholdA,
                                    onValueChangeFloat = {

                                        onIndexUpdate(slot.copy(thresholdA = it))
                                    },
                                    valueRange = sl.min..sl.max,
                                    label = sl.name,
                                    steps = sl.step.let {
                                        if (it == null) {
                                            return@let 0
                                        }
                                        return@let ((sl.max - sl.min) / it).toInt()
                                    }
                                )
                            }

                            2 -> {
                                SliderOptionItem(
                                    value = slot.thresholdB,
                                    onValueChangeFloat = {
                                        onIndexUpdate(slot.copy(thresholdB = it))
                                    },
                                    valueRange = sl.min..sl.max,
                                    label = sl.name,
                                    steps = sl.step.let {
                                        if (it == null) {
                                            return@let 0
                                        }
                                        return@let ((sl.max - sl.min) / it).toInt()
                                    }
                                )
                            }
                        }

                    }
                }
                SliderOptionItem(
                    label = stringResource(R.string.param_guidance_start),
                    value = slot.guidanceStart,
                    valueRange = 0f..1f,
                    baseFloat = 0.01f,
                    onValueChangeFloat = {
                        onIndexUpdate(slot.copy(guidanceStart = it))
                    })
                SliderOptionItem(
                    label = stringResource(R.string.param_guidance_end),
                    value = slot.guidanceEnd,
                    valueRange = 0f..1f,
                    baseFloat = 0.01f,
                    onValueChangeFloat = {
                        onIndexUpdate(slot.copy(guidanceEnd = it))
                    })
                TextPickUpItem(
                    label = stringResource(R.string.param_control_mode),
                    value = ConstValues.ControlNetModeList[slot.controlMode],
                    options = ConstValues.ControlNetModeList
                ) {
                    onIndexUpdate(slot.copy(controlMode = ConstValues.ControlNetModeList.indexOf(it)))
                }
                TextPickUpItem(
                    label = stringResource(R.string.controlnet_resizemode),
                    value = ConstValues.ControlNetResizeModeList[slot.resizeMode],
                    options = ConstValues.ControlNetResizeModeList
                ) {
                    onIndexUpdate(
                        slot.copy(
                            resizeMode = ConstValues.ControlNetResizeModeList.indexOf(
                                it
                            )
                        )
                    )
                }
                SliderOptionItem(
                    label = stringResource(R.string.param_control_weight),
                    value = slot.weight,
                    valueRange = 0f..2f,
                    baseFloat = 0.05f,
                    onValueChangeFloat = {
                        onIndexUpdate(slot.copy(weight = it))
                    })

            }

        }
    }


}