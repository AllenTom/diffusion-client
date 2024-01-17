package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.allentom.diffusion.R
import com.allentom.diffusion.store.Prompt
import com.allentom.diffusion.store.PromptStore
import com.allentom.diffusion.store.SavePrompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun PromptSelectDialog(
    promptList: List<Prompt>,
    title: String,
    onDismiss: () -> Unit,
    onValueChange: (List<Prompt>) -> Unit,
) {
    var selectedPromptList by remember { mutableStateOf(promptList) }
    var inputPromptText by remember { mutableStateOf("") }
    var searchResults by remember {
        mutableStateOf<List<SavePrompt>>(emptyList())
    }
    var currentSelectPromptIndex by remember {
        mutableStateOf(null as Int?)
    }
    var selectIndex by remember {
        mutableStateOf(0)
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchJob: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    fun refreshSearchResult() {
        scope.launch(Dispatchers.IO) {
            if (inputPromptText.isNotEmpty()) {
                PromptStore.searchPrompt(
                    context,
                    inputPromptText,
                    selectedPromptList.map { it.text })
                    .let { results ->
                        searchResults = results.filter { result ->
                            !selectedPromptList.map { it.text }.contains(result.text)
                        }
                    }
            } else {
                searchResults =
                    PromptStore.getTopNPrompt(context, 10, selectedPromptList.map { it.text })
            }
        }
    }
    LaunchedEffect(Unit) {
        refreshSearchResult()
        if (selectedPromptList.isNotEmpty()) {
            currentSelectPromptIndex = 0
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
                onValueChange(selectedPromptList)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(title, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Row{
                    FilterChip(selected = selectIndex == 0, onClick = {
                        selectIndex = 0
                    }, label = {
                        Text(stringResource(R.string.current))
                    })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = selectIndex == 1, onClick = {
                        selectIndex = 1
                    }, label = {
                        Text(stringResource(R.string.library))
                    })
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (selectIndex == 0) {
                        val selectPromptIndex = currentSelectPromptIndex
                        if (selectPromptIndex != null) {
                            selectedPromptList.getOrNull(selectPromptIndex)?.let { prompt ->
                                FilterChip(
                                    selected = true, onClick = {
                                    }, label = {
                                        Text("${prompt.text} (${prompt.piority})")
                                    },
                                    leadingIcon = {
                                        IconButton(
                                            onClick = {
                                                selectedPromptList =
                                                    selectedPromptList.toMutableList()
                                                        .map {
                                                            if (it == prompt) {
                                                                return@map Prompt(
                                                                    text = it.text,
                                                                    piority = it.piority + 1
                                                                )
                                                            } else {
                                                                it
                                                            }
                                                        }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = null,
                                            )

                                        }
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                selectedPromptList =
                                                    selectedPromptList.toMutableList()
                                                        .map {
                                                            if (it == prompt) {
                                                                if (prompt.piority == 0) {
                                                                    return@map it
                                                                }
                                                                return@map Prompt(
                                                                    text = it.text,
                                                                    piority = it.piority - 1
                                                                )
                                                            } else {
                                                                it
                                                            }
                                                        }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                currentSelectPromptIndex = null
                                selectedPromptList = emptyList()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                )
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                FlowRow {
                                    selectedPromptList.forEachIndexed { index, prompt ->
                                        FilterChip(
                                            onClick = {
                                                currentSelectPromptIndex = index
                                            },
                                            selected = true,
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    modifier = Modifier.clickable {
                                                        if (currentSelectPromptIndex == index) {
                                                            currentSelectPromptIndex = null
                                                        }
                                                        selectedPromptList =
                                                            selectedPromptList.filter { it != prompt }

                                                        refreshSearchResult()
                                                    }
                                                )
                                            },
                                            label = {
                                                Text(prompt.text)
                                            },
                                            leadingIcon = {
                                                Text(prompt.piority.toString())
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (selectIndex == 1) {
                        OutlinedTextField(
                            value = inputPromptText,
                            onValueChange = { newValue ->
                                inputPromptText = newValue
                                searchJob?.cancel()
                                searchJob = coroutineScope.launch {
                                    delay(500L)  // delay for 300ms
                                    refreshSearchResult()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        selectedPromptList =
                                            selectedPromptList + Prompt(inputPromptText, 0)
                                        inputPromptText = ""
                                    }
                                )
                            },

                            )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(searchResults.size) {
                                val prompt = searchResults[it]
                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            selectedPromptList =
                                                selectedPromptList + Prompt(prompt.text, 0)
                                            refreshSearchResult()
                                        }
                                        .padding(4.dp)
                                        .fillMaxWidth()

                                ) {
                                    Text(text = prompt.text)
                                    if (prompt.text != prompt.nameCn) {
                                        Text(text = prompt.nameCn)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}


