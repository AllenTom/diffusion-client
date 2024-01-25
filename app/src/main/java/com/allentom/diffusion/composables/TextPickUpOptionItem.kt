package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R

@Composable
fun TextPickUpItem(
    label: String,
    value: String?,
    title: String = label,
    options: List<String>,
    onValueChange: (String) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = label) },
        supportingContent = {
            Text(text = value.let {
                it ?: stringResource(R.string.select_a_value)
            })
        }
    )

    if (showDialog) {
        AlertDialog(
            modifier = Modifier
                .heightIn(max = 500.dp)
                .fillMaxWidth(),
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                LazyColumn {
                    items(options.size) { index ->
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onValueChange(options[index])
                                    showDialog = false
                                },
                            headlineContent = {
                                Text(
                                    text = options[index],
                                    color = if (options[index] == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }
            },
            confirmButton = {

            },
            dismissButton = {

            }
        )
    }
}