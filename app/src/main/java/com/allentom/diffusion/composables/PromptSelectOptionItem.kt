package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.allentom.diffusion.store.Prompt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptSelectOptionItem(
    label: String,
    value: List<Prompt>,
    title: String = label,
    onValueChange: (List<Prompt>) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = label) },
        supportingContent = {
            PromptFlowRow(promptList = value)
        }
    )

    if (showDialog) {
        PromptSelectDialog(
            promptList = value,
            title = title,
            onDismiss = { showDialog = false },
            onValueChange = {
                showDialog = false
                onValueChange(it)
            }
        )
    }
}