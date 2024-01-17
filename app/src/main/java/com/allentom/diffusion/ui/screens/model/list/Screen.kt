package com.allentom.diffusion.ui.screens.model.list

import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.ActionItem
import com.allentom.diffusion.composables.BottomActionSheet
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.MatchOptionDialog
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.ModelEntity
import com.allentom.diffusion.store.ModelStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.model.detail.ModelDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModelListScreen(navController: NavController) {
    var modelList by remember {
        mutableStateOf<List<ModelEntity>>(emptyList())
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    fun refresh() {
        scope.launch(Dispatchers.IO) {
            val result = getApiClient().getModels()
            result.body()?.let {
                DrawViewModel.models = it
            }
            modelList = ModelStore.getAll(context).filter {
                DrawViewModel.models.any { model -> model.modelName == it.name || model.title == it.name }
            }
        }
    }
    LaunchedEffect(Unit) {
        refresh()
    }
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columns = screenWidthDp / 180
    var actionMenuDisplay by remember { mutableStateOf(false) }
    var currentModel by remember { mutableStateOf<ModelEntity?>(null) }
    val modelIcon = ImageVector.vectorResource(id = R.drawable.ic_model)
    var isMoreMenuDisplay by remember { mutableStateOf(false) }
    var isMatchAll by remember { mutableStateOf(false) }
    var totalMatchModel by remember { mutableStateOf(1) }
    var currentMatchModel by remember { mutableStateOf(0) }
    var isMatchDialogOpen by remember { mutableStateOf(false) }
    if (actionMenuDisplay && currentModel != null) {
        BottomActionSheet(items = listOf(
            DrawViewModel.models.firstOrNull { currentModel?.name == it.title || currentModel?.name == it.modelName }
                .let {
                    if (it != null) {
                        ActionItem(
                            text = stringResource(R.string.switch_model),
                            onAction = {
                                actionMenuDisplay = false
                                scope.launch(Dispatchers.IO) {
                                    DrawViewModel.switchModel(it.modelName)
                                }
                            }
                        )
                    } else {
                        null
                    }

                }
        ).filterNotNull()) {
            actionMenuDisplay = false
        }
    }
    fun matchAll(isSkipExist: Boolean? = true) {
        if (isMatchAll) {
            return
        }
        scope.launch(Dispatchers.IO) {
            isMatchAll = true
            val list = modelList
            totalMatchModel = list.size
            currentMatchModel = 0
            list.forEach { model ->
                if (model.civitaiApiId != null && isSkipExist == true) {
                    currentMatchModel++
                    return@forEach
                }
                try {
                    ModelStore.matchModelByModelId(context, model.modelId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                currentMatchModel++
            }
            refresh()
            isMatchAll = false
        }
    }
    if (isMatchAll) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(text = stringResource(R.string.matching_model))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        stringResource(
                            R.string.match_model_progress_text,
                            currentMatchModel,
                            totalMatchModel
                        )
                    )
                    Text(text = modelList[currentMatchModel].name)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = currentMatchModel.toFloat() / totalMatchModel.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = { },
            dismissButton = { }
        )
    }
    if (DrawViewModel.isSwitchingModel) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(id = R.string.dialog_switch_model_title)) },
            text = { Text(stringResource(id = R.string.dialog_switch_model_content)) },
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.model_list_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        isMoreMenuDisplay = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "menu",
                        )
                    }
                    if (AppConfigStore.config.enablePlugin) {
                        DropdownMenu(
                            expanded = isMoreMenuDisplay,
                            onDismissRequest = { isMoreMenuDisplay = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.match_all_model)) },
                                onClick = {
                                    isMoreMenuDisplay = false
                                    isMatchDialogOpen = true
                                }
                            )
                        }
                    }

                }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(modelList.size) { idx ->
                            val model = modelList[idx]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                    }
                                    .combinedClickable(
                                        onLongClick = {
                                            currentModel = model
                                            actionMenuDisplay = true
                                        },
                                        onClick = {
                                            ModelDetailViewModel.asNew()
                                            navController.navigate(
                                                Screens.ModelDetailScreen.route.replace(
                                                    "{modelId}",
                                                    model.modelId.toString()
                                                )
                                            )

                                        }
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(150.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (model.coverPath != null) {
                                        AsyncImage(
                                            model = model.coverPath,
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize(),
                                        )
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
                                Column {
                                    Text(
                                        text = model.name,
                                        fontSize = 16.sp,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .height(50.dp),
                                        maxLines = 3,
                                    )
                                }
                            }
                        }
                    }
                    DrawBar()
                }
            }
        }
    )
}