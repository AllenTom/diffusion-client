package com.allentom.diffusion.ui.screens.controlnet

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.composables.ControlNetImportDialog
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.store.ControlNetStore
import com.allentom.diffusion.store.SaveControlNet
import com.allentom.diffusion.ui.DrawBarViewModel
import com.allentom.diffusion.ui.screens.home.HomeViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ControlNetScreen(navController: NavController) {
    var controlNetList by remember {
        mutableStateOf<List<SaveControlNet>>(emptyList())
    }
    var currentControlNet by remember {
        mutableStateOf<SaveControlNet?>(null)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun refresh() {
        scope.launch(Dispatchers.IO) {
            controlNetList = ControlNetStore.getAll(context)
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columns = screenWidthDp / 180 // Adjust this value to change the width of each column


    var isSelectMode by remember {
        mutableStateOf(false)
    }
    var selectedControlNetIds by remember {
        mutableStateOf<List<Long>>(emptyList())
    }
    var isActionMenuShow by remember {
        mutableStateOf(false)
    }
    var isImportFromFolder by remember {
        mutableStateOf(false)
    }
    var isImportProgressDialogShow by remember {
        mutableStateOf(false)
    }
    var currentImportIndex by remember {
        mutableStateOf(0)
    }
    var totalImportCount by remember {
        mutableStateOf(1)
    }
    LaunchedEffect(Unit) {
        refresh()
    }
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            uriList.forEach { uri ->
                scope.launch(Dispatchers.IO) {
                    ControlNetStore.addControlNet(context, uri)
                    refresh()
                }
            }
        }

    fun pickImageFromGalleryAndConvertToBase64() {
        pickImageLauncher.launch("image/*")
    }
    if (isImportProgressDialogShow) {
        AlertDialog(
            onDismissRequest = {
            },
            title = {
                Text(text = stringResource(R.string.importing_control_net))
            },
            text = {
                Column {
                    Text(
                        text = stringResource(
                            id = R.string.importing,
                            currentImportIndex.toString(),
                            totalImportCount.toString()
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = currentImportIndex.toFloat() / totalImportCount.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {},
        )


    }
    if (isImportFromFolder) {
        ControlNetImportDialog(onDismissRequest = {
            isImportFromFolder = false
        }) {
            isImportFromFolder = false
            scope.launch(Dispatchers.IO) {
                try {
                    totalImportCount = it.size
                    isImportProgressDialogShow = true
                    it.forEach { item ->
                        ControlNetStore.addControlNet(context, item.sourceUri, item.previewUri)
                        currentImportIndex += 1
                    }
                    refresh()
                } catch (e: Exception) {
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }

                } finally {
                    isImportProgressDialogShow = false
                }

            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Control Net") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    if (isSelectMode) {
                        IconButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    controlNetList.filter { selectedControlNetIds.contains(it.id) }
                                        .forEach {
                                            ControlNetStore.removeControlNet(context, it)
                                        }
                                    isSelectMode = false
                                    selectedControlNetIds = emptyList()
                                    refresh()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(
                                    R.string.delete_items,
                                    selectedControlNetIds.size.toString()
                                ),
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                pickImageFromGalleryAndConvertToBase64()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
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
                            text = { Text(stringResource(R.string.import_from_folder)) },
                            onClick = {
                                isActionMenuShow = false
                                isImportFromFolder = true
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        currentControlNet?.let { saveControlNet ->
            ControlInfoBottomSheet(
                controlNet = saveControlNet,
                onDismiss = {
                    currentControlNet = null
                },
                onUseParams = { index ->
                    currentControlNet?.let {
                        scope.launch(Dispatchers.IO) {
                            DrawViewModel.applyControlNetParams(context, index, it)
                        }
                        HomeViewModel.selectedIndex = 0
                        navController.popBackStack()
                    }
                })
        }
        Column(
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(controlNetList.size) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .border(
                                    width = 4.dp,
                                    color = if (isSelectMode && selectedControlNetIds.contains(
                                            controlNetList[index].id
                                        )
                                    ) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectMode) {
                                            // In edit mode, add or remove the id from the selected ids
                                            val id = controlNetList[index].id
                                            if (selectedControlNetIds.contains(id)) {
                                                selectedControlNetIds =
                                                    selectedControlNetIds.filter { it != id }
                                                if (selectedControlNetIds.isEmpty()) {
                                                    isSelectMode = false
                                                }
                                            } else {
                                                selectedControlNetIds = selectedControlNetIds + id
                                            }
                                        } else {
                                            // Not in edit mode, perform the normal click action
                                            currentControlNet = controlNetList[index]
                                        }
                                    },
                                    onLongClick = {
                                        // Enter edit mode on long press
                                        isSelectMode = true
                                        selectedControlNetIds = listOf(controlNetList[index].id)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val item = controlNetList[index]
                            AsyncImage(
                                model = item.previewPath.ifEmpty {
                                    item.path
                                },
                                contentDescription = item.path,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
            DrawBar()
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ControlInfoBottomSheet(
    controlNet: SaveControlNet,
    onDismiss: () -> Unit,
    onUseParams: (index: Int) -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState()
    var imageActionDialogOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var selectedSlotToAdd by remember {
        mutableStateOf(0)
    }
    if (imageActionDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                imageActionDialogOpen = false
            },
            title = {
                Text(text = stringResource(id = R.string.action))
            },
            text = {
                ListItem(
                    headlineContent = {
                        Text(text = stringResource(R.string.send_to_caption))
                    },
                    modifier = Modifier.clickable {
                        scope.launch {
                            imageActionDialogOpen = false
                            val result = Util.readImageWithPathToBase64(controlNet.previewPath)
                            DrawBarViewModel.imageBase64 = result
                        }
                    }
                )
            },
            confirmButton = {}
        )
    }
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = controlNet.path,
                            contentDescription = controlNet.path,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    controlNet.previewPath.takeIf { it.isNotEmpty() }?.let {
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(200.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        imageActionDialogOpen = true
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = it,
                                contentDescription = it,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DrawViewModel.inputControlNetParams.slots.indices.forEach { index ->
                        FilterChip(
                            label = { Text(text = stringResource(id = R.string.slot, index + 1)) },
                            selected = index == selectedSlotToAdd,
                            onClick = {
                                selectedSlotToAdd = index
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Button(onClick = {
                    onUseParams(selectedSlotToAdd)
                    onDismiss()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.use_control_net))
                }

            }
        }
    }
}