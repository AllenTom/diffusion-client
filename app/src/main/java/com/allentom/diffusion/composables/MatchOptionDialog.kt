package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
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
fun MatchOptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (skipExist: Boolean) -> Unit,
) {
    var skipExist by remember {
        mutableStateOf(true)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onConfirm(skipExist)
                onDismiss()
            }) {
                Text(stringResource(R.string.match))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        title = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.match_options))
            }

        },
        text = {
            Column {
                Row {
                    Text(stringResource(R.string.skip_already_matched), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = skipExist, onCheckedChange = {
                        skipExist = it
                    })
                }
            }

        })
}