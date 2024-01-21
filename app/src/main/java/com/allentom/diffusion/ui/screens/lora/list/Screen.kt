package com.allentom.diffusion.ui.screens.lora.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.extension.thenIf
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.LoraPrompt
import com.allentom.diffusion.store.PromptStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.lora.detail.LoraDetailViewModel
import com.allentom.diffusion.ui.screens.model.detail.ModelDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoraListScreen(navController: NavController) {
    val modelIcon = ImageVector.vectorResource(id = R.drawable.ic_model)
    var loraList by remember {
        mutableStateOf(emptyList<LoraPrompt>())
    }
    var itemImageFit by remember {
        mutableStateOf(AppConfigStore.config.loraViewDisplayMode)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun refresh() {
        scope.launch(Dispatchers.IO) {
            loraList = PromptStore.getAllLoraPrompt(context).map { it.toPrompt() }.filter {
                DrawViewModel.loraList.any { lora -> lora.name == it.name }
            }
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

    fun matchAll() {
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

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columns = screenWidthDp / 180
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
                                    matchAll()
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
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(loraList.size) { idx ->
                    val prompt = loraList[idx]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                            }
                            .combinedClickable(
                                onClick = {
                                    LoraDetailViewModel.asNew()
                                    navController.navigate(
                                        Screens.LoraPromptDetail.route.replace(
                                            "{id}",
                                            prompt.id.toString()
                                        )
                                    )

                                }
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .height(220.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .thenIf(itemImageFit == "Fit", Modifier.blur(16.dp))
                            ) {
                                if (prompt.previewPath != null) {
                                    AsyncImage(
                                        model = prompt.previewPath,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(150.dp)
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {

                                    if (prompt.previewPath != null) {
                                        if (itemImageFit == "Fit") {
                                            AsyncImage(
                                                model = prompt.previewPath,
                                                contentDescription = null,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        }
                                    } else {
                                        Icon(
                                            modelIcon,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(48.dp)
                                                .height(48.dp),

                                            )

                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        )
                                        .padding(8.dp),
                                ) {
                                    Column {
                                        Text(
                                            text = prompt.title.ifBlank { prompt.name },
                                            fontSize = 16.sp,
                                            maxLines = 2,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }

                            }

                        }

                    }
                }

            }
            DrawBar()
        }

    }
}