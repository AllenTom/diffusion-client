package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R

@Composable
fun SeedOptionItem(
    label: String,
    value: Long,
    title: String = label,
    fullWidth: Boolean = true,
    onValueChange: (Long) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputSeed by remember { mutableStateOf(value) }
    val iconDice = ImageVector.vectorResource(id = R.drawable.ic_dice)
    var inputTextFiledValue by remember { mutableStateOf(value.toString()) }
    OptionDisplay(
        label = label,
        value = value.toString(),
        fullWidth = fullWidth
    ) {
        showDialog = true
    }
    fun rollDice():Long{
        return (0..100000000).random().toLong()
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    SwitchOptionItem(label = stringResource(R.string.random), value = inputSeed == -1L) {
                        inputSeed = if (it) -1L else rollDice()
                        inputTextFiledValue = inputSeed.toString()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = inputTextFiledValue,
                            onValueChange = {
                                it.toLongOrNull()?.let { seed -> inputSeed = seed }
                                inputTextFiledValue = it
                            },
                            isError = inputTextFiledValue.toLongOrNull() == null,
                            trailingIcon = {
                                IconButton(onClick = {
                                    inputSeed = rollDice()
                                    inputTextFiledValue = inputSeed.toString()
                                }) {
                                    Icon(iconDice, contentDescription = "Roll")
                                }
                            }
                        )
                    }

                }
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    onValueChange(inputSeed)
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}