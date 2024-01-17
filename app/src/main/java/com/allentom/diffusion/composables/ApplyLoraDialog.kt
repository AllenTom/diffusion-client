package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.store.LoraPrompt
import com.allentom.diffusion.store.LoraPromptWithRelation
import com.allentom.diffusion.store.Prompt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ApplyLoraDialog(
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.apply_lora),
    onApply: (lora: LoraPrompt) -> Unit,
    lora:LoraPromptWithRelation,
) {
    var weight by remember {
        mutableStateOf(0.8f)
    }
    var selectedPrompt by remember {
        mutableStateOf<List<Prompt>>(emptyList())
    }
    val triggerPrompt = lora.triggerText.map {
        Prompt(text = it.text, piority = 0)
    }
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = title)
        },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    lora.loraPrompt.previewPath?.let { coverPath ->
                        AsyncImage(
                            model = coverPath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }.also {
                        Text(text = stringResource(id = R.string.no_preview))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Weight: $weight")
                Slider(
                    value = weight,
                    onValueChange = {
                        weight = "%.2f".format(it).toFloat()
                    },
                    valueRange = 0f..1f,
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),

                ) {
                    triggerPrompt.forEach {
                        FilterChip(
                            onClick = {
                                selectedPrompt = if (selectedPrompt.contains(it)) {
                                    selectedPrompt.filter { prompt ->
                                        prompt != it
                                    }
                                } else {
                                    selectedPrompt + it
                                }
                            },
                            label = {
                                Text(text = it.text)
                            },
                            selected = selectedPrompt.contains(it)
                        )
                    }

                }

            }
        },
        confirmButton = {
            Button(onClick = {
                onApply(
                    lora.loraPrompt.toPrompt().copy(prompts = selectedPrompt)
                )
            }) {
                Text(text = stringResource(id = R.string.apply))
            }
        },
    )
}