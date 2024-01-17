package com.allentom.diffusion.ui.screens.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.Util
import com.allentom.diffusion.store.SaveHistory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(history: SaveHistory, onDismiss: () -> Unit, onUseParams: () -> Unit) {
    val modalBottomSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        LazyColumn {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    LazyRow {
                        item {
                            for (img in history.imagePaths) {
                                val imgRatio = history.width.toFloat() / history.height.toFloat()
                                AsyncImage(
                                    model = img.path, contentDescription = null, modifier = Modifier
                                        .width((200 * imgRatio).dp)
                                        .height(200.dp),
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }

                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = {
                            onUseParams()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Use Params")
                    }
                }

                ListItem(
                    headlineContent = {
                        Text(text = "Prompt")
                    },
                    supportingContent = { Text(text = history.prompt.map { it.getPromptText() }.joinToString(",")) },
                )
                ListItem(
                    headlineContent = {
                        Text(text = "Negative Prompt")
                    },
                    supportingContent = { Text(text = history.negativePrompt.map { it.getPromptText() }.joinToString(",")) },
                )
                ListItem(
                    headlineContent = {
                        Text(text = "Steps")
                    },
                    supportingContent = { Text(text = history.steps.toString()) },
                )
                ListItem(
                    headlineContent = {
                        Text(text = "Sampler Name")
                    },
                    supportingContent = { Text(text = history.samplerName) },
                )
                ListItem(
                    headlineContent = {
                        Text(text = "SD Model Checkpoint")
                    },
                    supportingContent = { Text(text = history.sdModelCheckpoint) },
                )
                ListItem(
                    headlineContent = {
                        Text(text = "Width")
                    },
                    supportingContent = { Text(text = history.width.toString()) },
                )
                ListItem(
                    headlineContent = {
                        Text(text = "Height")
                    },
                    supportingContent = { Text(text = history.height.toString()) },
                )
                ListItem(
                    headlineContent = {
                        Text(text = "CFG Scale")
                    },
                    supportingContent = { Text(text = history.cfgScale.toString()) },
                )
                ListItem(
                    headlineContent = {
                        Text(text = "Batch Size")
                    },
                    supportingContent = { Text(text = history.batchSize.toString()) },
                )
                ListItem(
                    headlineContent = {
                        Text(text = "Time")
                    },
                    supportingContent = { Text(text = Util.formatUnixTime(history.time)) },
                )

            }
        }
    }
}