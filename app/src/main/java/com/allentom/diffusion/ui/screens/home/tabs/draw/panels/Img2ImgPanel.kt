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
            value = DrawViewModel.img2ImgParam.imgBase64,
            onValueChange = { _, it, filePath, width, height ->
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(
                    imgBase64 = it,
                    imgFilename = filePath,
                    width = width,
                    height = height
                )
            }
        )
        if (DrawViewModel.img2ImgParam.imgBase64 != null) {
            SwitchOptionItem(
                label = stringResource(id = R.string.inpaint_dialog_title),
                value = DrawViewModel.img2ImgParam.inpaint
            ) {
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(inpaint = it)
            }
        }
        if (DrawViewModel.img2ImgParam.imgBase64 != null && DrawViewModel.img2ImgParam.inpaint) {
            MaskDrawOptionItem(
                label = stringResource(R.string.inpaint_mask),
                value = DrawViewModel.inputImg2ImgMaskPreview,
                backgroundImageBase64 = DrawViewModel.img2ImgParam.imgBase64
            ) {
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(
                    mask = it,
                )
                DrawViewModel.inputImg2ImgMaskPreview = Util.combineBase64Images(
                    DrawViewModel.img2ImgParam.imgBase64!!,
                    it
                )
            }
            SliderOptionItem(label = stringResource(id = R.string.mask_blur),
                value = DrawViewModel.img2ImgParam.maskBlur,
                valueRange = 0f..64f,
                useInt = true,
                onValueChangeInt = {
                    DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(
                        maskBlur = it.toFloat()
                    )
                }

            )
            TextPickUpItem(
                label = stringResource(id = R.string.mask_mode),
                value = ConstValues.MaskInvertOptions[DrawViewModel.img2ImgParam.inpaintingMaskInvert],
                options = ConstValues.MaskInvertOptions
            ) {
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(
                    inpaintingMaskInvert = ConstValues.MaskInvertOptions.indexOf(it)
                )
            }
            TextPickUpItem(
                label = stringResource(id = R.string.masked_content),
                value = ConstValues.InpaintingFillOptions[DrawViewModel.img2ImgParam.inpaintingFill],
                options = ConstValues.InpaintingFillOptions
            ) {
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(
                    inpaintingFill = ConstValues.InpaintingFillOptions.indexOf(it)
                )
            }
            TextPickUpItem(
                label = stringResource(id = R.string.inpaint_area),
                value = ConstValues.InpaintingFullResOptions[DrawViewModel.img2ImgParam.inpaintingFullRes],
                options = ConstValues.InpaintingFullResOptions
            ) {
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(
                    inpaintingFullRes = ConstValues.InpaintingFullResOptions.indexOf(it)
                )
            }
            SliderOptionItem(label = stringResource(id = R.string.only_masked_padding_pixels),
                value = DrawViewModel.img2ImgParam.inpaintingFullResPadding.toFloat(),
                valueRange = 0f..256f,
                useInt = true,
                onValueChangeInt = {
                    DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(
                        inpaintingFullResPadding = it
                    )
                }
            )

        }


        SliderOptionItem(label = stringResource(R.string.param_denoising_strength),
            value = DrawViewModel.img2ImgParam.denoisingStrength, valueRange = 0f..1f,
            baseFloat = 0.01f,
            onValueChangeFloat = {
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(denoisingStrength = it)
            })
        SliderOptionItem(label = stringResource(R.string.param_cfg_scale),
            value = DrawViewModel.img2ImgParam.cfgScale, valueRange = 1f..30f,
            useInt = true,
            baseFloat = 0.5f,
            onValueChangeInt = {
                DrawViewModel.img2ImgParam =
                    DrawViewModel.img2ImgParam.copy(cfgScale = it.toFloat())
            })
        TextPickUpItem(
            label = stringResource(R.string.param_resize_mode),
            value = ConstValues.Img2ImgResizeModeList[DrawViewModel.img2ImgParam.resizeMode],
            options = ConstValues.Img2ImgResizeModeList
        )
        SliderOptionItem(label = stringResource(id = R.string.param_scale_by),
            value = DrawViewModel.img2ImgParam.scaleBy,
            valueRange = 0.05f..4f,
            baseFloat = 0.05f,
            onValueChangeFloat = {
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(scaleBy = it)
            })
        SliderOptionItem(label = stringResource(R.string.param_width),
            value = DrawViewModel.img2ImgParam.width.toFloat(),
            valueRange = 64f..2048f,
            steps = (2048 - 64) / 8,
            useInt = true,
            onValueChangeInt = {
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(width = it)
            })
        SliderOptionItem(label = stringResource(R.string.param_height),
            value = DrawViewModel.img2ImgParam.height.toFloat(),
            valueRange = 64f..2048f,
            steps = (2048 - 64) / 8,
            useInt = true,
            onValueChangeInt = {
                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(height = it)
            })
    }
}