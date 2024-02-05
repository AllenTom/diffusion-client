package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.api.entity.Embedding
import com.allentom.diffusion.store.prompt.EmbeddingPrompt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmbeddingSelectOptionItem(
    label: String,
    value: List<EmbeddingPrompt>,
    title: String = label,
    embeddingList: Map<String, Embedding>,
    onValueChange: (List<EmbeddingPrompt>) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(text = label) },
        supportingContent = {
            FlowRow {
                value.forEach {
                    AssistChip(
                        onClick = {

                        },
                        label = {
                            Text(text = it.text)
                        },
                        leadingIcon = {
                            if (it.piority != 0) {
                                Text(text = it.piority.toString())
                            }
                        },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    )

    if (showDialog) {
        EmbeddingSelectDialog(
            embeddingList = embeddingList,
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