package com.allentom.diffusion.ui.parts

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpanned
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.allentom.diffusion.ImageCacheHelper
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.api.civitai.entities.CivitaiImageItem
import com.allentom.diffusion.api.civitai.entities.CivitaiModel
import com.allentom.diffusion.composables.loaddingShimmer
import com.allentom.diffusion.extension.thenIf
import com.allentom.diffusion.ui.screens.civitai.CivitaiImageViewModel
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImageListViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CivitaiModelView(
    navController: NavController,
    civitaiModel: CivitaiModel?,
    isLoading: Boolean = false,
) {
    var currentPreviewIndex by remember {
        mutableStateOf(0)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        civitaiModel.let { cvModel ->
            Spacer(modifier = Modifier.height(8.dp))
            cvModel?.images?.getOrNull(currentPreviewIndex)
                .let { displayImage ->
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .height(200.dp)
                            .fillMaxWidth()
                            .thenIf(
                                isLoading,
                                Modifier
                                    .background(
                                        loaddingShimmer(
                                            targetValue = 1300f,
                                            showShimmer = true
                                        )
                                    )
                                    .clip(shape = RoundedCornerShape(16.dp))
                            )
                            .clickable {
                                CivitaiImageViewModel.image =
                                    displayImage
                                navController.navigate(Screens.CivitaiModelImageScreen.route)
                            }
                    ) {
                        displayImage?.let {
                            AsyncImage(
                                model = it.url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                    }
                }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .thenIf(
                        isLoading,
                        Modifier
                            .background(
                                loaddingShimmer(
                                    targetValue = 1300f,
                                    showShimmer = true
                                )
                            )
                            .clip(shape = RoundedCornerShape(16.dp))
                    )
            ) {
                LazyRow {
                    items(cvModel?.images?.size ?: 0) {
                        val image = cvModel?.images?.get(it)
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .clickable {
                                    currentPreviewIndex = it
                                }
                                .border(
                                    width = 2.dp,
                                    color = if (it == currentPreviewIndex) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color.Transparent
                                    }
                                )
                        ) {
                            image?.let {
                                AsyncImage(
                                    model = it.url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()

                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .thenIf(
                            isLoading,
                            Modifier
                                .background(
                                    loaddingShimmer(
                                        targetValue = 1300f,
                                        showShimmer = true
                                    )
                                )
                                .clip(shape = RoundedCornerShape(16.dp))
                        )
                ) {

                }

            }else{
                cvModel?.stats?.let { stats ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Stats",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(text = "Rating")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = stats.rating.toString())
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(text = "Count")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = stats.ratingCount.toString())
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(text = "Downloads")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = stats.downloadCount.toString())
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                cvModel?.let {
                    ListItem(
                        headlineContent = {
                            Text(text = "Published At")

                        }, supportingContent = {
                            Text(text = cvModel.publishedAt)
                        }
                    )
                    ListItem(
                        headlineContent = {
                            Text(text = "Description")
                        },
                        supportingContent = {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                factory = { context -> TextView(context) },
                                update = {
                                    it.text = HtmlCompat.fromHtml(
                                        cvModel.description ?: "No description",
                                        HtmlCompat.FROM_HTML_MODE_COMPACT
                                    )
                                }
                            )
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun CivitaiImageGrid(
    navController: NavController,
    civitaiImageList: List<CivitaiImageItem>,
    isLoading: Boolean? = false,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyGridState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collectLatest { lastIndex ->
                if (lastIndex != null && lastIndex >= civitaiImageList.size - 1) {
                    onLoadMore()
                }
            }
    }
    if (isLoading == true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(loaddingShimmer(targetValue = 1300f, showShimmer = true)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
            )
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = listState,
    ) {
        items(civitaiImageList.size) {
            val item = civitaiImageList[it]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = item.url, contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    imageLoader = ImageCacheHelper.imageLoader,
                ) {
                    val state = painter.state
                    when (state) {
                        is AsyncImagePainter.State.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        loaddingShimmer(
                                            targetValue = 1300f,
                                            showShimmer = true
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                )
                            }

                        }

                        is AsyncImagePainter.State.Error -> {
                            state.result
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = stringResource(id = R.string.error))
                            }
                        }

                        else -> {
                            SubcomposeAsyncImageContent(
                                modifier = Modifier
                                    .clickable {
                                        CivitaiImageListViewModel.imageList = civitaiImageList
                                        navController.navigate(
                                            Screens.CivitaiImageDetail.route.replace(
                                                "{id}",
                                                item.id.toString()
                                            )
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}