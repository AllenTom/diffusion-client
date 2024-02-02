package com.allentom.diffusion.modifier;

import android.content.Context
import com.allentom.diffusion.R
import com.allentom.diffusion.api.OverrideSetting
import com.allentom.diffusion.service.Text2ImageParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.IntListModifier
import com.allentom.diffusion.ui.screens.home.tabs.draw.ModifierLibrary
import com.allentom.diffusion.ui.screens.home.tabs.draw.TextOptionListModifier

fun registerModifier(context: Context) {
    ModifierLibrary.registerModifier(context, "step") {
        StepModifier()
    }
    ModifierLibrary.registerModifier(context, "cfgScale") {
        CFGScaleModifier()
    }
    ModifierLibrary.registerModifier(context, "sampler") {
        SamplerModifier()
    }
    ModifierLibrary.registerModifier(context, "sdModel") {
        ModelModifier()
    }
    ModifierLibrary.registerModifier(context, "controlNetSlotEnable") {
        ControlNetSlotEnableModifier()
    }
    ModifierLibrary.registerModifier(context, "AdetailerSlotEnable") {
        AdetailerSlotEnableModifier()
    }
    ModifierLibrary.registerModifier(context, "ReactorEnable") {
        ReactorEnableModifier()
    }
    ModifierLibrary.registerModifier(context, "vaeModel") {
        VaeModifier()
    }
    ModifierLibrary.registerModifier(context, "hiresSampler") {
        HiresSamplerModifier()
    }
}

class StepModifier : IntListModifier() {
    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        return param.copy(steps = args[index])
    }

    override fun getKey(): String {
        return "step"
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.param_steps)
    }
}

class CFGScaleModifier : IntListModifier() {
    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        return param.copy(cfgScale = args[index].toFloat())
    }

    override fun getKey(): String {
        return "cfgScale"
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.param_cfg_scale)
    }
}

class SamplerModifier : TextOptionListModifier() {
    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        return param.copy(samplerName = args[index])
    }

    override fun getKey(): String {
        return "sampler"
    }

    override fun getOptions(): List<String> {
        return DrawViewModel.samplerList.map { it.name }
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.param_sampler)
    }
}

class ModelModifier : TextOptionListModifier() {
    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        val overrideSetting = param.overrideSetting ?: OverrideSetting()
        return param.copy(
            overrideSetting = overrideSetting.copy(
                sdModelCheckpoint = args[index]
            )
        )
    }

    override fun getKey(): String {
        return "sdModel"
    }

    override fun getOptions(): List<String> {
        return DrawViewModel.models.map { it.modelName }
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.param_model)
    }
}


class VaeModifier : TextOptionListModifier() {
    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        val overrideSetting = param.overrideSetting ?: OverrideSetting()
        return param.copy(
            overrideSetting = overrideSetting.copy(
                sdVae = args[index]
            )
        )
    }

    override fun getKey(): String {
        return "vaeModel"
    }

    override fun getOptions(): List<String> {
        return DrawViewModel.vaeList.map { it.modelName }
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.vae)
    }
}

class HiresSamplerModifier : TextOptionListModifier() {
    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        return param.copy(
            hiresFixParam = param.hiresFixParam?.copy(
                hrUpscaler = args[index]
            )
        )
    }

    override fun getKey(): String {
        return "hiresSampler"
    }

    override fun getOptions(): List<String> {
        return DrawViewModel.upscalers.map { it.name }
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.mod_hrupscaler)
    }
}

class HiresStepModifier : IntListModifier() {
    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        return param.copy(
            hiresFixParam = param.hiresFixParam?.copy(
                hrSteps = args[index].toLong()
            )
        )
    }

    override fun getKey(): String {
        return "hiresStep"
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.mod_hirstep)
    }
}