package com.allentom.diffusion.ui.screens.lora.detail

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.api.civitai.getCivitaiApiClient
import com.allentom.diffusion.composables.ActionItem
import com.allentom.diffusion.composables.ApplyLoraDialog
import com.allentom.diffusion.composables.BottomActionSheet
import com.allentom.diffusion.composables.CivitaiModelSelectDialog
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.Prompt
import com.allentom.diffusion.store.PromptStore
import com.allentom.diffusion.ui.parts.CivitaiImageGrid
import com.allentom.diffusion.ui.parts.CivitaiModelView
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImageFilterPanel
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.model.detail.ModelDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun LoraDetailScreen(
    navController: NavController,
    id: Long,
) {
    var currentPreviewIndex by remember {
        mutableStateOf(0)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isFilterModalShow by remember {
        mutableStateOf(false)
    }
    val filterIcon = ImageVector.vectorResource(id = R.drawable.ic_filter)
    var civitaiModelLoadingError by remember {
        mutableStateOf<String?>(null)
    }

    fun refreshCivitaiImage(modelId: Long, modelVersionId: Long) {
        if (LoraDetailViewModel.isLoading) {
            return
        }
        LoraDetailViewModel.page = 1
        scope.launch {
            try {
                LoraDetailViewModel.isLoading = true
                val result = getCivitaiApiClient().getImageList(
                    page = LoraDetailViewModel.page,
                    limit = 20,
                    sort = LoraDetailViewModel.filter.sort,
                    period = LoraDetailViewModel.filter.period,
                    nsfw = LoraDetailViewModel.filter.nsfw,
                    modelId = modelId,
                    modelVersionId = modelVersionId
                )
                if (result.isSuccessful) {
                    LoraDetailViewModel.civitaiImageList = result.body()?.items ?: emptyList()
                }
                LoraDetailViewModel.isLoading = false
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        if (LoraDetailViewModel.civitaiModelVersion != null) {
            return
        }
        LoraDetailViewModel.isCivitaiModelLoading = true
        scope.launch(Dispatchers.IO) {
            currentPreviewIndex = 0
            LoraDetailViewModel.loraModel = PromptStore.getLoraPromptWithRelate(context, id)
            LoraDetailViewModel.loraModel?.loraPrompt?.civitaiId?.let {
                try {
                    getCivitaiApiClient().getModelVersionById(it.toString()).let {
                        LoraDetailViewModel.civitaiModelVersion = it.body()
                        val modelId = it.body()?.modelId
                        val modelVersionId = it.body()?.id
                        if (modelId != null && modelVersionId != null) {
                            refreshCivitaiImage(modelId, modelVersionId)
                        }
                        if (modelId != null) {
                            LoraDetailViewModel.civitaiModel = getCivitaiApiClient().getModelById(modelId).body()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    civitaiModelLoadingError = e.message
                    scope.launch {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    LoraDetailViewModel.isCivitaiModelLoading = false
                }

            }
        }
    }
    LaunchedEffect(Unit) {
        refresh()
    }
    var isFetchCivitaiDialogShow by remember {
        mutableStateOf(false)
    }
    var isActionMenuShow by remember {
        mutableStateOf(false)
    }
    var selectedTriggerTextList by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var isTriggerActionBottomSheetShow by remember {
        mutableStateOf(false)
    }
    var isAddLoraDialogShow by remember {
        mutableStateOf(false)
    }

    val pagerState = rememberPagerState {
        3
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            LoraDetailViewModel.selectedTabIndex = pagerState.currentPage
        }
    }

    if (isFetchCivitaiDialogShow) {
        CivitaiModelSelectDialog(
            onDismiss = {
                isFetchCivitaiDialogShow = false
            },
            onApply = {
                scope.launch(Dispatchers.IO) {
                    PromptStore.linkCivitaiModelById(context, id, it.id.toString())
                    refresh()
                }
                isFetchCivitaiDialogShow = false
            }
        )
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
                CivitaiImageFilterPanel(filter = LoraDetailViewModel.filter, onFilterChange = {
                    LoraDetailViewModel.filter = it
                    AppConfigStore.updateCivitaiImageFilter(context, it)
                    refreshCivitaiImage(
                        LoraDetailViewModel.civitaiModelVersion?.modelId ?: 0,
                        LoraDetailViewModel.civitaiModelVersion?.id ?: 0
                    )
                })
            }
        }
    }
    if (isAddLoraDialogShow && LoraDetailViewModel.loraModel != null) {
        ApplyLoraDialog(onDismiss = {
            isAddLoraDialogShow = false
        }, onApply = { lora ->
            isAddLoraDialogShow = false
            val exist =
                DrawViewModel.inputLoraList.find { it.name == LoraDetailViewModel.loraModel?.loraPrompt?.name }
            if (exist == null) {
                DrawViewModel.inputLoraList += lora
                Toast.makeText(
                    context,
                    context.getString(R.string.added), Toast.LENGTH_SHORT
                ).show()
            }
        }, lora = LoraDetailViewModel.loraModel!!)
    }
    fun loadMore() {
        if (LoraDetailViewModel.isLoading && LoraDetailViewModel.civitaiImageList.isEmpty()) {
            return
        }
        scope.launch {
            try {
                LoraDetailViewModel.page += 1
                val result =
                    getCivitaiApiClient().getImageList(
                        page = LoraDetailViewModel.page,
                        limit = 20,
                        sort = LoraDetailViewModel.filter.sort,
                        period = LoraDetailViewModel.filter.period,
                        nsfw = LoraDetailViewModel.filter.nsfw,
                        modelVersionId = LoraDetailViewModel.civitaiModelVersion?.id,
                        modelId = LoraDetailViewModel.civitaiModelVersion?.modelId,
                    )
                if (result.isSuccessful) {
                    LoraDetailViewModel.civitaiImageList += result.body()?.items ?: emptyList()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    if (isTriggerActionBottomSheetShow) {
        BottomActionSheet(items = listOf(
            ActionItem(
                text = stringResource(id = R.string.add_to_prompt),
                onAction = {
                    DrawViewModel.inputPromptText = DrawViewModel.inputPromptText.filter { exist ->
                        selectedTriggerTextList.none { it == exist.text }
                    } + selectedTriggerTextList.map { text ->
                        Prompt(text, 0)
                    }
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.added_to_prompt,
                            selectedTriggerTextList.size.toString()
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ),
            ActionItem(
                text = stringResource(id = R.string.assign_to_prompt),
                onAction = {
                    DrawViewModel.inputPromptText = selectedTriggerTextList.map { text ->
                        Prompt(text, 0)
                    }
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.assigned_to_prompt,
                            selectedTriggerTextList.size.toString()
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ),
        )) {
            isTriggerActionBottomSheetShow = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val model = LoraDetailViewModel.loraModel
                    if (model != null) {
                        if (model.loraPrompt.title.isEmpty()) {
                            Text(text = model.loraPrompt.name, maxLines = 1)
                        } else {
                            Text(text = model.loraPrompt.title, maxLines = 1)
                        }
                    } else {
                        Text(text = stringResource(R.string.lora_model_list_screen_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    if (LoraDetailViewModel.selectedTabIndex == 2) {
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
                        isActionMenuShow = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                        )

                    }
                    DropdownMenu(
                        expanded = isActionMenuShow,
                        onDismissRequest = { isActionMenuShow = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.fetch_from_civitai)) },
                            onClick = {
                                isFetchCivitaiDialogShow = true
                                isActionMenuShow = false
                            }
                        )
                        if (AppConfigStore.config.enablePlugin) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.auto_match_with_civitai)) },
                                onClick = {
                                    isActionMenuShow = false
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            PromptStore.matchLoraByModelId(context, id)
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
                        DropdownMenuItem(text = {
                            Text(stringResource(R.string.use_this_lora))

                        }, onClick = {
                            isAddLoraDialogShow = true
                            isActionMenuShow = false
                        })
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            Column {
                TabRow(
                    selectedTabIndex = LoraDetailViewModel.selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Tab(
                        selected = (LoraDetailViewModel.selectedTabIndex == 0),
                        onClick = {
                            LoraDetailViewModel.selectedTabIndex = 0
                            scope.launch {
                                pagerState.animateScrollToPage(LoraDetailViewModel.selectedTabIndex)
                            }
                        },
                        text = {
                            Text(text = stringResource(R.string.model))
                        },
                    )
                    Tab(
                        selected = (LoraDetailViewModel.selectedTabIndex == 1),
                        onClick = {
                            LoraDetailViewModel.selectedTabIndex = 1
                            scope.launch {
                                pagerState.animateScrollToPage(LoraDetailViewModel.selectedTabIndex)
                            }
                        },
                        text = {
                            Text(text = stringResource(R.string.civitai))
                        }
                    )
                    Tab(
                        selected = (LoraDetailViewModel.selectedTabIndex == 2),
                        onClick = {
                            LoraDetailViewModel.selectedTabIndex = 2
                            scope.launch {
                                pagerState.animateScrollToPage(LoraDetailViewModel.selectedTabIndex)
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
                                    LoraDetailViewModel.loraModel?.let { model ->
                                        model.loraPrompt.previewPath?.let {
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
                                        model.triggerText.takeIf { it.isNotEmpty() }?.let {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.trigger_text),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (selectedTriggerTextList.isNotEmpty()) {
                                                    TextButton(onClick = {
                                                        isTriggerActionBottomSheetShow = true
                                                    }) {
                                                        Text(text = stringResource(R.string.action))
                                                    }
                                                }
                                                TextButton(onClick = {
                                                    selectedTriggerTextList =
                                                        if (selectedTriggerTextList.isEmpty()) {
                                                            model.triggerText.map { it.text }
                                                        } else {
                                                            emptyList()
                                                        }
                                                }) {
                                                    if (selectedTriggerTextList.isEmpty()) {
                                                        Text(text = stringResource(id = R.string.select_all))
                                                    } else {
                                                        Text(text = stringResource(id = R.string.deselect_all))
                                                    }
                                                }
                                            }
                                            FlowRow(
                                            ) {
                                                it.forEach { prompt ->
                                                    FilterChip(
                                                        onClick = {
                                                            selectedTriggerTextList =
                                                                if (selectedTriggerTextList.contains(
                                                                        prompt.text
                                                                    )
                                                                ) {
                                                                    selectedTriggerTextList.filter { it != prompt.text }
                                                                } else {
                                                                    selectedTriggerTextList + prompt.text
                                                                }
                                                        },
                                                        selected = selectedTriggerTextList.contains(
                                                            prompt.text
                                                        ),
                                                        label = {
                                                            Text(text = prompt.text)
                                                        },
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
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
                                    civitaiModelVersion = LoraDetailViewModel.civitaiModelVersion,
                                    isLoading = LoraDetailViewModel.isCivitaiModelLoading,
                                    civitaiModel = LoraDetailViewModel.civitaiModel,
                                )
                            }

                        2 -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            CivitaiImageGrid(
                                navController = navController,
                                civitaiImageList = LoraDetailViewModel.civitaiImageList,

                                ) {
                                loadMore()
                            }
                        }
                    }
                }
                DrawBar()
            }
        }
    }
}