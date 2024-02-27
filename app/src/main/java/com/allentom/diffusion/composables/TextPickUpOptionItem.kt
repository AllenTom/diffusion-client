package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.R
import com.allentom.diffusion.extension.thenIf

@Composable
fun TextPickUpItem(
    label: String,
    value: String?,
    title: String = label,
    options: List<String>,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = true,
    onGetDisplayValue: (index: Int, value: String) -> String = { _, newVal -> newVal },
    onValueChange: (String) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    OptionDisplay(
        label = label,
        value = value.toString(),
        modifier = modifier,
        fullWidth = fullWidth,
    ) {
        showDialog = true
    }
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
                                    text = onGetDisplayValue(index, options[index]),
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