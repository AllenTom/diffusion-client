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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.store.Prompt
import com.allentom.diffusion.store.PromptStore
import com.allentom.diffusion.store.SavePrompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.RegionPromptParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PromptSelectDialog(
    promptList: List<Prompt>,
    title: String,
    onDismiss: () -> Unit,
    onValueChange: (List<Prompt>, RegionPromptParam?) -> Unit,
    regionParam: RegionPromptParam? = null
) {
    var selectedPromptList by remember { mutableStateOf(promptList) }
    var inputPromptText by remember { mutableStateOf("") }
    var searchResults by remember {
        mutableStateOf<List<SavePrompt>>(emptyList())
    }
    var currentSelectPromptIndex by remember {
        mutableStateOf(null as String?)
    }
    var selectIndex by remember {
        mutableStateOf(0)
    }
    var inputRegionParam by remember {
        mutableStateOf(regionParam)
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchJob: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    var editMode by remember {
        mutableStateOf(false)
    }

    fun refreshSearchResult() {
        scope.launch(Dispatchers.IO) {
            if (inputPromptText.isNotEmpty()) {
                PromptStore.searchPrompt(
                    context,
                    inputPromptText,
                    emptyList()
                )
                    .let { results ->
                        searchResults = results
                    }
            } else {
                searchResults =
                    PromptStore.getTopNPrompt(context, 10)
            }
        }
    }
    LaunchedEffect(Unit) {
        refreshSearchResult()
        if (selectedPromptList.isNotEmpty()) {
            currentSelectPromptIndex = selectedPromptList.first().randomId
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
                onValueChange(selectedPromptList, inputRegionParam)
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
                    if (inputRegionParam != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(selected = selectIndex == 2, onClick = {
                            selectIndex = 2
                        }, label = {
                            Text(stringResource(R.string.regional))
                        })
                    }
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
                            selectedPromptList.find { it.randomId == selectPromptIndex }
                                ?.let { prompt ->
                                    FilterChip(
                                        selected = currentSelectPromptIndex == prompt.randomId,
                                        onClick = {
                                        },
                                        label = {
                                            Column(
                                                modifier = Modifier.padding(4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = prompt.piority.toString())
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    Column(
                                                        modifier = Modifier.padding(4.dp)
                                                    ) {
                                                        Text(
                                                            text = prompt.getTranslationText(),
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onBackground.copy(
                                                                alpha = 0.6f
                                                            )
                                                        )
                                                        Text(prompt.text)

                                                    }

                                                }


                                            }
                                        },
                                        leadingIcon = {
                                            IconButton(
                                                onClick = {
                                                    selectedPromptList =
                                                        selectedPromptList.toMutableList()
                                                            .map {
                                                                if (it == prompt) {
                                                                    return@map it.copy(
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
                                                                    return@map it.copy(
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
                                    inputRegionParam?.let { regionParam ->
                                        if (regionParam.enable) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                            ) {
                                                for (regionIndex in 0 until regionParam.getTotalRegionCount()) {
                                                    FilterChip(
                                                        selected = prompt.regionIndex == regionIndex,
                                                        onClick = {
                                                            selectedPromptList =
                                                                selectedPromptList.toMutableList()
                                                                    .map {
                                                                        if (it == prompt) {

                                                                            return@map it.copy(
                                                                                regionIndex = regionIndex
                                                                            )
                                                                        } else {
                                                                            it
                                                                        }
                                                                    }
                                                            currentSelectPromptIndex =
                                                                prompt.copy(regionIndex = regionIndex).randomId
                                                        },
                                                        label = {
                                                            if (regionParam.useCommon && regionIndex == 0) {
                                                                Text(stringResource(R.string.common_region))
                                                            } else {
                                                                Text(
                                                                    stringResource(
                                                                        id = R.string.region,
                                                                        regionIndex.toString()
                                                                    )
                                                                )

                                                            }
                                                        })
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                            }
                                        }
                                    }

                                }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                editMode = !editMode
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
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
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (inputRegionParam != null && inputRegionParam!!.enable) {
                                for (regionIndex in 0 until inputRegionParam!!.getTotalRegionCount()) {
                                    if (inputRegionParam!!.useCommon && regionIndex == 0) {
                                        Text(text = stringResource(id = R.string.common_region))
                                    } else {
                                        Text(
                                            text = stringResource(
                                                id = R.string.region,
                                                regionIndex.toString()
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PromptEditContainer(promptList = selectedPromptList.filter {
                                        it.regionIndex == regionIndex
                                    }, onPromptClick = {
                                        currentSelectPromptIndex = it.randomId
                                    }, isItemSelected = {
                                        currentSelectPromptIndex == it.randomId
                                    }, onPromptDelete = { prompt ->
                                        if (currentSelectPromptIndex == prompt.randomId) {
                                            currentSelectPromptIndex = null
                                        }
                                        selectedPromptList =
                                            selectedPromptList.filter { it != prompt }

                                        refreshSearchResult()
                                    }, editMode = editMode
                                    )
                                }
                            } else {
                                PromptEditContainer(
                                    promptList = selectedPromptList,
                                    onPromptClick = {
                                        currentSelectPromptIndex = it.randomId
                                    },
                                    isItemSelected = {
                                        currentSelectPromptIndex == it.randomId
                                    },
                                    onPromptDelete = { prompt ->
                                        if (currentSelectPromptIndex == prompt.randomId) {
                                            currentSelectPromptIndex = null
                                        }
                                        selectedPromptList =
                                            selectedPromptList.filter { it != prompt }

                                        refreshSearchResult()
                                    },
                                    editMode = editMode
                                )
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
                                                selectedPromptList + prompt.toPrompt()
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
                    if (selectIndex == 2) {
                        inputRegionParam?.let {
                            RegionalPrompterPanel(
                                it,
                                onValueChange = { newVal ->
                                    val totalRegion = newVal.getTotalRegionCount()
                                    selectedPromptList = selectedPromptList.map { prompt ->
                                        if (prompt.regionIndex >= totalRegion) {
                                            return@map prompt.copy(regionIndex = 0)
                                        } else {
                                            return@map prompt
                                        }
                                    }
                                    inputRegionParam = newVal
                                },
                                onUseCommonChange = { newVal ->
                                    regionParam?.let { regionParam ->
                                        val totalRegion = regionParam.getTotalRegionCount()
                                        selectedPromptList = selectedPromptList.map { prompt ->
                                            if (prompt.regionIndex >= totalRegion - 1) {
                                                return@map prompt.copy(regionIndex = 0)
                                            } else {
                                                return@map prompt
                                            }
                                        }
                                        inputRegionParam =
                                            inputRegionParam?.copy(useCommon = newVal)
                                    }
                                }
                            )
                        }

                    }
                }
            }
        }
    )
}

data class Region(
    val index: Int = -1,
    val ratio: Int = 1,
    val color: Int = 1,
    var subRegions: List<Region> = emptyList(),
    val layout: String = "Column"
)

fun getRegionIndex(input: String): List<Region> {
    var root = Region(-1, 1, 0)
    var curRegionIndex = -1
    var regions = input.split(";")
    if (!input.contains(";")) {
        regions = input.split(",")
    }
    try {
        regions.forEach { regionText ->
            if (regionText.isEmpty()) {
                return@forEach
            }
            val subRegion = regionText.split(",")
            var region: Region? = null
            if (subRegion.size == 1) {
                curRegionIndex++
                region = Region(curRegionIndex, subRegion[0].toInt(), 0)
            } else {
                region = Region(
                    -1,
                    subRegion[0].toInt(),
                    0,
                    subRegion.subList(1, subRegion.size).map { subRegionText ->
                        curRegionIndex++
                        Region(
                            index = curRegionIndex,
                            ratio = subRegionText.toInt(),
                            color = 0,
                            subRegions = emptyList()
                        )
                    })
            }
            root = root.copy(subRegions = root.subRegions?.plus(region) ?: listOf(region))
        }
        return root.subRegions ?: emptyList()

    } catch (e: Exception) {
        return emptyList()
    }

    // example: 1,1,1;2,1,1
//    var inputDividerRatio = input
//    var curRegion = Region(
//        subRegions = emptyList(),
//    )
//
//    var root = Region(
//        subRegions = listOf(curRegion),
//        layout = "Row"
//    )
//
//
//    var cur = 0
//    var regionIndex = -1
//    var curDigit = ""
//    while (cur < inputDividerRatio.length) {
//        val curChar = inputDividerRatio[cur]
//        if (curChar.isDigit()) {
//            curDigit += curChar
//        }
//        if (curChar == ',') {
//            regionIndex += 1
//            val ratio = curDigit.takeIf { it.isNotEmpty() }?.toInt() ?: 1
//            curDigit = ""
//            val newRegion = Region(
//                index = regionIndex,
//                ratio = ratio,
//            )
//            curRegion.subRegions += newRegion
//        }
//        if (curChar == ';') {
//            regionIndex += 1
//            var ratioText = ""
//            curDigit = ""
//            while (true) {
//                cur += 1
//                if (cur >= inputDividerRatio.length) {
//                    break
//                }
//                val nextChar = inputDividerRatio[cur]
//                if (nextChar.isDigit()) {
//                    ratioText += nextChar
//                }
//                if (nextChar == ',') {
//                    break
//                }
//            }
//            val newRegion = Region(
//                index = regionIndex,
//                ratio = ratioText.takeIf { it.isNotEmpty() }?.toInt() ?: 1,
//            )
//            root.subRegions += newRegion
//            curRegion = newRegion
//        }
//        cur += 1
//
//    }
//    return root.subRegions ?: emptyList()

}

//fun main() {
//    val result = getRegionIndex("1;1;1;1;")
//    print(result)
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionalPrompterPanel(
    regionParam: RegionPromptParam,
    onValueChange: (RegionPromptParam) -> Unit = {},
    onUseCommonChange: (Boolean) -> Unit = {}
) {
    var selectedMode by remember {
        mutableStateOf("Columns")
    }
    var inputRegionCount by remember {
        mutableStateOf(regionParam.regionCount.toString())
    }


    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(id = R.string.enable), modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = regionParam.enable, onCheckedChange = {
            onValueChange(regionParam.copy(enable = it))
        })
    }
//    Row {
//        FilterChip(
//            selected = selectedMode == "Columns",
//            onClick = {
//                selectedMode = "Columns"
//            },
//            label = {
//                Text(text = "Columns")
//            }
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        FilterChip(
//            selected = selectedMode == "Rows",
//            onClick = {
//                selectedMode = "Rows"
//            },
//            label = {
//                Text(text = "Rows")
//            }
//        )
//    }
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = regionParam.dividerText, onValueChange = {
            onValueChange(regionParam.copy(dividerText = it))
        }, label = {
            Text(text = stringResource(R.string.region_divider_ratio))
        })
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = inputRegionCount,
        onValueChange = { inputVal ->
            inputRegionCount = inputVal
            try {
                onValueChange(regionParam.copy(regionCount = inputVal.toInt()))
            } catch (e: Exception) {

            }

        },
        label = {
            Text(text = stringResource(R.string.region_count))
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.region_usecommon), modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = regionParam.useCommon, onCheckedChange = {
            onUseCommonChange(it)
        })
    }

//    Column(
//        modifier = Modifier
//            .width(200.dp)
//            .height(200.dp)
//    ) {
//        getRegionIndex(inputDividerRatio).forEach { region ->
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(region.ratio.toFloat())
//                    .background(Color(region.color)),
//                contentAlignment = androidx.compose.ui.Alignment.Center
//            ) {
//                Box(
//                    modifier = Modifier.background(Color(0x80000000))
//                ) {
//                    if (region.subRegions.isNotEmpty()) {
//                        Row {
//                            region.subRegions.forEach { subRegion ->
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxHeight()
//                                        .weight(subRegion.ratio.toFloat())
//                                        .background(Color(subRegion.color ?: 0xFF000000.toInt())),
//                                    contentAlignment = androidx.compose.ui.Alignment.Center
//                                ) {
//                                    Box(
//                                        modifier = Modifier.background(Color(0x80000000))
//                                    ) {
//                                        Text(text = subRegion.index.toString(), color = Color.White)
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    if (region.index != -1) {
//                        Text(text = region.index.toString(), color = Color.White)
//                    }
//                }
//
//            }
//        }
//    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptEditContainer(
    promptList: List<Prompt>,
    onPromptClick: (Prompt) -> Unit = {},
    isItemSelected: (Prompt) -> Boolean = { false },
    onPromptDelete: (Prompt) -> Unit = {},
    editMode: Boolean = false
) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        promptList.forEachIndexed { index, prompt ->
            PromptChip(
                prompt = prompt,
                onClickPrompt = {
                    onPromptClick(it)
                },
                selected = isItemSelected(prompt),
                tail = {
                    if (editMode) {
                        IconButton(onClick = {
                            onPromptDelete(prompt)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                            )
                        }
                    }
                })
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
