package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.allentom.diffusion.R
import com.allentom.diffusion.api.entity.Embedding
import com.allentom.diffusion.store.prompt.EmbeddingPrompt
import kotlin.math.min


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun EmbeddingSelectDialog(
    embeddingList: Map<String, Embedding>,
    title: String,
    onDismiss: () -> Unit,
    onValueChange: (List<EmbeddingPrompt>) -> Unit,
    value: List<EmbeddingPrompt>
) {
    var selectedEmbeddingList by remember { mutableStateOf(value) }
    var inputEmbeddingText by remember { mutableStateOf("") }
    var searchResults by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var currentSelectEmbeddingIndex by remember {
        mutableStateOf(null as Int?)
    }
    var currentIndex by remember {
        mutableStateOf(0)
    }

    fun refreshSearchResult() {
        if (inputEmbeddingText.isNotEmpty()) {
            searchResults =
                embeddingList.keys.filter { it.contains(inputEmbeddingText) }

        } else {
            searchResults =
                embeddingList.keys.toList().subList(0, min(10, embeddingList.keys.toList().size))
        }
    }
    LaunchedEffect(Unit) {
        refreshSearchResult()
        if (selectedEmbeddingList.isNotEmpty()) {
            currentSelectEmbeddingIndex = 0
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxSize(),
        confirmButton = {
            Button(onClick = {
                onValueChange(selectedEmbeddingList)
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Row {
                Text(title, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(selected = currentIndex == 0, onClick = {
                    currentIndex = 0
                }, label = {
                    Text(stringResource(R.string.current))
                })
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(selected = currentIndex == 1, onClick = {
                    currentIndex = 1
                }, label = {
                    Text(stringResource(R.string.library))
                })

            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (currentIndex == 0) {
                        LazyColumn(
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f)
                        ) {
                            items(selectedEmbeddingList.size) { index ->
                                val embedding = selectedEmbeddingList[index]
                                ListItem(
                                    modifier = Modifier.clickable {
                                        currentSelectEmbeddingIndex = index
                                    },
                                    trailingContent = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.clickable {
                                                if (currentSelectEmbeddingIndex == index) {
                                                    currentSelectEmbeddingIndex = null
                                                }
                                                selectedEmbeddingList =
                                                    selectedEmbeddingList.filter { it != embedding }

                                                refreshSearchResult()
                                            }
                                        )
                                    },
                                    headlineContent = {
                                        Text(embedding.text)
                                    },
                                    leadingContent = {
                                        Text(embedding.piority.toString())
                                    }
                                )
                            }
                        }
                    }
                    if (currentIndex == 1) {
                        OutlinedTextField(
                            value = inputEmbeddingText,
                            modifier = Modifier.fillMaxWidth(),
                            onValueChange = {
                                inputEmbeddingText = it
                                refreshSearchResult()
                            })
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(searchResults.size) {
                                val embedding = searchResults[it]
                                ListItem(
                                    headlineContent = {
                                        Text(embedding)
                                    },
                                    modifier = Modifier.clickable {
                                        selectedEmbeddingList =
                                            selectedEmbeddingList + EmbeddingPrompt(embedding, 0)
                                        inputEmbeddingText = ""
                                        refreshSearchResult()
                                    },

                                    )
                            }
                        }
                    }
                }
            }
        }
    )
}


