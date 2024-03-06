package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.api.translate.TranslateHelper
import com.allentom.diffusion.api.translate.TranslateLanguages
import kotlinx.coroutines.launch
import java.lang.Exception

@Composable
fun TranslateDialog(
    onDismiss: () -> Unit,
    onConfirm: (source: String, to: String) -> Unit,
    initialText: String = "",
    initFrom: TranslateLanguages = TranslateLanguages.Auto,
    initTo: TranslateLanguages = TranslateLanguages.English,
    autoTranslate: Boolean = false
) {
    val context = LocalContext.current
    var inputText by remember {
        mutableStateOf(initialText)
    }
    var resultText by remember {
        mutableStateOf("")
    }
    var from by remember {
        mutableStateOf(initFrom)
    }
    var to by remember {
        mutableStateOf(initTo)
    }
    var isFromDropdownExpanded by remember {
        mutableStateOf(false)
    }
    var isToDropdownExpanded by remember {
        mutableStateOf(false)
    }
    var isTranslating by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    fun translate() {
        if (inputText.isEmpty()) {
            return
        }
        scope.launch {
            isTranslating = true
            try {
                resultText = TranslateHelper.translateText(
                    inputText, from, to
                ).sentences.joinToString(".")
            } catch (e: Exception) {
                resultText = context.getString(R.string.translate_failed)
            } finally {
                isTranslating = false
            }
        }
    }
    LaunchedEffect(Unit) {
        if (autoTranslate) {
            translate()
        }
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

    fun swapSrcAndDst() {
        val temp = inputText
        inputText = resultText
        resultText = temp
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxSize(),
        confirmButton = {
            Button(
                enabled = resultText.isNotBlank() && resultText != context.getString(R.string.translate_failed),
                onClick = {
                    onConfirm(initialText, resultText)
                    onDismiss()
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
                Text(stringResource(R.string.translate), modifier = Modifier.weight(1f))
            }
        },
        text = {
            Column {
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
                            swapSrcAndDst()
                        }) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_swap),
                            contentDescription = "swap"
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
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    enabled = !isTranslating,
                    value = inputText,
                    onValueChange = {
                        inputText = it
                    },
                    label = {
                        Text(stringResource(R.string.input))
                    },
                    trailingIcon = {

                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    enabled = !isTranslating,
                    value = resultText, onValueChange = {
                        resultText = it

                    }, label = {
                        Text(stringResource(R.string.result))
                    }
                )
            }

        }
    )
}