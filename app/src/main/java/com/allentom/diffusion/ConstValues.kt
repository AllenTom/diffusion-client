package com.allentom.diffusion

import android.content.Context

object ConstValues {
    lateinit var Img2ImgResizeModeList: List<String>
    lateinit var ControlNetModeList: List<String>
    lateinit var MaskInvertOptions: List<String>
    lateinit var InpaintingFillOptions: List<String>
    lateinit var InpaintingFullResOptions: List<String>
    lateinit var ReactorGenderDetectionOptions: List<String>
    lateinit var ReactorRestoreFaceOption:List<String>
    fun initValues(context: Context) {
        Img2ImgResizeModeList = listOf(
            context.getString(R.string.just_resize),
            context.getString(R.string.crop_and_resize),
            context.getString(R.string.resize_and_fill),
            context.getString(R.string.just_resize_latent_upscale)
        )
        ControlNetModeList =
            listOf(
                context.getString(R.string.balanced),
                context.getString(R.string.my_prompt_is_more_important),
                context.getString(R.string.controlnet_is_more_important)
            )

        MaskInvertOptions = listOf(
            context.getString(R.string.inpaint_masked),
            context.getString(R.string.inpaint_not_masked)
        )

        InpaintingFillOptions = listOf(
            context.getString(R.string.fill),
            context.getString(R.string.original),
            context.getString(R.string.latent_noise),
            context.getString(R.string.latent_nothing)
        )

        InpaintingFullResOptions = listOf(
            context.getString(R.string.whole_picture),
            context.getString(
                R.string.only_masked
            )
        )
        ReactorGenderDetectionOptions = listOf(
            context.getString(R.string.reactor_gender_detect_no),
            context.getString(R.string.reactor_gender_detect_female_only),
            context.getString(R.string.reactor_gender_detect_male_only)
        )
        ReactorRestoreFaceOption = listOf(
            "None","CodeFormer","GFPGAN"
        )

    }
}