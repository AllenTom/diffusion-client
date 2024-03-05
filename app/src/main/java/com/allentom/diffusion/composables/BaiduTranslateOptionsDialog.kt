package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R

@Composable
fun BaiduTranslateOptionsDialog(
    initialAppKey: String,
    initialSecretKey: String,
    onDismiss: () -> Unit,
    onConfirm: (appKey: String, secretKey: String) -> Unit
) {
    var appKey by remember {
        mutableStateOf(initialAppKey)
    }
    var secretKey by remember {
        mutableStateOf(initialSecretKey)
    }

    fun validate(): Boolean {
        return appKey.isNotBlank() && secretKey.isNotBlank()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                enabled = validate(),
                onClick = {
                    onConfirm(appKey, secretKey)
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
                Text(stringResource(R.string.baidu_translate_option_dialog_title),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = appKey,
                    onValueChange = {
                        appKey = it
                    },
                    label = {
                        Text(stringResource(R.string.baidu_app_key))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = secretKey,
                    onValueChange = {
                        secretKey = it
                    },
                    label = {
                        Text(stringResource(R.string.baidu_secret_key))
                    }
                )
            }

        }
    )
}
