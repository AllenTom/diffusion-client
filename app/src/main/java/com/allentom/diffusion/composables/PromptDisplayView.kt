package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.store.Prompt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PromptDisplayView(
    promptList: List<Prompt>,
    title: String = "Prompt",
    titleComponent: (@Composable () -> Unit)? = null,
    canScroll: Boolean = true,
    onClickPrompt: ((Prompt) -> Unit)? = {},
    onAction: (List<Prompt>) -> Unit
) {
    var selectedPromptList by remember {
        mutableStateOf(emptyList<String>())
    }
    var selectMode by remember {
        mutableStateOf(false)
    }
    Column(
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (titleComponent != null) {
                titleComponent()
            } else {
                Text(text = title)
            }
            Spacer(modifier = Modifier.weight(1f))
            if (selectedPromptList.isNotEmpty()) {
                TextButton(onClick = {
                    val contextPrompt = promptList.filter { prompt ->
                        selectedPromptList.contains(prompt.text)
                    }
                    onAction(contextPrompt)
                }) {
                    Text(text = "Action")
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            TextButton(onClick = {
                if (!selectMode) {
                    selectMode = true
                    return@TextButton
                }
                if (selectedPromptList.size == promptList.size) {
                    selectedPromptList = emptyList()
                    selectMode = false
                    return@TextButton
                } else {
                    selectedPromptList = promptList.map { it.text }
                }
            }) {
                if (selectMode) {
                    if (selectedPromptList.size == promptList.size) {
                        Text(text = "Deselect all")
                    } else {
                        Text(text = "Select all")
                    }
                } else {
                    Text(text = "Select")
                }

            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = if (canScroll) Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())  else {
                Modifier
            }

        ) {
            promptList.forEach {
                FilterChip(
                    selected = selectedPromptList.contains(it.text),
                    leadingIcon = {
                        Text(text = it.piority.toString())
                    },
                    onClick = {
                        if (!selectMode) {
                            onClickPrompt?.invoke(it)
                            return@FilterChip
                        }
                        selectedPromptList =
                            if (selectedPromptList.contains(it.text)) {
                                selectedPromptList.filter { selectedText ->
                                    selectedText != it.text
                                }

                            } else {
                                selectedPromptList + it.text
                            }
                        if (selectedPromptList.isEmpty()) {
                            selectMode = false
                        }
                    },
                    label = {
                        Text(text = it.text)
                    })
            }
        }
    }
}