package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.store.prompt.LoraPrompt
import com.allentom.diffusion.store.prompt.PromptStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class
)
@Composable
fun LoraSelectDialog(
    loraList: List<Lora>,
    title: String,
    onDismiss: () -> Unit,
    onValueChange: (List<LoraPrompt>) -> Unit,
    value: List<LoraPrompt>
) {
    val useDevice = DetectDeviceType()
    var selectedLoraList by remember { mutableStateOf(value) }
    var inputLoraText by remember { mutableStateOf("") }
    var searchResults by remember {
        mutableStateOf<List<Lora>>(emptyList())
    }
    var currentSelectLoraIndex by remember {
        mutableStateOf(null as Int?)
    }
    var currentIndex by remember {
        mutableStateOf(0)
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columns = screenWidthDp / 180

    fun refreshSearchResult() {
        searchResults = if (inputLoraText.isNotEmpty()) {
            loraList.filter { it.name.contains(inputLoraText) && selectedLoraList.none { used -> used.name == it.name } }
        } else {
            loraList.filter { selectedLoraList.none { used -> used.name == it.name } }
        }
    }

    fun onAddLora(lora: Lora) {
        scope.launch(Dispatchers.IO) {
            val loraInfo =
                PromptStore.getLoraPromptWithRelate(context, lora.entity?.id ?: 0) ?: return@launch
            selectedLoraList = selectedLoraList + LoraPrompt(
                loraInfo.loraPrompt.loraPromptId,
                lora.name,
                1f,
                title = lora.entity?.title ?: "",
                previewPath = lora.entity?.previewPath,
                prompts = emptyList(),
                triggerText = loraInfo.triggerText.map { it.toPrompt() },
            )
            refreshSearchResult()
        }

    }
    LaunchedEffect(Unit) {
        refreshSearchResult()
        if (selectedLoraList.isNotEmpty()) {
            currentSelectLoraIndex = 0
        }
        scope.launch(Dispatchers.IO) {
            selectedLoraList = selectedLoraList.map {
                val loraInfo = PromptStore.getLoraPromptWithRelate(context, it.id) ?: return@map it
                return@map it.copy(
                    triggerText = loraInfo.triggerText.map { it.toPrompt() }
                )
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onValueChange(selectedLoraList)
            }) {
                Text(stringResource(id = R.string.apply))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        title = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(title)
                Spacer(modifier = Modifier.weight(1f))
                FilterChip(selected = currentIndex == 0, onClick = {
                    currentIndex = 0
                }, label = {
                    Text(stringResource(id = R.string.current))
                })
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(selected = currentIndex == 1, onClick = {
                    currentIndex = 1
                }, label = {
                    Text(stringResource(id = R.string.library))
                })
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
                    if (currentIndex == 0) {
                        currentSelectLoraIndex?.let { selectIndex ->
                            selectedLoraList.getOrNull(selectIndex)?.let { lora ->
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column {
                                            Text(
                                                text = lora.title.ifEmpty {
                                                    lora.name
                                                },
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Column {
                                                Text("Weight:${lora.weight}")
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Slider(
                                                    value = lora.weight,
                                                    valueRange = 0f..2f,
                                                    onValueChange = { newVal ->
                                                        selectedLoraList =
                                                            selectedLoraList.toMutableList()
                                                                .map {
                                                                    if (it == lora) {
                                                                        return@map lora.copy(weight = Util.formatFloatByBaseFloat(newVal,0.01f))
                                                                    } else {
                                                                        it
                                                                    }
                                                                }
                                                    })
                                            }
                                        }

                                    }
                                    lora.previewPath?.let { previewPath ->
                                        Box(
                                            modifier = Modifier
                                                .height(120.dp)
                                                .width(120.dp)
                                        ) {
                                            AsyncImage(
                                                model = previewPath,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                    }
                                }
                                if (lora.triggerText.isNotEmpty()) {
                                    FlowRow(
                                        modifier = Modifier
                                            .heightIn(max = 300.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        lora.triggerText.forEach {
                                            PromptChip(
                                                prompt = it,
                                                selected = lora.isTriggered(it),
                                                onClickPrompt = {
                                                    if (lora.isTriggered(it)) {
                                                        selectedLoraList =
                                                            selectedLoraList.map { curLora ->
                                                                if (curLora.name == lora.name) {
                                                                    return@map curLora.copy(
                                                                        prompts = curLora.prompts.filter { prompt ->
                                                                            prompt.text != it.text
                                                                        }
                                                                    )
                                                                }
                                                                curLora
                                                            }
                                                    } else {
                                                        selectedLoraList =
                                                            selectedLoraList.map { curLora ->
                                                                if (curLora.name == lora.name) {
                                                                    return@map curLora.copy(
                                                                        prompts = curLora.prompts + it
                                                                    )
                                                                }
                                                                curLora
                                                            }
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                    }
                                }

                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            items(selectedLoraList.size) { index ->
                                val lora = selectedLoraList[index]
                                ListItem(
                                    modifier = Modifier.clickable {
                                        currentSelectLoraIndex = index
                                    },
                                    trailingContent = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.clickable {
                                                if (currentSelectLoraIndex == index) {
                                                    currentSelectLoraIndex = null
                                                }
                                                selectedLoraList =
                                                    selectedLoraList.filter { it != lora }

                                                refreshSearchResult()
                                            }
                                        )
                                    },
                                    headlineContent = {
                                        if (lora.title.isNotEmpty()) {
                                            Text(lora.title)
                                        } else {
                                            Text(lora.name)
                                        }
                                    },
                                    leadingContent = {
                                        Text(String.format("%.1f", lora.weight))
                                    }
                                )
                            }
                        }
                    }
                    if (currentIndex == 1) {
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
                            LoraList(loraList = searchResults) {
                                onAddLora(it)
                            }
                        }else{
                            LoraGrid(
                                columnCount = columns,
                                loraList = searchResults,
                            ) {
                                onAddLora(it)
                            }
                        }
                    }
                }
            }
        }
    )
}


