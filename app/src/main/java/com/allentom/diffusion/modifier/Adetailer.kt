package com.allentom.diffusion.modifier

import android.content.Context
import com.allentom.diffusion.R
import com.allentom.diffusion.service.Text2ImageParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.TextOptionListModifier

class AdetailerSlotEnableModifier : TextOptionListModifier() {
    override fun getOptions(): List<String> {
        return listOf("Enable", "Disable")
    }

    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        return param.copy(
            adetailerParam = param.adetailerParam?.copy(
                enabled = args[index] == "Enable"
            )
        )
    }


    override fun getKey(): String {
        return "AdetailerSlotEnable"
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.mod_adetailer_enable)
    }
}