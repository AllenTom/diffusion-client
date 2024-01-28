package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.R

@Composable
fun TextAreaOptionItem(
    label: String,
    value: String,
    title: String = label,
    onValueChange: (String) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf(value) }

    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = label) },
        supportingContent = { Text(text = value.toString()) }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(stringResource(R.string.enter_text)) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    onValueChange(inputText)
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}