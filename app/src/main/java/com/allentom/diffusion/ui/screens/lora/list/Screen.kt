package com.allentom.diffusion.ui.screens.lora.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.LoraGrid
import com.allentom.diffusion.composables.MatchOptionDialog
import com.allentom.diffusion.composables.gridCountForDeviceWidth
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.prompt.PromptStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.lora.detail.LoraDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoraListScreen(navController: NavController) {
    val modelIcon = ImageVector.vectorResource(id = R.drawable.ic_model)
    var loraList by remember {
        mutableStateOf(DrawViewModel.loraList)
    }
    var itemImageFit by remember {
        mutableStateOf(AppConfigStore.config.loraViewDisplayMode)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isMatchDialogOpen by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch(Dispatchers.IO) {
            DrawViewModel.loraList = DrawViewModel.loadLora(context)
            loraList = DrawViewModel.loraList
        }
    }
    LaunchedEffect(Unit) {
        refresh()
    }
    var isActionMenuShow by remember {
        mutableStateOf(false)
    }
    var totalLora by remember {
        mutableStateOf(1)
    }
    var currentLora by remember {
        mutableStateOf(0)
    }
    var isMatchAll by remember {
        mutableStateOf(false)
    }
    val imageFitIcon = ImageVector.vectorResource(id = R.drawable.ic_image_fit)
    val imageCropIcon = ImageVector.vectorResource(id = R.drawable.ic_image_crop)
    fun onChangeImageFit(newImageFit: String) {
        itemImageFit = newImageFit
        AppConfigStore.updateAndSave(context) {
            it.copy(loraViewDisplayMode = newImageFit)
        }
    }

    fun matchAll(isSkipExist: Boolean) {
        if (isMatchAll) {
            return
        }
        scope.launch(Dispatchers.IO) {
            isMatchAll = true
            val list = PromptStore.getAllLoraPrompt(context).map { it.toPrompt() }.filter {
                DrawViewModel.loraList.any { lora -> lora.name == it.name }
            }
            totalLora = list.size
            list.forEach { loraPrompt ->
                try {
                    if (isSkipExist && loraPrompt.civitaiId != null) {
                        currentLora++
                        return@forEach
                    }
                    PromptStore.matchLoraByModelId(context, loraPrompt.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                currentLora++
            }
            refresh()
            isMatchAll = false
        }
    }

    if (isMatchAll) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(text = stringResource(R.string.matching_lora))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.match_lora_progress, currentLora, totalLora))
                    Text(text = loraList[currentLora].name)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = currentLora.toFloat() / totalLora.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = { },
            dismissButton = { }
        )
    }

    if (isMatchDialogOpen) {
        MatchOptionDialog(onDismiss = {
            isMatchDialogOpen = false
        }) { isSkipExist ->
            matchAll(isSkipExist)
        }
    }


    val columns = gridCountForDeviceWidth(itemWidth = 180)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lora_list_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    if (itemImageFit == "Fit") {
                        IconButton(
                            onClick = {
                                onChangeImageFit("Crop")
                            }
                        ) {
                            Icon(
                                imageCropIcon,
                                contentDescription = "Menu",
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                onChangeImageFit("Fit")
                            }
                        ) {
                            Icon(
                                imageFitIcon,
                                contentDescription = "Menu",
                            )
                        }
                    }
                    IconButton(onClick = {
                        isActionMenuShow = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                        )

                    }
                    if (AppConfigStore.config.enablePlugin) {
                        DropdownMenu(
                            expanded = isActionMenuShow,
                            onDismissRequest = { isActionMenuShow = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.match_all_lora)) },
                                onClick = {
                                    isActionMenuShow = false
                                    isMatchDialogOpen = true
                                }
                            )
                        }
                    }

                }
            )
        },
    ) { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LoraGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                columnCount = columns,
                loraList = loraList,
                itemImageFit = itemImageFit,
                onCLick = {
                    LoraDetailViewModel.asNew()
                    navController.navigate(
                        Screens.LoraPromptDetail.route.replace(
                            "{id}",
                            it.entity?.id.toString()
                        )
                    )
                })
            DrawBar()
        }

    }
}