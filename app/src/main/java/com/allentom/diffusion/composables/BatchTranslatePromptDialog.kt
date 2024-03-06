package com.allentom.diffusion.composables

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.api.translate.TranslateHelper
import com.allentom.diffusion.api.translate.TranslateLanguages
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.prompt.Prompt
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchTranslatePromptDialog(
    onDismiss: () -> Unit,
    inputPrompts: List<Prompt>,
    onUpdated: (List<Prompt>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var promptList by remember {
        mutableStateOf(inputPrompts)
    }
    var isTranslating by remember {
        mutableStateOf(false)
    }
    var from by remember {
        mutableStateOf(TranslateLanguages.English)
    }
    var to by remember {
        mutableStateOf(AppConfigStore.config.preferredLanguage)
    }
    var isFromDropdownExpanded by remember {
        mutableStateOf(false)
    }
    var isToDropdownExpanded by remember {
        mutableStateOf(false)
    }
    var onlyTranslateWithoutTranslated by remember {
        mutableStateOf(true)
    }
    var selectMode by remember {
        mutableStateOf(false)
    }
    var selectedPromptIds by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    fun onSelectAll() {
        selectedPromptIds = promptList.map { it.randomId }
    }

    fun onExitSelectMode() {
        selectMode = false
        selectedPromptIds = emptyList()
    }

    fun confirmSelect() {
        val promptsToUpdate = promptList.filter {
            selectedPromptIds.contains(it.randomId)

        }
        onUpdated(promptsToUpdate)
        onDismiss()
    }

    fun swap() {
        if (from == TranslateLanguages.Auto) {
            from = to
        } else {
            val temp = from
            from = to
            to = temp
        }
    }

    fun translate() {
        scope.launch {
            isTranslating = true
            promptList = promptList.map {
                if (it.translation != it.text && onlyTranslateWithoutTranslated) {
                    return@map it
                }
                try {
                    val result = TranslateHelper.translateText(it.text, from, to)
                    it.copy(translation = result.sentences.joinToString("."))
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@map it
                }
            }
            isTranslating = false
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxSize(),
        confirmButton = {
            if (selectMode) {
                Button(
                    onClick = {
                        confirmSelect()
                    }) {
                    Text(stringResource(R.string.update_selected_prompts, selectedPromptIds.size))
                }
            } else {
                Button(
                    onClick = {
                        onUpdated(promptList)
                        onDismiss()
                    }) {
                    Text(stringResource(R.string.confirm))
                }
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
                Text(stringResource(R.string.translate), modifier = Modifier.weight(1f))
            }
        },
        text = {
            Column {
                if (selectMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.select_prompts_count,
                                selectedPromptIds.size
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            onSelectAll()
                        }) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_select_all),
                                contentDescription = "select all"
                            )
                        }
                        IconButton(onClick = {
                            onExitSelectMode()
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "exit select mode"
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isTranslating,
                                onClick = {
                                    isFromDropdownExpanded = !isFromDropdownExpanded
                                }
                            ) {
                                Text(ConstValues.TranslateLangs[from] ?: from.name)
                            }
                            DropdownMenu(
                                modifier = Modifier.height(400.dp),
                                expanded = isFromDropdownExpanded, onDismissRequest = {
                                    isFromDropdownExpanded = false
                                }) {
                                TranslateHelper.getFromLanguage().forEach {
                                    DropdownMenuItem(
                                        text = {
                                            Text(ConstValues.TranslateLangs[it] ?: it.name)
                                        }, onClick = {
                                            from = it
                                            isFromDropdownExpanded = false
                                        })
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(
                            enabled = !isTranslating,
                            onClick = {
                                swap()
                            }) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_swap_horiz),
                                contentDescription = "swap"
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isTranslating,
                                onClick = {
                                    isToDropdownExpanded = !isToDropdownExpanded
                                }
                            ) {
                                Text(ConstValues.TranslateLangs[to] ?: to.name)
                            }
                            DropdownMenu(
                                modifier = Modifier.height(400.dp),
                                expanded = isToDropdownExpanded, onDismissRequest = {
                                    isToDropdownExpanded = false
                                }) {
                                TranslateHelper.getToLanguage().forEach {
                                    DropdownMenuItem(
                                        text = {
                                            Text(ConstValues.TranslateLangs[it] ?: it.name)
                                        }, onClick = {
                                            to = it
                                            isToDropdownExpanded = false
                                        }
                                    )
                                }

                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        IconButton(
                            enabled = !isTranslating,
                            onClick = {
                                selectMode = true
                            }) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "select mode"
                            )
                        }
                        IconButton(
                            enabled = !isTranslating,
                            onClick = {
                                translate()
                            }) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_translate),
                                contentDescription = "translate"
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.only_translate_without_translated),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            modifier = Modifier.scale(0.6f),
                            checked = onlyTranslateWithoutTranslated,
                            onCheckedChange = {
                                onlyTranslateWithoutTranslated = it
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    promptList.forEach {
                        PromptChip(
                            prompt = it,
                            onlyShowTranslation = false,
                            selected = selectMode && selectedPromptIds.contains(it.randomId),
                            onClickPrompt = {
                                if (selectMode) {
                                    if (selectedPromptIds.contains(it.randomId)) {
                                        selectedPromptIds =
                                            selectedPromptIds.filter { id -> id != it.randomId }
                                    } else {
                                        selectedPromptIds = selectedPromptIds + it.randomId
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}