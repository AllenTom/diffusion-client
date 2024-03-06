package com.allentom.diffusion

import android.content.Context
import com.allentom.diffusion.api.translate.TranslateLanguages

object ConstValues {
    lateinit var Img2ImgResizeModeList: List<String>
    lateinit var ControlNetModeList: List<String>
    lateinit var ControlNetResizeModeList: List<String>
    lateinit var MaskInvertOptions: List<String>
    lateinit var InpaintingFillOptions: List<String>
    lateinit var InpaintingFullResOptions: List<String>
    lateinit var ReactorGenderDetectionOptions: List<String>
    lateinit var ReactorRestoreFaceOption: List<String>
    lateinit var AdetailerMaskMergeOptions: List<String>
    lateinit var HiresFixModeList: List<String>
    lateinit var SearchTypeMapping: Map<String, String>
    lateinit var TranslateLangs: Map<TranslateLanguages,String>
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
            "None", "CodeFormer", "GFPGAN"
        )
        AdetailerMaskMergeOptions = listOf(
            context.getString(R.string.none),
            context.getString(R.string.merge),
            context.getString(R.string.merge_and_invert)
        )

        HiresFixModeList = listOf(
            context.getString(R.string.param_scale_by),
            context.getString(R.string.resize_and_fill),
        )
        SearchTypeMapping = mapOf(
            "prompt" to context.getString(R.string.param_prompt),
            "style" to context.getString(R.string.styles)
        )
        ControlNetResizeModeList = listOf(
            context.getString(R.string.just_resize),
            context.getString(R.string.scale_to_fit_inner_fit),
            context.getString(R.string.envelope_outer_fit),
        )
        TranslateLangs = mapOf(
            TranslateLanguages.Auto to context.getString(R.string.auto),
            TranslateLanguages.Chinese to context.getString(R.string.chinese),
            TranslateLanguages.English to context.getString(R.string.english),
            TranslateLanguages.Korean to context.getString(R.string.korean),
            TranslateLanguages.Japanese to context.getString(R.string.japanese),
            TranslateLanguages.French to context.getString(R.string.french),
            TranslateLanguages.Spanish to context.getString(R.string.spanish),
            TranslateLanguages.Thai to context.getString(R.string.thai),
            TranslateLanguages.Arabic to context.getString(R.string.arabic),
            TranslateLanguages.Russian to context.getString(R.string.russian),
            TranslateLanguages.Portuguese to context.getString(R.string.portuguese),
            TranslateLanguages.German to context.getString(R.string.german),
            TranslateLanguages.Italian to context.getString(R.string.italian),
            TranslateLanguages.Greek to context.getString(R.string.greek),
            TranslateLanguages.Dutch to context.getString(R.string.dutch),
            TranslateLanguages.Polish to context.getString(R.string.polish),
            TranslateLanguages.Bulgarian to context.getString(R.string.bulgarian),
            TranslateLanguages.Estonian to context.getString(R.string.estonian),
            TranslateLanguages.Danish to context.getString(R.string.danish),
            TranslateLanguages.Finnish to context.getString(R.string.finnish),
            TranslateLanguages.Czech to context.getString(R.string.czech),
            TranslateLanguages.Romanian to context.getString(R.string.romanian),
            TranslateLanguages.Slovenian to context.getString(R.string.slovenian),
            TranslateLanguages.Swedish to context.getString(R.string.swedish),
            TranslateLanguages.Hungarian to context.getString(R.string.hungarian),
            TranslateLanguages.TraditionalChinese to context.getString(R.string.traditional_chinese),
            TranslateLanguages.Vietnamese to context.getString(R.string.vietnamese)
        )

    }


}