package com.allentom.diffusion.ui.screens.model.detail

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.api.civitai.getCivitaiApiClient
import com.allentom.diffusion.composables.CivitaiModelSelectDialog
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.ModelStore
import com.allentom.diffusion.ui.parts.CivitaiImageGrid
import com.allentom.diffusion.ui.parts.CivitaiModelView
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImageFilterPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModelDetailScreen(navController: NavController, id: Long) {
    val filterIcon = ImageVector.vectorResource(id = R.drawable.ic_filter)
    var isMoreMenuDisplay by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pagerState = rememberPagerState {
        3
    }
    val listState = rememberLazyGridState()
    var currentPreviewIndex by remember {
        mutableStateOf(0)
    }
    var isFilterModalShow by remember {
        mutableStateOf(false)
    }

    fun refreshCivitaiImage(modelId: Long, modelVersionId: Long) {
        if (ModelDetailViewModel.isLoading) {
            return
        }
        ModelDetailViewModel.page = 1
        scope.launch {
            try {
                ModelDetailViewModel.isLoading = true
                val result = getCivitaiApiClient().getImageList(
                    page = ModelDetailViewModel.page,
                    limit = 20,
                    sort = ModelDetailViewModel.filter.sort,
                    period = ModelDetailViewModel.filter.period,
                    nsfw = ModelDetailViewModel.filter.nsfw,
                    modelId = modelId,
                    modelVersionId = modelVersionId
                )
                if (result.isSuccessful) {
                    ModelDetailViewModel.civitaiImageList = result.body()?.items ?: emptyList()
                }
                ModelDetailViewModel.isLoading = false
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        if (ModelDetailViewModel.civitaiModel != null) {
            return
        }
        ModelDetailViewModel.isCivitaiModelLoading = true
        scope.launch(Dispatchers.IO) {
            currentPreviewIndex = 0
            ModelDetailViewModel.model = ModelStore.getByID(context, id)
            ModelDetailViewModel.model?.civitaiApiId?.let {
                getCivitaiApiClient().getModelVersionById(it.toString()).let {
                    ModelDetailViewModel.civitaiModel = it.body()
                    val modelId = it.body()?.modelId
                    val modelVersionId = it.body()?.id
                    if (modelId != null && modelVersionId != null) {
                        refreshCivitaiImage(modelId, modelVersionId)
                    }
                }
            }
            ModelDetailViewModel.isCivitaiModelLoading = false
        }
    }
    LaunchedEffect(Unit) {
        refresh()
    }
    fun loadMore() {
        if (ModelDetailViewModel.isLoading && ModelDetailViewModel.civitaiImageList.isEmpty()) {
            return
        }
        scope.launch {
            try {
                ModelDetailViewModel.page += 1
                val result =
                    getCivitaiApiClient().getImageList(
                        page = ModelDetailViewModel.page,
                        limit = 20,
                        sort = ModelDetailViewModel.filter.sort,
                        period = ModelDetailViewModel.filter.period,
                        nsfw = ModelDetailViewModel.filter.nsfw,
                        modelVersionId = ModelDetailViewModel.civitaiModel?.id,
                        modelId = ModelDetailViewModel.civitaiModel?.modelId,
                    )
                if (result.isSuccessful) {
                    ModelDetailViewModel.civitaiImageList += result.body()?.items ?: emptyList()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collectLatest { lastIndex ->
                if (lastIndex != null && lastIndex >= ModelDetailViewModel.civitaiImageList.size - 1) {
                    loadMore()
                }
            }
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            ModelDetailViewModel.selectedTabIndex = pagerState.currentPage
        }
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
                CivitaiImageFilterPanel(filter = ModelDetailViewModel.filter, onFilterChange = {
                    ModelDetailViewModel.filter = it
                    AppConfigStore.updateCivitaiImageFilter(context, it)
                    refreshCivitaiImage(
                        ModelDetailViewModel.civitaiModel?.modelId ?: 0,
                        ModelDetailViewModel.civitaiModel?.id ?: 0
                    )
                })
            }
        }
    }
    var isFetchCivitaiDialogShow by remember {
        mutableStateOf(false)
    }

    if (isFetchCivitaiDialogShow) {
        CivitaiModelSelectDialog(
            onDismiss = {
                isFetchCivitaiDialogShow = false
            },
            onApply = {
                scope.launch(Dispatchers.IO) {
                    ModelStore.linkCivitaiModel(context, it, id)
                    refresh()
                }
                isFetchCivitaiDialogShow = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = ModelDetailViewModel.model?.name ?: stringResource(id = R.string.model), maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    if (ModelDetailViewModel.selectedTabIndex == 2) {
                        IconButton(onClick = {
                            isFilterModalShow = true
                        }) {
                            Icon(
                                imageVector = filterIcon,
                                contentDescription = "filter",
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
                    DropdownMenu(
                        expanded = isMoreMenuDisplay,
                        onDismissRequest = { isMoreMenuDisplay = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.fetch_from_civitai)) },
                            onClick = {
                                isFetchCivitaiDialogShow = true
                                isMoreMenuDisplay = false
                            }
                        )
                        if (AppConfigStore.config.enablePlugin) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.auto_match_with_civitai)) },
                                onClick = {
                                    isMoreMenuDisplay = false
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            ModelStore.matchModelByModelId(context, id)
                                            refresh()
                                            scope.launch(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.matched),
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            scope.launch(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.match_failed),
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }

                                        }
                                    }
                                }
                            )
                        }

                    }
                }
            )
        },
    ) { paddingValues: PaddingValues ->
        Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
            Column {
                TabRow(
                    selectedTabIndex = ModelDetailViewModel.selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Tab(
                        selected = (ModelDetailViewModel.selectedTabIndex == 0),
                        onClick = {
                            ModelDetailViewModel.selectedTabIndex = 0
                            scope.launch {
                                pagerState.animateScrollToPage(ModelDetailViewModel.selectedTabIndex)
                            }
                        },
                        text = {
                            Text(text = stringResource(R.string.model))
                        },
                    )
                    Tab(
                        selected = (ModelDetailViewModel.selectedTabIndex == 1),
                        onClick = {
                            ModelDetailViewModel.selectedTabIndex = 1
                            scope.launch {
                                pagerState.animateScrollToPage(ModelDetailViewModel.selectedTabIndex)
                            }
                        },
                        text = {
                            Text(text = stringResource(R.string.civitai))
                        }
                    )
                    Tab(
                        selected = (ModelDetailViewModel.selectedTabIndex == 2),
                        onClick = {
                            ModelDetailViewModel.selectedTabIndex = 2
                            scope.launch {
                                pagerState.animateScrollToPage(ModelDetailViewModel.selectedTabIndex)
                            }
                        },
                        text = {
                            Text(text = stringResource(R.string.civitai_images))
                        }
                    )
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { index ->
                    when (index) {
                        0 ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    ModelDetailViewModel.model?.let { model ->
                                        model.coverPath?.let {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                            ) {
                                                val currentPreviewImage = it
                                                AsyncImage(
                                                    model = currentPreviewImage,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                        1 ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                            ) {
                                CivitaiModelView(
                                    navController = navController,
                                    civitaiModel = ModelDetailViewModel.civitaiModel,
                                    isLoading = ModelDetailViewModel.isCivitaiModelLoading
                                )
                            }

                        2 -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            CivitaiImageGrid(
                                navController = navController,
                                civitaiImageList = ModelDetailViewModel.civitaiImageList
                            ) {
                                loadMore()
                            }

                        }
                    }
                }
            }
        }
        DrawBar()

    }
}

