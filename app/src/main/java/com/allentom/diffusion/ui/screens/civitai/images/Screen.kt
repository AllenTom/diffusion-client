package com.allentom.diffusion.ui.screens.civitai.images

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.allentom.diffusion.ImageCacheHelper
import com.allentom.diffusion.Screens
import com.allentom.diffusion.api.civitai.getCivitaiApiClient
import com.allentom.diffusion.composables.TextPickUpItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivitaiImagesScreen(navController: NavController) {

    val scope = rememberCoroutineScope()
    var page by remember {
        mutableStateOf(1)
    }
    var pageSize by remember {
        mutableStateOf(20)
    }
    var isLoading by remember {
        mutableStateOf(false)
    }
    var filter by remember {
        mutableStateOf(CivitaiImageFilter())
    }
    var isFilterModalShow by remember {
        mutableStateOf(false)
    }

    fun refresh() {
        if (isLoading) {
            return
        }
        page = 1
        isLoading = true
        scope.launch {
            try {
                val result = getCivitaiApiClient().getImageList(
                    nsfw = filter.nsfw,
                    page = 1,
                    limit = 20,
                    sort = filter.sort,
                    period = filter.period
                )
                if (result.isSuccessful) {
                    CivitaiImageListViewModel.imageList = result.body()?.items ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isLoading = false
    }

    //    LaunchedEffect(Unit) {
//        refresh()
//    }
    fun loadMore() {
        if (isLoading) {
            return
        }
        scope.launch {
            try {
                isLoading = true
                page += 1
                val result =
                    getCivitaiApiClient().getImageList(
                        nsfw = "None",
                        page = page,
                        limit = pageSize,
                        sort = filter.sort,
                        period = filter.period
                    )
                if (result.isSuccessful) {
                    CivitaiImageListViewModel.imageList += result.body()?.items ?: emptyList()
                }
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val listState = rememberLazyGridState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collectLatest { lastIndex ->
                if (lastIndex != null && lastIndex >= CivitaiImageListViewModel.imageList.size - 1) {
                    loadMore()
                }
            }
    }
    LaunchedEffect(filter) {
        refresh()
    }
    if (isFilterModalShow) {
        ModalBottomSheet(onDismissRequest = {
            isFilterModalShow = false
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                CivitaiImageFilterPanel(filter = filter, onFilterChange = {
                    filter = it
                })
            }
        }

    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Civitai Image") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                isFilterModalShow = true
            }) {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        }

    ) { paddingValues: PaddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = listState,
            ) {
                items(CivitaiImageListViewModel.imageList.size) {
                    val item = CivitaiImageListViewModel.imageList[it]
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
                            contentScale = ContentScale.Crop,
                            imageLoader = ImageCacheHelper.imageLoader,
                        ) {
                            when (val state = painter.state) {
                                is AsyncImagePainter.State.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
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
                                        Text(text = "Error")
                                    }
                                }

                                else -> {
                                    SubcomposeAsyncImageContent(
                                        modifier = Modifier
                                            .clickable {
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
    }

}

//    limit (OPTIONAL)	number	The number of results to be returned per page. This can be a number between 0 and 200. By default, each page will return 100 results.
//    postId (OPTIONAL)	number	The ID of a post to get images from
//    modelId (OPTIONAL)	number	The ID of a model to get images from (model gallery)
//    modelVersionId (OPTIONAL)	number	The ID of a model version to get images from (model gallery filtered to version)
//    username (OPTIONAL)	string	Filter to images from a specific user
//    nsfw (OPTIONAL)	boolean | enum (None, Soft, Mature, X)	Filter to images that contain mature content flags or not (undefined returns all)
//    sort (OPTIONAL)	enum (Most Reactions, Most Comments, Newest)	The order in which you wish to sort the results
//    period (OPTIONAL)	enum (AllTime, Year, Month, Week, Day)	The time frame in which the images will be sorted
//    page (OPTIONAL)	number	The page from which to start fetching creators
data class CivitaiImageFilter(
    val nsfw: String? = "None",
    val sort: String? = "Newest",
    val period: String? = "AllTime",
):Serializable

@Composable
fun CivitaiImageFilterPanel(
    filter: CivitaiImageFilter,
    onFilterChange: (CivitaiImageFilter) -> Unit,
) {
    Column {
        TextPickUpItem(
            label = "NSFW",
            value = filter.nsfw,
            options = listOf("None", "Soft", "Mature", "X"),
            onValueChange = {
                onFilterChange(filter.copy(nsfw = it))
            })
        TextPickUpItem(
            label = "Sort",
            value = filter.sort,
            options = listOf("Most Reactions", "Most Comments", "Newest"),
            onValueChange = {
                onFilterChange(filter.copy(sort = it))
            })
        TextPickUpItem(
            label = "Period",
            value = filter.period,
            options = listOf("AllTime", "Year", "Month", "Week", "Day"),
            onValueChange = {
                onFilterChange(filter.copy(period = it))
            })
    }
}