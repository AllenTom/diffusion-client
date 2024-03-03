package com.allentom.diffusion.ui.screens.home.tabs.draw

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.DetectDeviceType
import com.allentom.diffusion.composables.DeviceType
import com.allentom.diffusion.composables.ImageBase64PreviewDialog
import com.allentom.diffusion.ui.parts.GenProgressGrid
import com.allentom.diffusion.ui.screens.home.tabs.draw.panels.ParamsModalBottomSheet
import com.allentom.diffusion.ui.screens.home.tabs.draw.panels.ParamsPanel
import com.origeek.imageViewer.previewer.ImagePreviewer
import com.origeek.imageViewer.previewer.rememberPreviewerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun DisplayBase64Image(base64String: String) {
    val imageBitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(base64String) {
        withContext(Dispatchers.IO) {
            val decodedString = Base64.decode(base64String, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imageBitmap.value = decodedByte
        }
    }
    imageBitmap.value?.let { bitmap ->
        AsyncImage(
            model = bitmap,
            contentDescription = "Base64 Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawScreen() {
    var isParamDisplayed by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val genItemList = DrawViewModel.runningTask?.currentTask?.genItemList ?: emptyList()
    val displayResultIndex = DrawViewModel.runningTask?.currentTask?.displayResultIndex ?: 0
    val imageViewerState = rememberPreviewerState(pageCount = { genItemList.size })
    var isImagePreviewerOpen by remember {
        mutableStateOf(false)
    }
    val deviceType = DetectDeviceType()

    if (DrawViewModel.isSwitchingModel) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(id = R.string.dialog_switch_model_title)) },
            text = { Text(stringResource(id = R.string.dialog_switch_model_content)) },
            confirmButton = { },
            dismissButton = { }
        )
    }

    if (isImagePreviewerOpen && displayResultIndex < genItemList.size) {
        val displayItem = genItemList[displayResultIndex]
        displayItem.getDisplayImageBase64()?.let {
            ImageBase64PreviewDialog(
                imageBase64 = it,
                isOpen = isImagePreviewerOpen,
                onDismissRequest = {
                    isImagePreviewerOpen = false
                })
        }
    }
    if (isParamDisplayed) {
        ParamsModalBottomSheet(
            onDismissRequest = {
                isParamDisplayed = false
            },
            onSwitchModel = {
                scope.launch {
                    DrawViewModel.switchModel(it)
                }
            },
            onSwitchVae = {
                scope.launch {
                    DrawViewModel.switchVae(it)
                }
            }
        )
    }
    Row {
        Box(
            modifier = Modifier
                .weight(1f)

        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    GenProgressGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizonLayout = deviceType == DeviceType.Tablet
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row {
                            if (deviceType == DeviceType.Phone) {
                                Button(
                                    onClick = {
                                        isParamDisplayed = true
                                    }) {
                                    Text(text = stringResource(id = R.string.params))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    DrawViewModel.startGenerate(context = context)
                                }) {
                                Icon(
                                    DrawViewModel.isGenerating.let { if (it) Icons.Filled.Add else Icons.Filled.Create },
                                    contentDescription = "Generate"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = DrawViewModel.isGenerating.let {
                                    if (it) stringResource(R.string.add_to_queue) else stringResource(
                                        id = R.string.draw_generate
                                    )
                                })
                            }
                        }
                    }
                }
                ImagePreviewer(
                    state = imageViewerState,
                    imageLoader = { index ->
                        val imgItem = genItemList[index]
                        val imageBase64 = imgItem.getDisplayImageBase64()
                        val decodedString = Base64.decode(imageBase64, Base64.DEFAULT)
                        val decodedByte =
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        BitmapPainter(decodedByte.asImageBitmap())
                    },
                    detectGesture = {
                        onTap = {
                            scope.launch {
                                imageViewerState.close()
                            }
                        }
                    },
                )
            }
        }
        if (deviceType != DeviceType.Phone) {
            Box(
                modifier = Modifier
                    .weight(
                        if (deviceType == DeviceType.Tablet) 0.75f
                        else 1f
                    )
                    .fillMaxWidth()

            ) {
                (deviceType != DeviceType.Phone).takeIf { it }.let {
                    ParamsPanel(
                        onSwitchVae = {
                            scope.launch {
                                DrawViewModel.switchVae(it)
                            }
                        },
                        onSwitchModel = {
                            scope.launch {
                                DrawViewModel.switchModel(it)
                            }
                        }
                    )
                }
            }
        }

    }
}