package com.allentom.diffusion.composables

import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SwitchOptionItem(
    label: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit = {}
) {


    ListItem(
        headlineContent = { Text(text = label) },
        trailingContent = {
            Switch(
                checked = value,
                onCheckedChange = {
                    onValueChange(it)
                }
            )
        },
    )
}