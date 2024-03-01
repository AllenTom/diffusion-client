package com.allentom.diffusion.ui.screens.model.list

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
import com.allentom.diffusion.api.entity.Model
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.ActionItem
import com.allentom.diffusion.composables.BottomActionSheet
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.MatchOptionDialog
import com.allentom.diffusion.extension.thenIf
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
        mutableStateOf<List<Model>>(emptyList())
    }
    var itemImageFit by remember {
        mutableStateOf(AppConfigStore.config.modelViewDisplayMode)
    }
    val imageFitIcon = ImageVector.vectorResource(id = R.drawable.ic_image_fit)
    val imageCropIcon = ImageVector.vectorResource(id = R.drawable.ic_image_crop)
    val context = LocalContext.current
    fun onChangeImageFit(newImageFit: String) {
        itemImageFit = newImageFit
        AppConfigStore.updateAndSave(context) {
            it.copy(modelViewDisplayMode = newImageFit)
        }
    }

    val scope = rememberCoroutineScope()
    fun refresh() {
        scope.launch(Dispatchers.IO) {
            val result = getApiClient().getModels()
            DrawViewModel.models = DrawViewModel.loadModel(context)
            modelList = DrawViewModel.models
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
                if (model.entity.civitaiApiId != null && isSkipExist == true) {
                    currentMatchModel++
                    return@forEach
                }
                try {
                    ModelStore.matchModelByModelId(context, model.entity.modelId)
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
                    Text(text = modelList[currentMatchModel].entity.name)
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
                                            currentModel = model.entity
                                            actionMenuDisplay = true
                                        },
                                        onClick = {
                                            ModelDetailViewModel.asNew()
                                            navController.navigate(
                                                Screens.ModelDetailScreen.route.replace(
                                                    "{modelId}",
                                                    model.entity.modelId.toString()
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
                                        if (model.entity.coverPath != null) {
                                            AsyncImage(
                                                model = model.entity.coverPath,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),

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

                                            if (model.entity.coverPath != null) {
                                                if (itemImageFit == "Fit") {
                                                    AsyncImage(
                                                        model = model.entity.coverPath,
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
                                                    text = model.title,
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
    )
}