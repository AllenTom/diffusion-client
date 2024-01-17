package com.allentom.diffusion.ui.screens.home.tabs.gallery


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.allentom.diffusion.Screens
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.store.HistoryStore
import com.allentom.diffusion.store.ImageHistory
import com.allentom.diffusion.ui.screens.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columns = screenWidthDp / 120 // Adjust this value to change the width of each column

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
                                .clickable {
                                    navController.navigate(
                                        Screens.ImageDetail.route.replace(
                                            "{id}", photo.name
                                        )
                                    )
                                })
                    }

                }
            },
        )
        DrawBar()
    }
}