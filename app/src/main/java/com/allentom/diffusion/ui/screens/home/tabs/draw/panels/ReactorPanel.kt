package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.ImageBase64PickupOptionItem
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorParam

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