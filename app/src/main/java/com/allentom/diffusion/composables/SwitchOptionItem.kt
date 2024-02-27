package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.extension.thenIf

@Composable
fun SwitchOptionItem(
    label: String,
    value: Boolean,
    fullWidth: Boolean = true,
    onValueChange: (Boolean) -> Unit = {}
) {
    if (fullWidth) {
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
    } else {
        Box(
            modifier = Modifier
                .thenIf(fullWidth, Modifier.fillMaxWidth())
                .clickable {
                    onValueChange(!value)
                }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = label)
                Text(text = if (value) "On" else "Off", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
        }
    }

}