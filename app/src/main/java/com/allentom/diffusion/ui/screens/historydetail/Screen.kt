package com.allentom.diffusion.ui.screens.historydetail

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.Util
import com.allentom.diffusion.composables.ActionItem
import com.allentom.diffusion.composables.BottomActionSheet
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.HistoryView
import com.allentom.diffusion.composables.ImageUriPreviewDialog
import com.allentom.diffusion.composables.IsWideWindow
import com.allentom.diffusion.store.history.HistoryStore
import com.allentom.diffusion.store.history.ImageHistory
import com.allentom.diffusion.store.history.SaveHistory
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.reactor.ReactorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(navController: NavController, historyId: Long) {
    var saveHistory by remember {
        mutableStateOf<SaveHistory?>(null)
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    var isHistoryActionOpen by remember {
        mutableStateOf(false)
    }
    val isWideDisplay = IsWideWindow()

    fun refresh() {
        scope.launch(Dispatchers.IO) {
            saveHistory = HistoryStore.getHistoryById(context, HistoryDetailViewModel.historyId)
        }
    }
    LaunchedEffect(Unit) {
        if (historyId != 0L) {
            HistoryDetailViewModel.historyId = historyId
        }
        refresh()
    }


    if (isHistoryActionOpen) {
        BottomActionSheet(items = listOf(
            ActionItem(text = stringResource(id = R.string.apply), onAction = {
                saveHistory?.let { saveHistory ->
                    scope.launch(Dispatchers.IO) {
                        DrawViewModel.applyHistory(context, saveHistory)
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.already_apply), Toast.LENGTH_SHORT
                    ).show()
                }
            }),
        ), onDismiss = {
            isHistoryActionOpen = false
        })
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.history_detail_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        isHistoryActionOpen = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    saveHistory?.let { currentHistory ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        FirstScreen(
                                            currentHistory = currentHistory,
                                            navController = navController
                                        )
                                        if (!isWideDisplay) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            SecondScreen(
                                                currentHistory = currentHistory,
                                                navController = navController
                                            )
                                        }
                                    }
                                }
                            }
                            if (isWideDisplay) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        item {
                                            SecondScreen(
                                                currentHistory = currentHistory,
                                                navController = navController
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
}

@Composable
fun FirstScreen(
    currentHistory: SaveHistory,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var currentDisplayImageIndex by remember {
        mutableStateOf(0)
    }
    var isImagePreviewOpen by remember {
        mutableStateOf(false)
    }
    var isActionMenuShow by remember {
        mutableStateOf(false)
    }

    if (isImagePreviewOpen) {
        currentHistory.imagePaths.getOrNull(currentDisplayImageIndex)?.let {
            ImageUriPreviewDialog(
                imageUri = it.path,
                onDismissRequest = {
                    isImagePreviewOpen = false
                }
            )
        }
    }
    fun favouriteImage(imageHistory: ImageHistory) {
        currentHistory.let { saveHistory ->
            scope.launch(Dispatchers.IO) {
                DrawViewModel.favouriteImageHistory(context, imageHistory, saveHistory.id)
            }
            Toast.makeText(
                context,
                context.getString(R.string.added_to_favourite), Toast.LENGTH_SHORT
            ).show()
        }
    }
    fun saveImageToDeviceGallery(imageHistory: ImageHistory) {
        scope.launch(Dispatchers.IO) {
            imageHistory.saveToDeviceGallery(context)
            scope.launch(
                Dispatchers.Main
            ) {
                Toast.makeText(
                    context,
                    context.getString(R.string.saved_to_gallery), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .height(300.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        currentHistory.imagePaths.getOrNull(currentDisplayImageIndex)?.let {
            AsyncImage(
                model = it.path,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        isImagePreviewOpen = true
                    }
            )
        }
    }
    currentHistory.imagePaths.getOrNull(currentDisplayImageIndex)?.let {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    MaterialTheme.colorScheme.primaryContainer
                )
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = it.seed.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Box(
            ) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            isActionMenuShow = true
                        },
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                DropdownMenu(
                    expanded = isActionMenuShow,
                    onDismissRequest = { isActionMenuShow = false }
                ) {

                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null
                            )

                        },
                        text = { Text(stringResource(R.string.send_to_image_to_image)) },
                        onClick = {
                            isActionMenuShow = false
                            scope.launch {
                                val width =
                                    currentHistory.savedImg2ImgParam?.width ?: currentHistory.width
                                val height =
                                    currentHistory.savedImg2ImgParam?.height
                                        ?: currentHistory.height

                                DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(
                                    imgBase64 = Util.readImageWithPathToBase64(it.path),
                                    imgFilename = it.path,
                                    width = width,
                                    height = height
                                )
                                DrawViewModel.baseParam = DrawViewModel.baseParam.copy(
                                    seed = it.seed
                                )
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.image_sent_to_image_to_image),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null
                            )

                        },
                        text = { Text(stringResource(id = R.string.send_to_reactor)) },
                        onClick = {
                            isActionMenuShow = false
                            scope.launch {
                                ReactorViewModel.addToReactorImages(
                                    uri = Uri.parse(it.path),
                                    name = it.path.split("/").last()
                                )
                                Util.readImageWithPathToBase64(it.path)
                                navController.navigate(Screens.ReactorScreen.route)
                            }
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null
                            )

                        },
                        text = { Text(stringResource(id = R.string.use_this_seed)) },
                        onClick = {
                            isActionMenuShow = false
                            scope.launch {
                                DrawViewModel.baseParam = DrawViewModel.baseParam.copy(
                                    seed = it.seed
                                )
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.applied_seed_value),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_photo_album),
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(R.string.add_to_gallery)) },
                        onClick = {
                            isActionMenuShow = false
                            favouriteImage(it)
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_download),
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(R.string.save_to_device_gallery)) },
                        onClick = {
                            isActionMenuShow = false
                            saveImageToDeviceGallery(it)
                        }
                    )
                }
            }
        }
    }

    LazyRow {
        items(currentHistory.imagePaths.size) { index ->
            val image = currentHistory.imagePaths[index]
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .height(100.dp)
                    .border(
                        width = 2.dp,
                        color = if (index == currentDisplayImageIndex) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
            ) {
                AsyncImage(
                    model = image.path,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            currentDisplayImageIndex = index
                        },
                    contentScale = ContentScale.Inside
                )
            }
        }
    }
}

@Composable
fun SecondScreen(currentHistory: SaveHistory, navController: NavController) {
    HistoryView(currentHistory = currentHistory, navController)
    Spacer(modifier = Modifier.height(120.dp))
}

@Composable
fun ParamItem(
    label: String, modifier: Modifier = Modifier, value: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Column {
                value()
            }
        }
    }
}