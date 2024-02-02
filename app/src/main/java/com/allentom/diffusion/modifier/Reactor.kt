package com.allentom.diffusion.modifier

import android.content.Context
import com.allentom.diffusion.R
import com.allentom.diffusion.service.Text2ImageParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.TextOptionListModifier

class ReactorEnableModifier : TextOptionListModifier() {
    override fun getOptions(): List<String> {
        return listOf("Enable", "Disable")
    }

    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        return param.copy(
            reactorParam = param.reactorParam?.copy(
                enabled = args[index] == "Enable"
            )
        )
    }

    override fun getKey(): String {
        return "ReactorEnable"
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.mod_reactorenable)
    }
}