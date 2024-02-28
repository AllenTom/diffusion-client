package com.allentom.diffusion.api.entity

import com.google.gson.annotations.SerializedName

data class ControlNetModule(
    @SerializedName("module_list") val moduleList: ArrayList<String>,
    @SerializedName("module_detail") val detail: Map<String, ModuleDetail>
)

data class Sliders(

    @SerializedName("name") val name: String,
    @SerializedName("value") val value: Float,
    @SerializedName("min") val min: Float,
    @SerializedName("max") val max: Float,
    @SerializedName("step") val step: Float?

)

data class ModuleDetail(

    @SerializedName("model_free") val modelFree: Boolean,
    @SerializedName("sliders") val sliders: ArrayList<Sliders?>

)

data class ControlNetDetectResult(
    val info: String,
    val images: List<String>,
)

data class ControlType(

    @SerializedName("module_list") var moduleList: ArrayList<String> = arrayListOf(),
    @SerializedName("model_list") var modelList: ArrayList<String> = arrayListOf(),
    @SerializedName("default_option") var defaultOption: String? = null,
    @SerializedName("default_model") var defaultModel: String? = null

)

data class ControlTypesResult(
    @SerializedName("control_types") var controlTypes: Map<String, ControlType> = mapOf()
)
