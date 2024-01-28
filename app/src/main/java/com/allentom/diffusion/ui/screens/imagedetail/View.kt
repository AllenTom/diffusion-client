package com.allentom.diffusion.ui.screens.imagedetail

import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
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
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.HistoryView
import com.allentom.diffusion.composables.ImageUriPreviewDialog
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.HistoryStore
import com.allentom.diffusion.store.ImageHistory
import com.allentom.diffusion.store.SaveHistory
import com.allentom.diffusion.ui.screens.extra.ExtraImageParam
import com.allentom.diffusion.ui.screens.extra.ExtraPanel
import com.allentom.diffusion.ui.screens.home.HomeViewModel
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
                        DropdownMenuItem(text = {
                            Text(stringResource(id = R.string.saved_to_gallery))
                        }, onClick = {
                            expanded = false
                            galleryItem?.let {
                                Util.copyImageFileToGallery(context, it.path, it.name)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.image_saved_to_gallery),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                genHistory?.let {
                                    scope.launch(Dispatchers.IO) {
                                        DrawViewModel.applyHistory(context,it)
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
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                galleryItem?.path?.let {
                                    val imageBase64 = Util.readImageWithPathToBase64(
                                        it
                                    )
                                    ReactorViewModel.targetImage = imageBase64
                                    ReactorViewModel.targetImageFileName = it.split("/").last()
                                    navController.navigate(Screens.ReactorScreen.route)
                                }

                            },
                            text = { Text(stringResource(R.string.send_to_reactor)) }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // GalleryDetail screen UI
        Column(
            modifier = Modifier.padding(paddingValues),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                genHistory?.let { genHis ->
                    galleryItem?.let { imgHis ->
                        LazyColumn {
                            item {
                                Box(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .wrapContentHeight()
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imgHis.path)
                                            .crossfade(true)
                                            .build(),
                                        contentScale = ContentScale.Fit,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .clickable {
                                                isPreviewDialogOpen = true
                                            }
                                    )
                                }
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    HistoryView(currentHistory = genHis,navController)
                                }
                                Spacer(modifier = Modifier.height(64.dp))
                            }


                        }
                    }

                }
            }
            DrawBar()
        }
    }
}

