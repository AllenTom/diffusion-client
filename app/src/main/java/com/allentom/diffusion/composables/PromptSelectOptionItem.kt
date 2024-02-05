package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.RegionPromptParam

@Composable
fun PromptSelectOptionItem(
    label: String,
    value: List<Prompt>,
    title: String = label,
    regionPromptParam: RegionPromptParam? = null,
    onValueChange: (List<Prompt>, RegionPromptParam?) -> Unit = { _, _ ->
    }
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = label) },
        supportingContent = {
            PromptFlowRow(promptList = value, regionPromptParam = regionPromptParam)
        }
    )

    if (showDialog) {
        PromptSelectDialog(
            promptList = value,
            title = title,
            onDismiss = { showDialog = false },
            onValueChange = { prompts, region ->
                showDialog = false
                onValueChange(prompts, region)
            },
            regionParam = regionPromptParam
        )
    }
}