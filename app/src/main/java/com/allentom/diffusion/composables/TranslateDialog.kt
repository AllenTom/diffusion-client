package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.allentom.diffusion.R
import com.allentom.diffusion.api.translate.TranslateHelper
import kotlinx.coroutines.launch
import java.lang.Exception

@Composable
fun TranslateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialText: String = ""
) {
    val context = LocalContext.current
    var inputText by remember {
        mutableStateOf(initialText)
    }
    var resultText by remember {
        mutableStateOf("")
    }
    val scope = rememberCoroutineScope()
    fun translate() {
        if (inputText.isEmpty()) {
            return
        }
        scope.launch {
            try {
                resultText = TranslateHelper.translateText(inputText).sentences.joinToString(".")

            }catch (e:Exception) {
                resultText = context.getString(R.string.translate_failed)
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxSize(),
        confirmButton = {
            Button(
                enabled = resultText.isNotBlank(),
                onClick = {
                    onConfirm(resultText)
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
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        translate()
                    }) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_translate),
                            contentDescription = "translate"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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