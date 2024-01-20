package com.allentom.diffusion.ui.parts

import android.content.Intent
import android.net.Uri
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
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
import com.allentom.diffusion.api.civitai.entities.CivitaiModelVersion
import com.allentom.diffusion.api.civitai.entities.Stats
import com.allentom.diffusion.composables.loaddingShimmer
import com.allentom.diffusion.extension.thenIf
import com.allentom.diffusion.ui.screens.civitai.CivitaiImageViewModel
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImageListViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CivitaiModelView(
    navController: NavController,
    civitaiModelVersion: CivitaiModelVersion?,
    civitaiModel: CivitaiModel?,
    isLoading: Boolean = false,
) {
    var currentPreviewIndex by remember {
        mutableStateOf(0)
    }
    var displayTabIndex by remember {
        mutableStateOf(0)
    }
    val context = LocalContext.current
    val linkIcon = ImageVector.vectorResource(id = R.drawable.ic_link)

    fun openBrowserWithUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        civitaiModelVersion.let { modelVersion ->
            Spacer(modifier = Modifier.height(8.dp))
            modelVersion?.images?.getOrNull(currentPreviewIndex)
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
                    items(modelVersion?.images?.size ?: 0) {
                        val image = modelVersion?.images?.get(it)
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
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
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

            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = displayTabIndex == 0,
                        onClick = {
                            displayTabIndex = 0
                        },
                        label = {
                            Text(text = stringResource(id = R.string.model))
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = displayTabIndex == 1,
                        onClick = {
                            displayTabIndex = 1
                        },
                        label = {
                            Text(text = stringResource(R.string.model_version))
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        val modelId = civitaiModel?.id
                        val modelVersionId = modelVersion?.id
                        if (modelId != null && modelVersionId != null) {
                            openBrowserWithUrl("https://civitai.com/models/${modelId}?modelVersionId=${modelVersionId}")
                        }
                    }) {
                        Icon(
                            imageVector = linkIcon,
                            contentDescription = null
                        )
                    }
                }
                when (displayTabIndex) {
                    0 -> {
                        civitaiModel?.let { cvModel ->
                            Spacer(modifier = Modifier.height(16.dp))
                            ListItem(
                                headlineContent = {
                                    Text(stringResource(id =R.string.name))
                                }, supportingContent = {
                                    Text(text = cvModel.name)
                                }
                            )
                            ListItem(headlineContent = {
                                Text(stringResource(R.string.model_tags))
                            }, supportingContent = {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    cvModel.tags.forEach {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer
                                                )
                                                .padding(vertical = 4.dp, horizontal = 8.dp)

                                        ) {
                                            Text(text = it)
                                        }
                                    }

                                }
                            })
                            ListItem(
                                headlineContent = {
                                    Text(stringResource(R.string.nsfw))
                                }, supportingContent = {
                                    Text(text = if (cvModel.nsfw) "Yes" else "No")
                                }
                            )
                            cvModel.stats.let { stats ->
                                Spacer(modifier = Modifier.height(16.dp))
                                CivitaiModelStats(modelStats = stats)
                            }

                            ListItem(
                                headlineContent = {
                                    Text(text = stringResource(R.string.description))
                                },
                                supportingContent = {
                                    AndroidView(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        factory = { context -> TextView(context) },
                                        update = {
                                            it.text = HtmlCompat.fromHtml(
                                                cvModel.description ?: context.getString(R.string.no_description),
                                                HtmlCompat.FROM_HTML_MODE_COMPACT
                                            )
                                        }
                                    )
                                }
                            )
                        }

                    }
                    1 -> {
                        modelVersion?.stats?.let { stats ->
                            Spacer(modifier = Modifier.height(16.dp))
                            CivitaiModelStats(modelStats = stats)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        modelVersion?.let {
                            ListItem(
                                headlineContent = {
                                    Text(text = stringResource(R.string.published_at))

                                }, supportingContent = {
                                    Text(text = modelVersion.publishedAt)
                                }
                            )
                            ListItem(
                                headlineContent = {
                                    Text(text = stringResource(R.string.description))
                                },
                                supportingContent = {
                                    AndroidView(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        factory = { context -> TextView(context) },
                                        update = {
                                            it.text = HtmlCompat.fromHtml(
                                                modelVersion.description ?: context.getString(R.string.no_description),
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

@Composable
fun CivitaiModelStats(
    modelStats: Stats?
) {
    modelStats?.let { stats ->
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
                    text = stringResource(R.string.rating_stats),
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(text = stringResource(R.string.rating))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stats.rating.toString())
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(text = stringResource(R.string.rating_count))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stats.ratingCount.toString())
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(text = stringResource(R.string.downloads_count))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stats.downloadCount.toString())
                    }
                }
            }
        }
    }
}