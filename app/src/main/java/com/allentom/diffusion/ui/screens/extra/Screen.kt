package com.allentom.diffusion.ui.screens.extra

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R
import com.allentom.diffusion.api.ExtraImageRequest
import com.allentom.diffusion.api.entity.Upscale
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.ImageBase64PickupOptionItem
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.SwitchOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image
import kotlinx.coroutines.launch
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraScreen() {
    var isParamDisplayed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val resolver = LocalContext.current.contentResolver
    val downloadIcon = ImageVector.vectorResource(id = R.drawable.ic_download)
    LaunchedEffect(UInt) {
        val result = getApiClient().getUpscalers()
        if (result.isSuccessful && result.body() != null) {
            ExtraViewModel.upscalerList = result.body()!!
        }
        AppConfigStore.config.extraImageHistory?.let {
            ExtraViewModel.extraParam = it.copy(image = null)
        }
    }
    fun saveToGallery() {
        val imgBase64 = ExtraViewModel.Resultimage
        val imgName = ExtraViewModel.imageName
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
            Toast.makeText(context, context.getString(R.string.saved_to_gallery), Toast.LENGTH_SHORT).show()
        }

    }

    suspend fun upscaleImage() {
        val inputImage = ExtraViewModel.extraParam.image ?: return
        ExtraViewModel.isProcessing = true
        try {
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
                    image = inputImage
                )
            )
            if (result.isSuccessful && result.body() != null) {
                ExtraViewModel.Resultimage = result.body()!!.image
            }
            ExtraViewModel.saveToCache(context)
        } finally {
            ExtraViewModel.isProcessing = false
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
                upscalerList = ExtraViewModel.upscalerList
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (ExtraViewModel.Resultimage != null) {
                    DisplayBase64Image(base64String = ExtraViewModel.Resultimage!!)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp)
            ) {
                ExtraViewModel.Resultimage?.let {
                    IconButton(onClick = {
                        saveToGallery()
                    }) {
                        Icon(
                            downloadIcon,
                            contentDescription = null,
                        )
                    }
                }

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                }
                Spacer(modifier = Modifier.width(16.dp))
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
                    enabled = !(ExtraViewModel.isProcessing || ExtraViewModel.extraParam.image == null)
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
                        ) { _,img, filename, _, _ ->
                            onUpdateParam(param.copy(image = img))
                            onSelectImage(img, filename, "", "")
                        }
                    }
                    SwitchOptionItem(
                        label = "Show extras results",
                        value = param.showExtrasResults,
                        onValueChange = {
                            onUpdateParam(param.copy(showExtrasResults = it))
                        })
                    SliderOptionItem(
                        label = stringResource(R.string.param_gfpgan_visibility),
                        value = param.gfpganVisibility,
                        onValueChangeFloat = {
                            onUpdateParam(param.copy(gfpganVisibility = it))
                        },
                        valueRange = 0f..1f
                    )
                    SliderOptionItem(
                        label = stringResource(R.string.codeformer_visibility),
                        value = param.codeformerVisibility,
                        onValueChangeFloat = {
                            onUpdateParam(param.copy(codeformerVisibility = it))
                        },
                        valueRange = 0f..1f
                    )
                    SliderOptionItem(
                        label = stringResource(R.string.codeformer_weight),
                        value = param.codeformerWeight,
                        onValueChangeFloat = {
                            onUpdateParam(param.copy(codeformerWeight = it))
                        },
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