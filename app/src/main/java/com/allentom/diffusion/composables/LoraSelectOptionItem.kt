package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.store.prompt.LoraPrompt

@Composable
fun LoraSelectOptionItem(
    label: String,
    value: List<LoraPrompt>,
    title: String = label,
    loraList: List<Lora>,
    onValueChange: (List<LoraPrompt>) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = label) },
        supportingContent = {
            Column {
                value.forEach {
                    ListItem(
                        headlineContent = {
                            Row {
                                Text(text = String.format("%.1f", it.weight))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = it.name)
                            }
                        },
                        supportingContent = {
                            PromptFlowRow(promptList = it.prompts)
                        },
                    )
                }
            }
        }
    )
    if (showDialog) {
        LoraSelectDialog(
            loraList = loraList,
            value = value,
            title = title,
            onDismiss = { showDialog = false },
            onValueChange = {
                showDialog = false
                onValueChange(it)
            }
        )
    }
}