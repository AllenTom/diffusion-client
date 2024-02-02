package com.allentom.diffusion.modifier

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.R
import com.allentom.diffusion.service.Text2ImageParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.ExtraGenModifyInput
import com.allentom.diffusion.ui.screens.home.tabs.draw.TextOptionListModifier

class ControlNetSlotEnableModifier : TextOptionListModifier() {
    var useSlotIndex by mutableStateOf(1)
    override fun getOptions(): List<String> {
        return listOf("Enable", "Disable")
    }

    override fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam {
        return param.copy(
            controlNetParam = param.controlNetParam?.copy(
                slots = param.controlNetParam.slots.mapIndexed { i, slot ->
                    if (i == useSlotIndex - 1) {
                        slot.copy(enabled = args[index] == "Enable")
                    } else {
                        slot
                    }
                }
            )
        )
    }

    override fun toSaveData(): String {
        return "$useSlotIndex,${args.joinToString(",")}"
    }

    override fun fromSaveData(data: String) {
        try {
            val split = data.split(",")
            useSlotIndex = split[0].toInt()
            args = split.subList(1, split.size)
        } catch (e: Exception) {
            useSlotIndex = 0
            super.fromSaveData("")
        }

    }

    override fun getKey(): String {
        return "controlNetSlotEnable"
    }

    override fun getExtraInput(): List<ExtraGenModifyInput> {
        return listOf(
            ExtraGenModifyInput(
                name = "Slot",
                value = useSlotIndex.toString(),
                inputType = "Int",
                onValueChange = {
                    useSlotIndex = it.toInt()
                },
                getDisplayLabel = { ctx ->
                    ctx.getString(R.string.mod_slot)
                },
                valueRange = 1f..3.toFloat()
            )
        )
    }

    override fun getDisplayLabel(context: Context): String {
        return context.getString(R.string.mod_contronnetslotenable)
    }
}