package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import kotlin.math.max

@Composable
fun CanvasSizeItem(
    width: Int,
    height: Int,
    label: String,
    fullWidth: Boolean = true,
    onConfirm: (Int, Int) -> Unit
) {
    var isPickupDialogShow by remember { mutableStateOf(false) }
    var inputWidth by remember { mutableStateOf(width) }
    var inputHeight by remember { mutableStateOf(height) }
    var rawWidth by remember { mutableStateOf(width.toString()) }
    var rawHeight by remember { mutableStateOf(height.toString()) }
    val boxRealSize = Util.calculateActualSize(
        200,
        200,
        inputWidth,
        inputHeight
    )
    var lockRatio by remember { mutableStateOf(false) }
    val swapIcon = ImageVector.vectorResource(id = R.drawable.ic_swap)
    val lockIcon = ImageVector.vectorResource(id = R.drawable.ic_lock)
    val unlockIcon = ImageVector.vectorResource(id = R.drawable.ic_unlock)
    val pickRatios = listOf("1:1", "4:3", "3:2", "16:9", "16:10", "21:9")
    OptionDisplay(
        label = label,
        value = "$width px x $height px",
        fullWidth = fullWidth
    ) {
        isPickupDialogShow = true
    }
    if (isPickupDialogShow) {
        AlertDialog(
            onDismissRequest = { isPickupDialogShow = false },
            confirmButton = {
                Button(onClick = {
                    onConfirm(inputWidth, inputHeight)
                    isPickupDialogShow = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                Button(onClick = {
                    isPickupDialogShow = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Row {
                    Text(text = label)
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center

                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .width(boxRealSize.first.dp)
                                    .height(boxRealSize.second.dp)
                            )
                            Box {
                                Text(
                                    text = "$inputWidth px X $inputHeight px",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (lockRatio) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ),
                                onClick = {
                                    lockRatio = !lockRatio
                                },
                            ) {
                                Icon(
                                    if (lockRatio) lockIcon else unlockIcon,
                                    contentDescription = null,
                                    tint = if (lockRatio) MaterialTheme.colorScheme.onPrimaryContainer else LocalContentColor.current
                                )

                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                val temp = inputWidth
                                inputWidth = inputHeight
                                inputHeight = temp
                                val tempRaw = rawWidth
                                rawWidth = rawHeight
                                rawHeight = tempRaw
                            }) {
                                Icon(swapIcon, contentDescription = null)
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(pickRatios.size) { idx ->
                                AssistChip(
                                    onClick = {
                                        val ratio = pickRatios[idx].split(":")
                                        val w = ratio[0].toInt()
                                        val h = ratio[1].toInt()
                                        inputHeight = inputWidth * h / w
                                        rawHeight = inputHeight.toString()
                                    }, label = {
                                        Text(pickRatios[idx])
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                            }

                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(R.string.param_width))
                        Slider(
                            value = inputWidth.toFloat(),
                            onValueChange = {
                                val oldWidth = inputWidth
                                inputWidth = it.toInt()
                                rawWidth = it.toInt().toString()
                                if (lockRatio) {
                                    inputHeight =
                                        max(
                                            1,
                                            (inputHeight.toFloat() / oldWidth * inputWidth).toInt()
                                        )
                                    rawHeight = inputHeight.toString()
                                }
                            },
                            valueRange = 0f..2048f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = rawWidth,
                            onValueChange = {
                                val oldWidth = inputWidth
                                rawWidth = it
                                it.toIntOrNull()?.let { value ->
                                    inputWidth = value
                                    if (lockRatio) {
                                        inputHeight =
                                            max(
                                                1,
                                                (inputHeight.toFloat() / oldWidth * inputWidth).toInt()
                                            )
                                        rawHeight = inputHeight.toString()
                                    }
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.param_height))
                        Slider(
                            value = inputHeight.toFloat(),
                            onValueChange = {
                                val oldHeight = inputHeight
                                inputHeight = it.toInt()
                                rawHeight = it.toInt().toString()
                                if (lockRatio) {
                                    inputWidth =
                                        max(
                                            1,
                                            (inputWidth.toFloat() / oldHeight * inputHeight).toInt()
                                        )
                                    rawWidth = inputWidth.toString()
                                }
                            },
                            valueRange = 0f..2048f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = rawHeight,
                            onValueChange = {
                                val oldHeight = inputHeight
                                rawHeight = it
                                it.toIntOrNull()?.let { value ->
                                    inputHeight = value
                                    if (lockRatio) {
                                        inputWidth =
                                            max(
                                                1,
                                                (inputWidth.toFloat() / oldHeight * inputHeight).toInt()
                                            )
                                        rawWidth = inputWidth.toString()
                                    }
                                }
                            },
                        )
                    }
                }
            }
        )
    }
}