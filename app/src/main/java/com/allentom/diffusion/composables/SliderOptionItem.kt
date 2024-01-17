package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R
import kotlin.math.roundToInt

@Composable
fun SliderOptionItem(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    useInt: Boolean = false,
    title: String = label,
    onValueChangeFloat: (Float) -> Unit = {},
    onValueChangeInt: (Int) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputValue by remember { mutableFloatStateOf(value) }
    var inputTextValue by remember { mutableStateOf(value.toString()) }
    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = label) },
        supportingContent = {
            Text(text = useInt.let {
                if (it) value.toInt().toString() else value.toString()
            })
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    Text(text = inputValue.toString())
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = inputValue,
                        onValueChange = {
                            inputValue = if (useInt) {
                                it.roundToInt().toFloat()
                            } else {
                                it
                            }
                            inputTextValue = inputValue.toString()
                        },
                        valueRange = valueRange,
                        steps = steps,
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = inputTextValue, onValueChange = {
                        inputTextValue = it
                        try {
                            inputValue = if (useInt) {
                                it.toInt().toFloat()
                            } else {
                                it.toFloat()
                            }
                        } catch (e: Exception) {

                        }
                    })
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    if (useInt) {
                        onValueChangeInt(inputValue.toInt())
                    } else {
                        onValueChangeFloat(inputValue)
                    }
                }) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}