package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.allentom.diffusion.R
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.store.prompt.PromptStore
import com.allentom.diffusion.store.prompt.PromptStyle
import com.allentom.diffusion.store.prompt.SavePrompt
import com.allentom.diffusion.store.prompt.StyleWithPrompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.RegionPromptParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun StyleEditDialog(
    stylePrompt: PromptStyle,
    title: String = stylePrompt.name,
    onDismiss: () -> Unit,
    onValueChange: (PromptStyle) -> Unit,
) {
    var inputStylePrompt by remember {
        mutableStateOf(stylePrompt)
    }


    var selectIndex by remember {
        mutableStateOf(0)
    }

    var selectMode by remember {
        mutableStateOf(false)
    }
    var selectedPromptIds by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var deleteSelectedPromptConfirmDialog by remember {
        mutableStateOf(false)
    }

    fun deleteSelectedPrompt() {
        inputStylePrompt = inputStylePrompt.copy(
            prompts = inputStylePrompt.prompts.filter {
                it.randomId !in selectedPromptIds
            }
        )
    }


    fun selectAllPrompt() {
        selectedPromptIds = inputStylePrompt.prompts.map { it.randomId }
    }

    fun onCloseSelectMode() {
        selectMode = false
        selectedPromptIds = emptyList()
    }

    if (deleteSelectedPromptConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                deleteSelectedPromptConfirmDialog = false
            },
            title = {
                Text(text = stringResource(R.string.delete_confirm))
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.are_you_sure_to_delete_selected_prompts,
                        selectedPromptIds.size
                    )
                )
            },
            confirmButton = {
                Button(onClick = {
                    deleteSelectedPrompt()
                    onCloseSelectMode()
                    deleteSelectedPromptConfirmDialog = false
                }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                Button(onClick = {
                    deleteSelectedPromptConfirmDialog = false
                }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
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
                onValueChange(inputStylePrompt)
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
                Row {
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
                        OutlinedTextField(
                            value = inputStylePrompt.name,
                            onValueChange = {
                                inputStylePrompt = inputStylePrompt.copy(
                                    name = it
                                )
                            },
                            label = {
                                Text("Name")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (!selectMode) {
                                IconButton(onClick = {
                                    selectMode = !selectMode
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                    )
                                }
                            }
                            if (selectMode) {
                                IconButton(
                                    enabled = selectedPromptIds.isNotEmpty(),
                                    onClick = {
                                        deleteSelectedPromptConfirmDialog = true
                                    }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    selectAllPrompt()
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_select_all),
                                        contentDescription = null,
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    onCloseSelectMode()
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_unselect_all),
                                        contentDescription = null,
                                    )
                                }
                                IconButton(onClick = {
                                    selectMode = !selectMode
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                    )
                                }
                            }

                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            PromptEditContainer(
                                promptList = inputStylePrompt.prompts,
                                onPromptClick = {
                                    if (selectMode) {
                                        if (it.randomId in selectedPromptIds) {
                                            selectedPromptIds = selectedPromptIds.filter {id ->
                                                id != it.randomId
                                            }
                                        } else {
                                            selectedPromptIds = selectedPromptIds + it.randomId
                                        }
                                    }

                                },
                                isItemSelected = {
                                    it.randomId in selectedPromptIds
                                },
                            )

                        }
                    }
                    if (selectIndex == 1) {
                        PromptLibraryPanel(onAddPrompt = {
                            inputStylePrompt = inputStylePrompt.copy(
                                prompts = inputStylePrompt.prompts + it
                            )
                        })
                    }
                }
            }

        }
    )
}
