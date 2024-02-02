package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.composables.ImageBase64PickupOptionItem
import com.allentom.diffusion.composables.MaskDrawOptionItem
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

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