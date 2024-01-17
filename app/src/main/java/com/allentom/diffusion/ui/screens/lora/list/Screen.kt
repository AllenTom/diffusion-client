package com.allentom.diffusion.ui.screens.lora.list

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.LoraPrompt
import com.allentom.diffusion.store.PromptStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.lora.detail.LoraDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoraListScreen(navController: NavController) {
    var loraList by remember {
        mutableStateOf(emptyList<LoraPrompt>())
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun refresh(){
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
                items(loraList.size) { index ->
                    val prompt = loraList[index]
                    Card(
                        modifier = Modifier
                            .height(200.dp)
                            .clickable {
                                LoraDetailViewModel.asNew()
                                navController.navigate(
                                    Screens.LoraPromptDetail.route.replace(
                                        "{id}",
                                        prompt.id.toString()
                                    )
                                )
                            }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (prompt.previewPath != null) {
                                    AsyncImage(
                                        model = prompt.previewPath,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(40.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                            ) {
                                Text(
                                    text = prompt.title.ifBlank { prompt.name },
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

            }
            DrawBar()
        }

    }
}