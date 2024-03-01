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
import com.allentom.diffusion.api.entity.Model

@Composable
fun ModelSelectOptionItem(
    label: String,
    value: String,
    title: String = label,
    modelList: List<Model>,
    onValueChange: (Model) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = label) },
        supportingContent = {
            Text(text = value)
        }
    )
    if (showDialog) {
        ModelSelectDialog(
            modelList = modelList,
            title = title,
            onDismiss = { showDialog = false },
            onValueChange = {
                showDialog = false
                onValueChange(it)
            }
        )
    }
}