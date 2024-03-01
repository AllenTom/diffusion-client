package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.allentom.diffusion.R
import com.allentom.diffusion.api.entity.Model

@Composable
fun ModelSelectDialog(
    modelList: List<Model>,
    title: String,
    onDismiss: () -> Unit,
    onValueChange: (Model) -> Unit,
) {
    val useDevice = DetectDeviceType()
    var inputLoraText by remember { mutableStateOf("") }
    var searchResults by remember {
        mutableStateOf<List<Model>>(emptyList())
    }
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columns = screenWidthDp / 180

    fun refreshSearchResult() {
        searchResults = if (inputLoraText.isNotEmpty()) {
            modelList.filter { it.title.contains(inputLoraText) }
        } else {
            modelList
        }
    }

    LaunchedEffect(Unit) {
        refreshSearchResult()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {

        },
        dismissButton = {

        },
        title = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(title)
                Spacer(modifier = Modifier.weight(1f))
            }

        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxSize(),
        text = {
            Box {
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = inputLoraText,
                        onValueChange = {
                            inputLoraText = it
                            refreshSearchResult()
                        },
                        placeholder = {
                            Text(stringResource(id = R.string.search))
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (useDevice == DeviceType.Phone) {
                        ModelList(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            modelList = searchResults,
                            onSelect = {
                                onValueChange(it)
                                onDismiss()
                            }
                        )
                    }else{
                        ModelGrid(
                            columnCount = columns,
                            modelList = searchResults,
                        ) {
                            onValueChange(it)
                            onDismiss()
                        }
                    }
                }
            }
        }
    )
}


