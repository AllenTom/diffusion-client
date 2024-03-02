package com.allentom.diffusion.ui.screens.home.tabs.gallery


import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.Util
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.gridCountForDeviceWidth
import com.allentom.diffusion.extension.thenIf
import com.allentom.diffusion.store.history.HistoryStore
import com.allentom.diffusion.store.history.ImageHistory
import com.allentom.diffusion.ui.screens.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryView(navController: NavController) {
    val context = LocalContext.current
    var galleryItems by remember { mutableStateOf(listOf<ImageHistory>()) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            galleryItems = HistoryStore.getFavoriteImageHistory(context)
        }
    }
    val columns = gridCountForDeviceWidth(itemWidth = 120)
    var selectedImageItemIds by remember { mutableStateOf(listOf<String>()) }
    val downloadIcon = ImageVector.vectorResource(id = R.drawable.ic_download)
    val unFavoriteIcon = ImageVector.vectorResource(id = R.drawable.ic_unfavo)
    fun saveAllSelectImageToDeviceImage() {
        selectedImageItemIds.forEach { selectedName ->
            galleryItems.find { it.name == selectedName }?.let { imageHistory: ImageHistory ->
                Util.copyImageFileToGallery(context, imageHistory.path,
                    "${Util.randomString(4)}_${imageHistory.name}"
                )
            }
        }
        HomeViewModel.gallerySelectMode = false
        selectedImageItemIds = listOf()
        Toast.makeText(context, context.getString(R.string.saved_to_device_gallery), Toast.LENGTH_SHORT).show()
    }
    fun unFavouriteAllSelectImage() {
        scope.launch(Dispatchers.IO) {
            selectedImageItemIds.forEach { selectedName ->
                galleryItems.find { it.name == selectedName }?.let { imageHistory: ImageHistory ->
                    HistoryStore.updateImageHistory(context, imageHistory.copy(favourite = false))
                }
            }
            galleryItems = HistoryStore.getFavoriteImageHistory(context)
            HomeViewModel.gallerySelectMode = false
            selectedImageItemIds = listOf()
        }

    }
    Column {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                items(
                    count = galleryItems.size,
                ) { photoIndex ->
                    val photo = galleryItems[photoIndex]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .thenIf(
                                selectedImageItemIds.contains(photo.name),
                                Modifier.border(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            )
                    ) {
                        AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                            .data(photo.path).crossfade(true).build(),
                            contentScale = when (HomeViewModel.galleryItemImageFit) {
                                0 -> ContentScale.Crop
                                1 -> ContentScale.Fit
                                else -> ContentScale.Fit
                            },
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .combinedClickable(
                                    onClick = {
                                        if (HomeViewModel.gallerySelectMode) {
                                            if (selectedImageItemIds.contains(photo.name)) {
                                                selectedImageItemIds =
                                                    selectedImageItemIds.filter { it != photo.name }
                                                if (selectedImageItemIds.isEmpty()) {
                                                    HomeViewModel.gallerySelectMode = false
                                                }
                                            } else {
                                                selectedImageItemIds =
                                                    selectedImageItemIds + photo.name
                                            }
                                        } else {
                                            navController.navigate(
                                                Screens.ImageDetail.route.replace(
                                                    "{id}", photo.name
                                                )
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        HomeViewModel.gallerySelectMode = true
                                        selectedImageItemIds = selectedImageItemIds + photo.name
                                    }
                                )
                        )
                    }
                }
            },
        )
        if (HomeViewModel.gallerySelectMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.selected_items, selectedImageItemIds.size)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        saveAllSelectImageToDeviceImage()
                    }) {
                        Icon(downloadIcon, contentDescription = "Download")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        unFavouriteAllSelectImage()
                    }) {
                        Icon(unFavoriteIcon, contentDescription = "Unfavourite")
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    Divider(
                        modifier = Modifier
                            .fillMaxHeight()  //fill the max height
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = {
                        HomeViewModel.gallerySelectMode = false
                        selectedImageItemIds = listOf()
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        } else {
            DrawBar()
        }

    }
}