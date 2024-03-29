package com.allentom.diffusion.ui.screens.imagedetail

import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.entity.Upscale
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.DetectDeviceType
import com.allentom.diffusion.composables.DeviceType
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.HistoryView
import com.allentom.diffusion.composables.ImageUriPreviewDialog
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.history.HistoryStore
import com.allentom.diffusion.store.history.ImageHistory
import com.allentom.diffusion.store.history.SaveHistory
import com.allentom.diffusion.ui.screens.extra.ExtraImageParam
import com.allentom.diffusion.ui.screens.extra.ExtraPanel
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.reactor.ReactorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetail(id: String, navController: NavController) {
    var galleryItem by remember { mutableStateOf<ImageHistory?>(null) }
    var genHistory by remember { mutableStateOf<SaveHistory?>(null) }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var isParamDisplayed by rememberSaveable { mutableStateOf(false) }
    var extraParam by rememberSaveable { mutableStateOf(ExtraImageParam()) }
    val scope = rememberCoroutineScope()
    var isPreviewDialogOpen by remember { mutableStateOf(false) }
    var isUpscaling by remember { mutableStateOf(false) }
    var upscalerList by remember { mutableStateOf(emptyList<Upscale>()) }
    var useDevice = DetectDeviceType()
    fun refresh() {
        scope.launch(Dispatchers.IO) {
            val imageHistory =
                HistoryStore.getImageHistoryWithName(context, ImageDetailViewModel.imageName)
                    ?: return@launch
            galleryItem = imageHistory
            val imgGenHistory = HistoryStore.getHistoryById(context, imageHistory.historyId)
            if (imgGenHistory != null) {
                genHistory = imgGenHistory
            }
        }
    }
    LaunchedEffect(Unit) {
        if (id != "") {
            ImageDetailViewModel.imageName = id
        }
        refresh()
        val result = getApiClient().getUpscalers()
        result.body()?.let {
            upscalerList = it
        }
        AppConfigStore.config.extraImageHistory?.let {
            extraParam = it

        }
    }

    suspend fun upscale() {
        val imagePath = galleryItem?.path ?: return
        isUpscaling = true
        val imageBase64: String = Util.convertImageToBase64(imagePath, context)
        extraParam = extraParam.copy(image = imageBase64)


        val result = getApiClient().extraSingleImage(extraParam.toExtraImageRequestBody())

        result.body()?.image?.let {
            // Decode the Base64 string to a byte array
            val imageBytes = Base64.decode(it, Base64.DEFAULT)

            // Write the byte array to the same file, replacing the old file
            val file = File(imagePath)
            file.writeBytes(imageBytes)
            refresh()
        }
        isUpscaling = false
        isParamDisplayed = false
        AppConfigStore.config =
            AppConfigStore.config.copy(extraImageHistory = extraParam.copy(image = null))
        AppConfigStore.saveData(context)
    }
    if (isParamDisplayed) {
        ExtraPanel(
            upscalerList = upscalerList,
            param = extraParam,
            onDismissRequest = { isParamDisplayed = false },
            onUpdateParam = { extraParam = it },
            usePickImage = false,
            footer = {
                Button(
                    onClick = {
                        scope.launch {
                            upscale()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = !isUpscaling

                ) {
                    Text(text = isUpscaling.let {
                        if (it) {
                            stringResource(R.string.upscaling)
                        } else {
                            stringResource(R.string.upscale)
                        }
                    })
                }
            }
        )
    }
    if (isPreviewDialogOpen) {
        galleryItem?.let {
            ImageUriPreviewDialog(
                imageUri = Uri.parse(it.path),
                onDismissRequest = { isPreviewDialogOpen = false },
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.gen_image_detail_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More actions")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                isParamDisplayed = true
                                expanded = false
                            },
                            text = { Text(stringResource(id = R.string.upscale)) }
                        )
                        Divider()
                        if (DrawViewModel.enableReactorFeat) {
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    galleryItem?.path?.let {
                                        ReactorViewModel.addToReactorImages(
                                            Uri.parse(it),
                                            it.substringAfterLast("/")
                                        )
                                        navController.navigate(Screens.ReactorScreen.route)
                                    }

                                },
                                text = { Text(stringResource(R.string.send_to_reactor)) }
                            )
                        }


                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                genHistory?.let {
                                    scope.launch(Dispatchers.IO) {
                                        DrawViewModel.applyHistory(context, it)
                                        scope.launch(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.params_applied),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                }
                            },
                            text = { Text(stringResource(R.string.apply_params)) }
                        )
                        Divider()
                        DropdownMenuItem(text = {
                            Text(stringResource(id = R.string.save_to_device_gallery))
                        }, onClick = {
                            expanded = false
                            galleryItem?.let {
                                it.saveToDeviceGallery(context = context)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.image_saved_to_gallery),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                }
            )
        }
    ) { paddingValues ->
        // GalleryDetail screen UI
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    genHistory?.let { genHis ->
                        galleryItem?.let { imgHis ->
                            if (useDevice == DeviceType.Phone) {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .fillMaxSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .height(200.dp)
                                            .fillMaxWidth()
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(imgHis.path)
                                                .crossfade(true)
                                                .build(),
                                            contentScale = ContentScale.Fit,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable {
                                                    isPreviewDialogOpen = true
                                                }
                                        )
                                    }

                                    Box(
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        HistoryView(
                                            currentHistory = genHis,
                                            navController = navController,
                                            onPromptUpdate = {
                                                genHistory = genHistory?.copy(
                                                    prompt = it
                                                )
                                            },
                                            onNegativePromptUpdate = {
                                                genHistory = genHistory?.copy(
                                                    negativePrompt = it
                                                )
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(64.dp))

                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize().padding(16.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imgHis.path)
                                            .crossfade(true)
                                            .build(),
                                        contentScale = ContentScale.Fit,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                isPreviewDialogOpen = true
                                            }
                                    )
                                }

                            }
                        }

                    }
                }
                if (useDevice != DeviceType.Phone) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 64.dp)
                    ) {
                        genHistory?.let { genHis ->
                            HistoryView(
                                currentHistory = genHis,
                                navController = navController,
                                onPromptUpdate = {
                                    genHistory = genHistory?.copy(
                                        prompt = it
                                    )
                                },
                                onNegativePromptUpdate = {
                                    genHistory = genHistory?.copy(
                                        negativePrompt = it
                                    )
                                }
                            )
                        }
                    }
                }
            }
            DrawBar()
        }
    }
}

