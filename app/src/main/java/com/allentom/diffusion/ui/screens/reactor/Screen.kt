package com.allentom.diffusion.ui.screens.reactor

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.ReactorRequestBody
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.ImageBase64PreviewDialog
import com.allentom.diffusion.composables.IsWideWindow
import com.allentom.diffusion.extension.toBase64
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image
import com.allentom.diffusion.ui.screens.home.tabs.draw.panels.ReactorPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactorScreen() {
    var isParamPanelVisible by remember {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var pickupTarget by remember {
        mutableStateOf("source")
    }
    var isProcessing by remember {
        mutableStateOf(false)
    }
    val isWideDisplay = IsWideWindow()
    var isResultPreviewDialogVisible by remember {
        mutableStateOf(false)
    }
    var currentSelectImageIndex by remember { mutableIntStateOf(0) }
    var isImagePreviewerOpen by remember { mutableStateOf(false) }
    var isSelectedMode by remember { mutableStateOf(false) }
    var totalToExport by remember { mutableStateOf(0) }
    var currentToExport by remember { mutableStateOf(0) }
    var isExporting by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    val downloadIcon = ImageVector.vectorResource(id = R.drawable.ic_download)
    val selectAllIcon = ImageVector.vectorResource(id = R.drawable.ic_select_all)
    val unselectAllIcon = ImageVector.vectorResource(id = R.drawable.ic_unselect_all)
    val reactorIcon = ImageVector.vectorResource(id = R.drawable.ic_swap)
    suspend fun process(withIndex: Int? = null) {
        ReactorViewModel.isProcessing = true
        try {
            val sourceImage = ReactorViewModel.param.singleImageResult ?: return
            ReactorViewModel.onStartGenerating(withIndex)
            ReactorViewModel.images.forEachIndexed { idx, inputItem ->
                if (ReactorViewModel.stopFlag) {
                    return@forEachIndexed
                }
                if (withIndex != null && withIndex != idx) {
                    return@forEachIndexed
                }
                context.contentResolver.openInputStream(inputItem.uri)?.use(InputStream::readBytes)
                    ?.let { bytes ->
                        val base64 = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            .asImageBitmap()
                            .toBase64()
                        val request = ReactorRequestBody(
                            sourceImage = sourceImage,
                            targetImage = base64,
                            sourceFacesIndex = listOf(0),
                            faceIndex = listOf(0),
                            upscaler = ReactorViewModel.param.upscaler,
                            scale = ReactorViewModel.param.scaleBy,
                            upscaleVisibility = ReactorViewModel.param.upscalerVisibility,
                            faceRestorer = ReactorViewModel.param.restoreFace,
                            restorerVisibility = ReactorViewModel.param.restoreFaceVisibility,
                            restoreFirst = if (ReactorViewModel.param.postprocessingOrder) 1 else 0,
                            model = "inswapper_128.onnx",
                            genderSource = ReactorViewModel.param.genderDetectionSource,
                            genderTarget = ReactorViewModel.param.genderDetectionTarget,
                            saveToFile = 0,
                            resultFilePath = ""
                        )
                        val response = getApiClient().reactorImage(request)
                        if (response.isSuccessful) {
                            ReactorViewModel.images =
                                ReactorViewModel.images.mapIndexed { index, item ->
                                    if (index == idx) {
                                        item.copy(
                                            resultImage = response.body()?.image,
                                            isGenerating = false
                                        )
                                    } else {
                                        item
                                    }
                                }
                        }
                    }
            }
        } finally {
            ReactorViewModel.isProcessing = false
            ReactorViewModel.stopFlag = false
            ReactorViewModel.images = ReactorViewModel.images.map {
                it.copy(isGenerating = false)
            }
        }

    }


    val galleryMultiImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris ->
            uris.map { uri ->
                ReactorViewModel.images += ImageItem(
                    name = uri.pathSegments.last(),
                    uri = uri
                )
            }
        }

    if (isImagePreviewerOpen) {
        val currentImage = ReactorViewModel.images.getOrNull(currentSelectImageIndex)?.resultImage
        currentImage?.let {
            ImageBase64PreviewDialog(imageBase64 = it, isOpen = false) {
                isImagePreviewerOpen = false
            }
        }
    }

    fun saveToGallery() {
        scope.launch(Dispatchers.Main) {
            totalToExport = ReactorViewModel.images.count { it.isExport }
            currentToExport = 0
            isExporting = true
        }
        ReactorViewModel.images.filter { it.isExport }.forEach { inputItem ->
            val imgBase64 = inputItem.resultImage
            if (imgBase64 != null) {
                val filename =
                    "diffusion_reactor_${UUID.randomUUID().toString().subSequence(0, 6)}.png"
                Util.saveImageBase64ToGallery(imgBase64, filename)


            }
            scope.launch(Dispatchers.Main) {
                currentToExport++
            }
        }
        scope.launch(Dispatchers.Main) {
            isExporting = false
            Toast.makeText(
                context,
                context.getString(R.string.image_saved_to_gallery),
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    if (isResultPreviewDialogVisible) {
        val currentImage = ReactorViewModel.images.getOrNull(currentSelectImageIndex)?.resultImage
        currentImage?.let {
            ImageBase64PreviewDialog(imageBase64 = it, isOpen = false) {
                isResultPreviewDialogVisible = false
            }
        }

    }
    with(ReactorViewModel) {
        if (isParamPanelVisible) {
            ModalBottomSheet(onDismissRequest = {
                isParamPanelVisible = false
            }, sheetState = sheetState) {
                ReactorPanel(
                    onValueChange = {
                        param = it
                    },
                    reactorParam = param,
                    showEnableOption = false
                )
            }
        }

    }
    BackHandler(enabled = isProcessing) {

    }

    @Composable
    fun second() {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        if (!ReactorViewModel.isProcessing) {
                            if (!isEditMode && !isSelectedMode) {
                                IconButton(onClick = {
                                    galleryMultiImageLauncher.launch(
                                        "image/*"
                                    )
                                }) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null
                                    )
                                }
                            }
                            if (!isEditMode) {
                                if (ReactorViewModel.images.any { it.resultImage != null }) {
                                    IconButton(onClick = {
                                        isSelectedMode = !isSelectedMode
                                    }) {
                                        if (isSelectedMode) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = null
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    if (isSelectedMode) {
                                        Spacer(
                                            modifier = Modifier.width(
                                                8.dp
                                            )
                                        )
                                        IconButton(onClick = {
                                            ReactorViewModel.images =
                                                ReactorViewModel.images.map {
                                                    it.copy(isExport = true)
                                                }
                                        }) {
                                            Icon(
                                                imageVector = unselectAllIcon,
                                                contentDescription = null
                                            )
                                        }
                                        Spacer(
                                            modifier = Modifier.width(
                                                8.dp
                                            )
                                        )
                                        IconButton(onClick = {
                                            ReactorViewModel.images =
                                                ReactorViewModel.images.map {
                                                    it.copy(isExport = false)
                                                }
                                        }) {
                                            Icon(
                                                imageVector = selectAllIcon,
                                                contentDescription = null
                                            )
                                        }
                                        Spacer(
                                            modifier = Modifier.width(
                                                8.dp
                                            )
                                        )
                                        IconButton(onClick = {
                                            isSelectedMode = false
                                            scope.launch {
                                                saveToGallery()
                                            }
                                        }) {
                                            Icon(
                                                imageVector = downloadIcon,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }

                            }
                            if (ReactorViewModel.images.isNotEmpty() && !isSelectedMode) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    isEditMode = !isEditMode
                                }) {
                                    if (isEditMode) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null
                                        )
                                    }
                                }
                                if (isEditMode) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = {
                                        ReactorViewModel.images =
                                            emptyList()
                                        isEditMode = false
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(text = stringResource(id = R.string.processing))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val displayList = ReactorViewModel.images.filter {
                        if (isSelectedMode) {
                            it.resultImage != null
                        } else {
                            true
                        }
                    }
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(displayList.size) { idx ->
                            val item = displayList[idx]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .height(120.dp)
                                    .border(
                                        width = if (
                                            currentSelectImageIndex == idx && !isSelectedMode && !isEditMode
                                        ) 4.dp else 0.dp,
                                        color = if (
                                            currentSelectImageIndex == idx && !isSelectedMode && !isEditMode
                                        ) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                            ) {
                                AsyncImage(
                                    model = item.uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (!isSelectedMode && !isEditMode) {
                                                currentSelectImageIndex =
                                                    idx
                                            }
                                        },
                                    contentScale = ContentScale.Fit
                                )
                                if (item.isGenerating) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(
                                                    alpha = 0.5f
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.processing),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                } else {
                                    if (isEditMode) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.7f
                                                    )
                                                )
                                                .clickable {
                                                    ReactorViewModel.images =
                                                        ReactorViewModel.images.filterIndexed { index, _ ->
                                                            index != idx
                                                        }
                                                    if (ReactorViewModel.images.isEmpty()) {
                                                        isEditMode =
                                                            false
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            )
                                        }
                                    }
                                    if (isSelectedMode) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = if (item.isExport) 0.7f else 0f
                                                    )
                                                )
                                                .clickable {
                                                    ReactorViewModel.images =
                                                        ReactorViewModel.images.mapIndexed { index, item ->
                                                            if (index == idx) {
                                                                item.copy(
                                                                    isExport = !item.isExport
                                                                )
                                                            } else {
                                                                item
                                                            }
                                                        }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (item.isExport) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (!ReactorViewModel.isProcessing) {
                    Button(
                        modifier = Modifier
                            .weight(1f),
                        onClick = {
                            isParamPanelVisible = true
                        }
                    ) {
                        Text(text = stringResource(id = R.string.param))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                if (ReactorViewModel.isProcessing) {
                    Button(
                        onClick = {
                            if (ReactorViewModel.isProcessing && !ReactorViewModel.stopFlag) {
                                ReactorViewModel.stopFlag = true
                            }
                        },
                        enabled = !ReactorViewModel.stopFlag
                    ) {
                        Text(text = ReactorViewModel.isProcessing.let {
                            if (ReactorViewModel.stopFlag) {
                                stringResource(R.string.interrupted)
                            } else {
                                stringResource(R.string.btn_stop)
                            }
                        })
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Button(
                    modifier = Modifier
                        .weight(1f),
                    onClick = {
                        if (!ReactorViewModel.isProcessing) {
                            scope.launch {
                                process()
                            }
                        }
                    },
                    enabled = !ReactorViewModel.isProcessing && !isExporting && !isEditMode && !isSelectedMode
                ) {
                    Text(text = ReactorViewModel.isProcessing.let {
                        if (it) {
                            stringResource(R.string.processing)
                        } else {
                            stringResource(R.string.reactor)
                        }
                    })
                }

            }

        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reactor)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {


                }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                val currentImage =
                                    ReactorViewModel.images.getOrNull(currentSelectImageIndex)

                                val imageResult = currentImage?.resultImage
                                val isGenerating = currentImage?.isGenerating ?: false
                                val sourceImage = currentImage?.uri
                                if (isGenerating) {
                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        LinearProgressIndicator()
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = stringResource(id = R.string.processing))
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (imageResult != null) {

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clickable {
                                                            isResultPreviewDialogVisible = true
                                                        }
                                                ) {
                                                    DisplayBase64Image(base64String = imageResult)
                                                }

                                            } else {
                                                Text(text = stringResource(id = R.string.none))
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 8.dp
                                            )
                                        ) {
                                            sourceImage?.let {
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            process(currentSelectImageIndex)
                                                        }
                                                    }
                                                ) {
                                                    Icon(reactorIcon, contentDescription = null)
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                            if (!isWideDisplay) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)

                                ) {
                                    second()
                                }
                            }

                        }

                        if (isWideDisplay) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp)
                            ) {
                                second()
                            }

                        }
                    }
                }
            }
        }
    )
}