package com.allentom.diffusion.ui.screens.home.tabs.draw

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.api.OverrideSetting
import com.allentom.diffusion.service.Text2ImageParam
import com.google.gson.Gson
import org.w3c.dom.Text

object ModifierLibrary {
    val AxisOption = mutableMapOf<String,String>()
    val factory = mutableMapOf<String, () -> GenModifier>()
    fun getModifierByName(name: String): GenModifier? {
        return factory[name]?.invoke()
    }
    fun registerModifier(context: Context,name: String,modifier: () -> GenModifier) {
        if (factory.containsKey(name)) {
            return
        }
        factory[name] = modifier
        val mod = modifier()
        AxisOption[name] = mod.getDisplayLabel(context)
    }
    fun init(context: Context){
        AxisOption["none"] = context.getString(R.string.none)
    }
}


interface GenModifier {
    fun onText2ImageParamChange(param: Text2ImageParam, index: Int): Text2ImageParam
    fun getKey(): String

    fun getStringValue(): String
    fun parseRawValue(value: String)
    fun getGenCount(): Int

    fun toSaveData(): String
    fun fromSaveData(data: String)

    fun getValueByIndex(index: Int): Any
    fun getExtraInput(): List<ExtraGenModifyInput>

    fun getDisplayLabel(context: Context): String
}

abstract class IntListModifier : GenModifier {
    var args by mutableStateOf<List<Int>>(emptyList())

    override fun getStringValue(): String {
        return args.joinToString(",")
    }

    override fun parseRawValue(value: String) {
        args = value.split(",").map { it.toInt() }.toMutableList()
    }

    override fun getGenCount(): Int {
        return args.size
    }

    override fun toSaveData(): String {
        return getStringValue()
    }

    override fun fromSaveData(data: String) {
        try {
            args = data.split(",").map { it.toInt() }
        } catch (e: Exception) {
            args = emptyList()
        }
    }

    override fun getValueByIndex(index: Int): Any {
        return args[index]
    }

    override fun getExtraInput(): List<ExtraGenModifyInput> {
        return emptyList()
    }

}


abstract class TextOptionListModifier : GenModifier {
    var args by mutableStateOf<List<String>>(emptyList())

    override fun getStringValue(): String {
        return args.joinToString(",")
    }

    override fun parseRawValue(value: String) {
        args = value.split(",").toMutableList()
    }

    override fun getGenCount(): Int {
        return args.size
    }

    override fun toSaveData(): String {
        return getStringValue()
    }

    override fun fromSaveData(data: String) {
        try {
            args = data.split(",").filter { it.isNotEmpty() }
        } catch (e: Exception) {
            args = emptyList()
        }
    }

    override fun getValueByIndex(index: Int): Any {
        return args[index]
    }

    abstract fun getOptions(): List<String>

    override fun getExtraInput(): List<ExtraGenModifyInput> {
        return emptyList()
    }
}

data class ExtraGenModifyInput(
    val name: String,
    val value: String,
    val inputType: String,
    val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val getDisplayLabel: ((Context) -> String) = { name },
    val onValueChange: ((String) -> Unit)? = null
)


