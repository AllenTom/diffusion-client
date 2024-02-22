package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.TextAreaOptionItem
import com.allentom.diffusion.composables.TextListPickUpItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.GenModifier
import com.allentom.diffusion.ui.screens.home.tabs.draw.IntListModifier
import com.allentom.diffusion.ui.screens.home.tabs.draw.ModifierLibrary
import com.allentom.diffusion.ui.screens.home.tabs.draw.TextOptionListModifier


@Composable
fun AxisPanel() {
    val context = LocalContext.current

    @Composable
    fun render(mod: GenModifier) {
        when (mod) {
            is IntListModifier -> {
                TextAreaOptionItem(
                    label = mod.getDisplayLabel(context),
                    value = mod.getStringValue(),
                    onValueChange = {
                        try {
                            mod.parseRawValue(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )
            }

            is TextOptionListModifier -> {
                TextListPickUpItem(
                    label = mod.getDisplayLabel(context),
                    value = mod.args,
                    options = mod.getOptions(),
                ) {
                    mod.args = it
                }
            }
        }
        mod.getExtraInput().forEach {
            when (it.inputType) {
                "Int" ->
                    SliderOptionItem(
                        label = it.getDisplayLabel(context),
                        value = it.value.toFloat(),
                        useInt = true,
                        valueRange = it.valueRange,
                    ) { newVal ->
                        it.onValueChange?.invoke(newVal.toString())
                    }
            }

        }

    }
    Column {
        TextPickUpItem(
            label = stringResource(R.string.xaxis),
            value = DrawViewModel.xyzParam.xAxis?.getDisplayLabel(context),
            options = ModifierLibrary.AxisOption.values.toList()
        ) {
            val key = ModifierLibrary.AxisOption.keys.find { key ->
                ModifierLibrary.AxisOption[key] == it
            }
            if (key == null) {
                return@TextPickUpItem
            }
            if (it == "none") {
                DrawViewModel.xyzParam = DrawViewModel.xyzParam.copy(xAxis = null)
                return@TextPickUpItem
            }
            DrawViewModel.xyzParam =
                DrawViewModel.xyzParam.copy(xAxis = ModifierLibrary.getModifierByName(key))
        }
        DrawViewModel.xyzParam.xAxis?.let { mod ->
            render(mod = mod)
        }
        TextPickUpItem(
            label = stringResource(R.string.yaxis),
            value = DrawViewModel.xyzParam.yAxis?.getDisplayLabel(context),
            options = ModifierLibrary.AxisOption.values.toList()
        ) {
            val key = ModifierLibrary.AxisOption.keys.find { key ->
                ModifierLibrary.AxisOption[key] == it
            }
            if (key == null) {
                return@TextPickUpItem
            }
            if (it == "none") {
                DrawViewModel.xyzParam = DrawViewModel.xyzParam.copy(yAxis = null)
                return@TextPickUpItem
            }
            DrawViewModel.xyzParam =
                DrawViewModel.xyzParam.copy(yAxis = ModifierLibrary.getModifierByName(key))
        }
        DrawViewModel.xyzParam.yAxis?.let { mod ->
            render(mod = mod)
        }
    }
}