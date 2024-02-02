package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextListPickUpItem(
    label: String,
    value: List<String>?,
    title: String = label,
    options: List<String>,
    onValueChange: (List<String>) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedOptions by remember { mutableStateOf(value ?: emptyList()) }
    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = label) },
        supportingContent = {
            if (value.isNullOrEmpty()) {
                Text(text = stringResource(R.string.select_a_value))
            } else {
                Text(text = value.joinToString(","))
            }
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        selectedOptions.forEach {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f)
                                ){
                                    Text(
                                        text = it,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,

                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            selectedOptions -= it
                                        }
                                )

                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)

                    ) {
                        items(options.size) { index ->
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedOptions += options[index]
                                    },
                                headlineContent = {
                                    Text(
                                        text = options[index],
                                    )
                                }
                            )
                        }
                    }
                }


            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(selectedOptions)
                    showDialog = false
                }) {
                    Text(text = stringResource(R.string.confirm))
                }

            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}