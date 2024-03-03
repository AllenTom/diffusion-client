package com.allentom.diffusion.ui.screens.extra

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.allentom.diffusion.api.ExtraImageRequest
import com.allentom.diffusion.api.entity.Upscale
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.ImageBase64PickupOptionItem
import com.allentom.diffusion.composables.ImageBase64PreviewDialog
import com.allentom.diffusion.composables.IsWideWindow
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.extension.toBase64
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraScreen() {
    var isParamDisplayed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val resolver = LocalContext.current.contentResolver
    val downloadIcon = ImageVector.vectorResource(id = R.drawable.ic_download)
    val selectAllIcon = ImageVector.vectorResource(id = R.drawable.ic_select_all)
    val unselectAllIcon = ImageVector.vectorResource(id = R.drawable.ic_unselect_all)
    val extraIcon = ImageVector.vectorResource(id = R.drawable.ic_extra_image)
    var currentSelectImageIndex by remember { mutableIntStateOf(0) }
    var isImagePreviewerOpen by remember { mutableStateOf(false) }
    var isSelectedMode by remember { mutableStateOf(false) }
    var totalToExport by remember { mutableStateOf(0) }
    var currentToExport by remember { mutableStateOf(0) }
    var isExporting by remember { mutableStateOf(false) }
    val isWideDisplay = IsWideWindow()
    LaunchedEffect(Unit) {
        val result = getApiClient().getUpscalers()
        if (result.isSuccessful && result.body() != null) {
            ExtraViewModel.upscalerList = result.body()!!
        }
        AppConfigStore.config.extraImageHistory?.let {
            ExtraViewModel.extraParam = it.copy(image = null)
        }
    }
    var isEditMode by remember { mutableStateOf(false) }
    val galleryMultiImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris ->
            uris.map { uri ->
                ExtraViewModel.inputImages += InputImageItem(
                    name = uri.pathSegments.last(),
                    uri = uri
                )
            }
        }

    fun saveToGallery() {
        scope.launch(Dispatchers.Main) {
            totalToExport = ExtraViewModel.inputImages.count { it.isExport }
            currentToExport = 0
            isExporting = true
        }
        ExtraViewModel.inputImages.filter { it.isExport }.forEach { inputItem ->
            val imgBase64 = inputItem.resultImage
            val imgName = inputItem.name
            if (imgName != null && imgBase64 != null) {
                val decodedString = Base64.decode(imgBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "upscale_${imgName}")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/DiffusionUpscale"
                    )
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    val outputStream: OutputStream? = resolver.openOutputStream(it)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    outputStream?.close()
                }

            }
            scope.launch(Dispatchers.Main) {
                currentToExport++
            }
        }
        scope.launch(Dispatchers.Main) {
            isExporting = false
        }
    }

    suspend fun upscaleImage(withIndex: Int? = null) {
        ExtraViewModel.isProcessing = true
        try {
            ExtraViewModel.onStartGenerating(withIndex)
            ExtraViewModel.inputImages.forEachIndexed { idx, inputItem ->
                if (ExtraViewModel.stopFlag) {
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
                        val result = getApiClient().extraSingleImage(
                            ExtraImageRequest(
                                resizeMode = ExtraViewModel.extraParam.resizeMode,
                                showExtrasResults = ExtraViewModel.extraParam.showExtrasResults,
                                gfpganVisibility = ExtraViewModel.extraParam.gfpganVisibility,
                                codeformerVisibility = ExtraViewModel.extraParam.codeformerVisibility,
                                codeformerWeight = ExtraViewModel.extraParam.codeformerWeight,
                                upscalingResize = ExtraViewModel.extraParam.upscalingResize,
                                upscalingResizeW = ExtraViewModel.extraParam.upscalingResizeW,
                                upscalingResizeH = ExtraViewModel.extraParam.upscalingResizeH,
                                upscalingCrop = ExtraViewModel.extraParam.upscalingCrop,
                                upscaler1 = ExtraViewModel.extraParam.upscaler1,
                                upscaler2 = ExtraViewModel.extraParam.upscaler2,
                                extrasUpscaler2Visibility = ExtraViewModel.extraParam.extrasUpscaler2Visibility,
                                upscaleFirst = ExtraViewModel.extraParam.upscaleFirst,
                                image = base64
                            )
                        )
                        if (result.isSuccessful && result.body() != null) {
                            ExtraViewModel.inputImages = ExtraViewModel.inputImages.map {
                                if (it.name == inputItem.name) {
                                    it.copy(
                                        resultImage = result.body()!!.image,
                                        isGenerating = false
                                    )
                                } else {
                                    it
                                }
                            }
                        }
                    }
            }
            ExtraViewModel.saveToCache(context)
        } finally {
            ExtraViewModel.isProcessing = false
            ExtraViewModel.stopFlag = false
            ExtraViewModel.inputImages = ExtraViewModel.inputImages.map {
                it.copy(isGenerating = false)
            }
        }

    }
    if (isImagePreviewerOpen && ExtraViewModel.inputImages.isNotEmpty()) {
        val displayItem = ExtraViewModel.inputImages[currentSelectImageIndex]
        displayItem.resultImage?.let {
            ImageBase64PreviewDialog(
                imageBase64 = it,
                isOpen = isImagePreviewerOpen,
                onDismissRequest = {
                    isImagePreviewerOpen = false
                })
        }
    }
    @Composable
    fun secondContent() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                if (!ExtraViewModel.isProcessing) {
                    if (!isEditMode && !isSelectedMode) {
                        IconButton(onClick = { galleryMultiImageLauncher.launch("image/*") }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                    if (!isEditMode) {
                        if (ExtraViewModel.inputImages.any { it.resultImage != null }) {
                            IconButton(onClick = {
                                isSelectedMode = !isSelectedMode
                            }) {
                                if (isSelectedMode) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                            if (isSelectedMode) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    ExtraViewModel.inputImages = ExtraViewModel.inputImages.map {
                                        it.copy(isExport = true)
                                    }
                                }) {
                                    Icon(imageVector = unselectAllIcon, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    ExtraViewModel.inputImages = ExtraViewModel.inputImages.map {
                                        it.copy(isExport = false)
                                    }
                                }) {
                                    Icon(imageVector = selectAllIcon, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = {
                                    isSelectedMode = false
                                    scope.launch {
                                        saveToGallery()
                                    }
                                }) {
                                    Icon(imageVector = downloadIcon, contentDescription = null)
                                }
                            }
                        }

                    }
                    if (ExtraViewModel.inputImages.isNotEmpty() && !isSelectedMode) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            isEditMode = !isEditMode
                        }) {
                            if (isEditMode) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            } else {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        }
                        if (isEditMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                ExtraViewModel.inputImages = emptyList()
                                isEditMode = false
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    }
                } else {
                    Text(text = stringResource(id = R.string.processing))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val displayList = ExtraViewModel.inputImages.filter {
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
                                        currentSelectImageIndex = idx
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
                                            ExtraViewModel.inputImages =
                                                ExtraViewModel.inputImages.filterIndexed { index, _ ->
                                                    index != idx
                                                }
                                            if (ExtraViewModel.inputImages.isEmpty()) {
                                                isEditMode = false
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Delete, contentDescription = null,
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
                                            ExtraViewModel.inputImages =
                                                ExtraViewModel.inputImages.mapIndexed { index, item ->
                                                    if (index == idx) {
                                                        item.copy(isExport = !item.isExport)
                                                    } else {
                                                        item
                                                    }
                                                }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.isExport) {
                                        Icon(
                                            Icons.Default.Check, contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (!ExtraViewModel.isProcessing) {
                    Button(
                        modifier = Modifier
                            .weight(1f),
                        onClick = {
                            isParamDisplayed = true
                        }
                    ) {
                        Text(text = stringResource(id = R.string.param))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                if (ExtraViewModel.isProcessing) {
                    Button(
                        onClick = {
                            if (ExtraViewModel.isProcessing && !ExtraViewModel.stopFlag) {
                                ExtraViewModel.stopFlag = true
                            }
                        },
                        enabled = !ExtraViewModel.stopFlag
                    ) {
                        Text(text = ExtraViewModel.isProcessing.let {
                            if (ExtraViewModel.stopFlag) {
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
                        if (!ExtraViewModel.isProcessing) {
                            scope.launch {
                                upscaleImage()
                            }
                        }
                    },
                    enabled = !ExtraViewModel.isProcessing && !isExporting && !isEditMode && !isSelectedMode
                ) {
                    Text(text = ExtraViewModel.isProcessing.let {
                        if (it) {
                            stringResource(R.string.processing)
                        } else {
                            stringResource(R.string.upscale)
                        }
                    })
                }

            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.image_extra_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { paddingValues ->
        if (isParamDisplayed) {
            ExtraPanel(
                param = ExtraViewModel.extraParam,
                onUpdateParam = {
                    ExtraViewModel.extraParam = it
                },
                onDismissRequest = {
                    isParamDisplayed = false
                },
                onSelectImage = { img, filename, width, height ->
                    ExtraViewModel.imageName = filename
                },
                upscalerList = ExtraViewModel.upscalerList,
                usePickImage = false
            )
        }
        Row(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val currentImage = ExtraViewModel.inputImages.getOrNull(currentSelectImageIndex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
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
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (imageResult != null) {
                                        DisplayBase64Image(base64String = imageResult)

                                    } else {
                                        Text(text = stringResource(id = R.string.none))
                                    }
                                }
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    sourceImage?.let {
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    upscaleImage(currentSelectImageIndex)
                                                }
                                            }
                                        ) {
                                            Icon(extraIcon, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!isWideDisplay) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        secondContent()

                    }
                }
            }
            if (isWideDisplay) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    secondContent()
                }
            }


        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraPanel(
    upscalerList: List<Upscale>,
    param: ExtraImageParam,
    onSelectImage: (imgBase64: String, filename: String, width: String, height: String) -> Unit = { _, _, _, _ ->
    },
    onUpdateParam: (param: ExtraImageParam) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    usePickImage: Boolean = true,
    footer: (@Composable () -> Unit)? = {}
) {
    val upscaleModeList = listOf("Scale by", "Scale to")

    ModalBottomSheet(onDismissRequest = {
        onDismissRequest()
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()

        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item {
                    if (usePickImage) {
                        ImageBase64PickupOptionItem(
                            label = stringResource(id = R.string.source),
                            value = param.image
                        ) { _, img, filename, _, _ ->
                            onUpdateParam(param.copy(image = img))
                            onSelectImage(img, filename, "", "")
                        }
                    }
                    SliderOptionItem(
                        label = stringResource(R.string.param_gfpgan_visibility),
                        value = param.gfpganVisibility,
                        onValueChangeFloat = {
                            onUpdateParam(param.copy(gfpganVisibility = it))
                        },
                        baseFloat = 0.01f,
                        valueRange = 0f..1f
                    )
                    SliderOptionItem(
                        label = stringResource(R.string.codeformer_visibility),
                        value = param.codeformerVisibility,
                        onValueChangeFloat = {
                            onUpdateParam(param.copy(codeformerVisibility = it))
                        },
                        baseFloat = 0.01f,
                        valueRange = 0f..1f
                    )
                    SliderOptionItem(
                        label = stringResource(R.string.codeformer_weight),
                        value = param.codeformerWeight,
                        onValueChangeFloat = {
                            onUpdateParam(param.copy(codeformerWeight = it))
                        },
                        baseFloat = 0.01f,
                        valueRange = 0f..1f
                    )
                    TextPickUpItem(
                        label = "Scale mode",
                        value = upscaleModeList[param.resizeMode],
                        options = upscaleModeList,
                    ) {
                        onUpdateParam(param.copy(resizeMode = upscaleModeList.indexOf(it)))
                    }
                    if (param.resizeMode == 0) {
                        SliderOptionItem(
                            label = stringResource(R.string.upscaling_resize),
                            value = param.upscalingResize,
                            onValueChangeFloat = {
                                onUpdateParam(param.copy(upscalingResize = it))
                            },
                            baseFloat = 0.05f,
                            valueRange = 1f..8f
                        )
                    }
                    if (param.resizeMode == 1) {
                        SliderOptionItem(
                            label = stringResource(R.string.upscaling_resize_w),
                            useInt = true,
                            value = param.upscalingResizeW.toFloat(),
                            onValueChangeInt = {
                                onUpdateParam(param.copy(upscalingResizeW = it))
                            },
                            valueRange = 64f..2048f
                        )
                        SliderOptionItem(
                            label = stringResource(R.string.upscaling_resize_h),
                            useInt = true,
                            value = param.upscalingResizeH.toFloat(),
                            onValueChangeInt = {
                                onUpdateParam(param.copy(upscalingResizeH = it))
                            },
                            valueRange = 64f..2048f
                        )
                        SwitchOptionItem(
                            label = stringResource(R.string.crop_to_fit),
                            value = param.upscalingCrop,
                            onValueChange = {
                                onUpdateParam(param.copy(upscalingCrop = it))
                            })
                    }


                    TextPickUpItem(
                        label = stringResource(R.string.upscaler_1),
                        value = param.upscaler1,
                        options = upscalerList.map { it.name },
                        onValueChange = {
                            onUpdateParam(param.copy(upscaler1 = it))
                        }
                    )
                    TextPickUpItem(
                        label = stringResource(R.string.upscaler_2),
                        value = param.upscaler2,
                        options = upscalerList.map { it.name },
                        onValueChange = {
                            onUpdateParam(param.copy(upscaler2 = it))
                        }
                    )
                    SliderOptionItem(
                        label = stringResource(R.string.extras_upscaler_2_visibility),
                        value = param.extrasUpscaler2Visibility.toFloat(),
                        onValueChangeFloat = {
                            onUpdateParam(param.copy(extrasUpscaler2Visibility = it.toInt()))
                        },
                        baseFloat = 0.01f,
                        valueRange = 0f..1f
                    )

                }
            }
            footer?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    footer()
                }
            }

        }

    }
}