package com.allentom.diffusion.ui.screens.reactor

import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.ReactorRequestBody
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.ImageBase64PreviewDialog
import com.allentom.diffusion.composables.IsWideWindow
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image
import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorPanel
import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    val downloadIcon = ImageVector.vectorResource(id = R.drawable.ic_download)
    var isResultPreviewDialogVisible by remember {
        mutableStateOf(false)
    }
    fun process() {
        scope.launch {
            try {
                isProcessing = true
                val request = ReactorRequestBody(
                    sourceImage = ReactorViewModel.param.singleImageResult!!,
                    targetImage = ReactorViewModel.targetImage!!,
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
                    ReactorViewModel.resultImage = response.body()?.image
                }
            } catch (e: Exception) {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }

            } finally {
                isProcessing = false
            }

        }


    }

    fun saveResultToDeviceGallery() {
        val imageContent = ReactorViewModel.resultImage ?: return
        val filename = "diffusion_reactor_${UUID.randomUUID().toString().subSequence(0, 6)}.png"
        Util.saveImageBase64ToGallery(imageContent, filename)
        Toast.makeText(
            context,
            context.getString(R.string.image_saved_to_gallery),
            Toast.LENGTH_SHORT
        ).show()
    }

    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                val imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                if (imageBase64 != null) {
                    if (pickupTarget == "source") {
                        ReactorViewModel.param = ReactorViewModel.param.copy(
                            singleImageResult = imageBase64,
                            singleImageResultFilename = it.lastPathSegment.toString(),
                        )
                    } else {
                        ReactorViewModel.targetImage = imageBase64
                        ReactorViewModel.targetImageFileName = it.lastPathSegment.toString()
                    }
                }
                // Use the base64 string
            }
        }


    fun pickImageFromGalleryAndConvertToBase64() {
        pickImageLauncher.launch("image/*")
    }
    if (isResultPreviewDialogVisible) {
        ReactorViewModel.resultImage?.let {
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize()
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
                                    ReactorViewModel.resultImage?.let {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .clickable {
                                                        isResultPreviewDialogVisible = true
                                                    }

                                            ){
                                                DisplayBase64Image(base64String = it)
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        saveResultToDeviceGallery()
                                                    }) {
                                                    Icon(
                                                        imageVector = downloadIcon,
                                                        contentDescription = "Save to gallery"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (isProcessing) {
                                        Text(text = "Processing...")
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.5f)
                                        .clickable {
                                            pickupTarget = "target"
                                            pickImageFromGalleryAndConvertToBase64()
                                        },
                                    contentAlignment = Alignment.Center


                                ) {
                                    ReactorViewModel.targetImage?.let {
                                        DisplayBase64Image(base64String = it)
                                    }
                                    if (ReactorViewModel.targetImage == null) {
                                        Text(text = stringResource(id = R.string.click_to_pick_image))
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()

                                ) {
                                    if (!isWideDisplay) {
                                        Button(
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                isParamPanelVisible = true
                                            },
                                            enabled = !isProcessing
                                        ) {
                                            Text(text = stringResource(R.string.param))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }

                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            process()

                                        },
                                        enabled = !isProcessing
                                    ) {
                                        Text(text = "Process")
                                    }
                                }
                            }

                            if (isWideDisplay) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    ReactorPanel(
                                        onValueChange = {
                                            ReactorViewModel.param = it
                                        },
                                        reactorParam = ReactorViewModel.param,
                                        showEnableOption = false
                                    )
                                }

                            }
                        }

                    }
                }
            }
        }
    )
}