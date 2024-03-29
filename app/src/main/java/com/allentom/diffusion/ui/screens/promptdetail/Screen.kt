package com.allentom.diffusion.ui.screens.promptdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.gridCountForDeviceWidth
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.history.HistoryWithRelation
import com.allentom.diffusion.store.history.ImageHistory
import com.allentom.diffusion.store.prompt.PromptStore
import com.allentom.diffusion.store.prompt.SavePrompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptDetailScreen(id: Long) {
    var propmt by remember {
        mutableStateOf<SavePrompt?>(null)
    }
    var historyList by remember {
        mutableStateOf<List<HistoryWithRelation>>(emptyList())
    }
    var images by remember {
        mutableStateOf<List<ImageHistory>>(emptyList())
    }
    val coverFitIcon = ImageVector.vectorResource(id = R.drawable.ic_image_fit)
    val coverCropIcon = ImageVector.vectorResource(id = R.drawable.ic_image_crop)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val columnCount = gridCountForDeviceWidth(itemWidth = 180)
    var itemImageFit by remember {
        mutableStateOf(AppConfigStore.config.promptDetailViewDisplayMode)
    }
    fun refreshPrompt() {
        scope.launch(Dispatchers.IO) {
            val pid = PromptDetailViewModel.promptId
            propmt = PromptStore.getPromptById(context, pid)
            propmt?.let { it ->
                historyList = PromptStore.getPromptHistory(context, it)
                images = historyList.map { it.imagePaths }.flatten().map { it.toImageHistory() }
                images.size
            }
        }
    }
    LaunchedEffect(Unit) {
        if (id != 0L) {
            PromptDetailViewModel.promptId = id
        }
        refreshPrompt()
    }
    fun onChangeImageFit(newImageFit: String) {
        itemImageFit = newImageFit
        AppConfigStore.updateAndSave(context) {
            it.copy(loraViewDisplayMode = newImageFit)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text(text = propmt?.text ?: "", maxLines = 1)
                        if (propmt?.text != propmt?.nameCn) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = propmt?.nameCn ?: "", maxLines = 1)
                        }
                    }

                },
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
                                coverCropIcon,
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
                                coverFitIcon,
                                contentDescription = "Menu",
                            )
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(images.size) { index ->
                    val image = images[index]
                    Box(modifier = Modifier
                        .height(180.dp)) {
                        AsyncImage(
                            model = image.path,
                            contentDescription = "Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = if (itemImageFit == "Fit") {
                                ContentScale.Fit
                            } else {
                                ContentScale.Crop
                            }
                        )
                    }
                }
            }
            DrawBar()
        }

    }
}