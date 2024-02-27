package com.allentom.diffusion.composables

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R
import com.allentom.diffusion.extension.thenIf
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.ui.DrawBarViewModel
import com.allentom.diffusion.ui.parts.GenProgressGrid
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.panels.ParamsModalBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawBar(
    extraContent: (@Composable () -> Unit)? = null
) {
    val deviceType = DetectDeviceType()
    val isTablet = deviceType == DeviceType.Tablet
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isParamsModalShow by remember { mutableStateOf(false) }
    var isImageModalShow by remember { mutableStateOf(false) }
    var currentPanelIndex by remember {
        mutableStateOf(0)
    }


    var isParamOpen by remember { mutableStateOf(false) }
    var sendOptionOpen by remember { mutableStateOf(false) }
    var sendOptionContextPrompts by remember { mutableStateOf<List<Prompt>>(emptyList()) }
    val imageBase64 = DrawBarViewModel.imageBase64


    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                DrawBarViewModel.imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            }
        }

    if (isParamOpen) {
        ModalBottomSheet(
            modifier = Modifier.thenIf(
                isTablet,
                Modifier.fillMaxWidth()
            ),
            onDismissRequest = {
            isParamOpen = false
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                TextPickUpItem(
                    label = stringResource(R.string.tagger_mode),
                    value = DrawBarViewModel.useTaggerName,
                    options = listOf("deepdanbooru", "clip"),
                    onValueChange = {
                        DrawBarViewModel.useTaggerName = it
                    })
            }
        }
    }


    fun pickImageFromGalleryAndConvertToBase64() {
        pickImageLauncher.launch("image/*")
    }

    if (sendOptionOpen) {
        BottomActionSheet(items = listOf(
            ActionItem(
                text = stringResource(id = R.string.append_to_prompt),
                onAction = {
                    sendOptionContextPrompts.forEach {
                        DrawViewModel.addInputPrompt(it)
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.append_to_prompt_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ),
            ActionItem(
                text = stringResource(id = R.string.assign_to_prompt),
                onAction = {
                    DrawViewModel.baseParam = DrawViewModel.baseParam.copy(
                        promptText = sendOptionContextPrompts
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.assign_to_prompt_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ),
            ActionItem(
                text = stringResource(id = R.string.append_to_negative_prompt),
                onAction = {
                    sendOptionContextPrompts.forEach {
                        DrawViewModel.addInputNegativePrompt(it)
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.append_to_negative_prompt_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ),
            ActionItem(
                text = stringResource(R.string.assign_negative_prompt),
                onAction = {
                    DrawViewModel.baseParam = DrawViewModel.baseParam.copy(
                        negativePromptText = sendOptionContextPrompts
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.assign_to_negative_prompt_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        ),
            onDismiss = {
                sendOptionOpen = false

            }
        )
    }

    if (DrawViewModel.isSwitchingModel) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(id = R.string.dialog_switch_model_title)) },
            text = { Text(stringResource(id = R.string.dialog_switch_model_content)) },
            confirmButton = { },
            dismissButton = { }
        )
    }
    if (isParamsModalShow) {
        ParamsModalBottomSheet(
            onDismissRequest = {
                isParamsModalShow = false
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

    if (isImageModalShow) {
        ModalBottomSheet(
            onDismissRequest = {
                isImageModalShow = false
            },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                Row {
                    FilterChip(selected = currentPanelIndex == 0, onClick = {
                        currentPanelIndex = 0
                    }, label = {
                        Text(text = stringResource(id = R.string.current))
                    })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = currentPanelIndex == 1, onClick = {
                        currentPanelIndex = 1
                    }, label = {
                        Text(text = stringResource(id = R.string.tools_caption))
                    })
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                when (currentPanelIndex) {
                    0 -> Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        GenProgressGrid(modifier = Modifier.fillMaxSize(), horizonLayout = isTablet)
                    }

                    1 ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clickable {
                                        pickImageFromGalleryAndConvertToBase64()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageBase64 != null) {
                                    imageBase64.let { base64 ->
                                        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                                        val bitmap = BitmapFactory.decodeByteArray(
                                            imageBytes,
                                            0,
                                            imageBytes.size
                                        )
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = stringResource(R.string.selected_image),
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                } else {
                                    Text(text = stringResource(R.string.click_to_pick_image))
                                }

                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                if (DrawBarViewModel.caption.isNotEmpty()) {
                                    PromptDisplayView(
                                        promptList = DrawBarViewModel.caption.map {
                                            Prompt(
                                                text = it,
                                                piority = 0
                                            )
                                        },
                                        canScroll = true,
                                    ) {
                                        sendOptionContextPrompts = it
                                        sendOptionOpen = true

                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        isParamOpen = true
                                    },
                                    enabled = !DrawBarViewModel.isCaptioning
                                ) {
                                    Text(text = stringResource(id = R.string.param))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            DrawBarViewModel.onCaption()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !DrawBarViewModel.isCaptioning
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = stringResource(id = R.string.tools_caption))
                                }
                            }


                        }
                }

            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(
                        role = null,
                    ) {
                        if (!DrawViewModel.isGenerating) {
                            isParamsModalShow = true
                        }
                    }
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(id = R.string.params)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            if (DrawViewModel.isGenerating) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.add_to_queue)
                    )
                }
            } else {
                Button(
                    modifier = Modifier
                        .weight(1f),
                    onClick = {
                        if (!DrawViewModel.isGenerating) {
                            DrawViewModel.startGenerate(context)
                        }
                    },
                ) {
                    Text(text = stringResource(id = R.string.draw_generate))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            Button(
                shape = CircleShape,
                onClick = {
                    isImageModalShow = true
                }) {
                Icon(Icons.Default.Menu, contentDescription = null)
            }
            if (extraContent != null) {
                extraContent()
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}