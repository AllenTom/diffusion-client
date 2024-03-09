package com.allentom.diffusion.ui.screens.civitai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.civitai.entities.CivitaiImageItem
import com.allentom.diffusion.composables.BatchTranslatePromptDialog
import com.allentom.diffusion.composables.DetectDeviceType
import com.allentom.diffusion.composables.DeviceType
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.ImageUriPreviewDialogWrapper
import com.allentom.diffusion.composables.PromptAction
import com.allentom.diffusion.composables.PromptActionState
import com.allentom.diffusion.composables.PromptDisplayView
import com.allentom.diffusion.composables.rememberImagePreviewDialogState
import com.allentom.diffusion.store.civitai.CivitaiImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable()
fun CivitaiModelImageScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val useDevice = DetectDeviceType()
    var onlyDisplayTranslated by remember {
        mutableStateOf(false)
    }
    var imageItem by remember {
        mutableStateOf(null as CivitaiImage?)
    }

    fun refreshData() {
        scope.launch(Dispatchers.IO) {
            CivitaiImageViewModel.image?.let {
                imageItem = CivitaiImage.fromCivitaiImageItem(context, it)
            }
        }
    }
    LaunchedEffect(Unit) {
        refreshData()
    }
    val prompt = imageItem?.promptList ?: emptyList()
    val negativePrompt = imageItem?.negativePromptList ?: emptyList()
    val promptActionState = PromptActionState()

    var isPromptTranslateDialogOpen by remember {
        mutableStateOf(false)
    }
    var isNegativePromptTranslateDialogOpen by remember {
        mutableStateOf(false)
    }
    PromptAction(actionState = promptActionState)

    val previewDialogState = rememberImagePreviewDialogState()
    ImageUriPreviewDialogWrapper(state = previewDialogState)
    if (isPromptTranslateDialogOpen) {
        BatchTranslatePromptDialog(
            onDismiss = {
                isPromptTranslateDialogOpen = false
            },
            inputPrompts = prompt
        ) { updatedPrompt ->
            imageItem?.let {
                imageItem = it.copy(
                    promptList = it.promptList.map {
                        val needToUpdated =
                            updatedPrompt.find { updatedPrompt -> updatedPrompt.text == it.text }
                        needToUpdated ?: it
                    }
                )
            }

        }
    }
    if (isNegativePromptTranslateDialogOpen) {
        BatchTranslatePromptDialog(
            onDismiss = {
                isNegativePromptTranslateDialogOpen = false
            },
            inputPrompts = negativePrompt
        ) { updatedPrompt ->
            imageItem?.let {
                imageItem = it.copy(
                    negativePromptList = it.negativePromptList.map {
                        val needToUpdated =
                            updatedPrompt.find { updatedPrompt -> updatedPrompt.text == it.text }
                        needToUpdated ?: it
                    }
                )
            }

        }
    }
    @Composable
    fun first() {
        CivitaiImageViewModel.image?.let { civitaiImage ->
            AsyncImage(
                model = civitaiImage.url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        previewDialogState.openPreview(civitaiImage.url)
                    },
            )
        }

    }

    @Composable
    fun second() {
        CivitaiImageViewModel.image?.meta?.let { imageMeta ->
            prompt.takeIf { it.isNotEmpty() }?.let { promptList ->
                PromptDisplayView(
                    promptList = promptList,
                    canScroll = false,
                    title = stringResource(
                        id = R.string.prompts
                    ),
                    onlyShowTranslation = onlyDisplayTranslated,
                    toolbar = {
                        IconButton(onClick = {
                            onlyDisplayTranslated = !onlyDisplayTranslated
                        }) {
                            Icon(
                                ImageVector.vectorResource(id = R.drawable.ic_translate_mode),
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            isPromptTranslateDialogOpen = true
                        }) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_translate),
                                contentDescription = null
                            )
                        }
                    }
                ) {
                    promptActionState.onOpenActionBottomSheet(
                        prompt = it, target = "prompt"
                    )

                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            negativePrompt.takeIf { it.isNotEmpty() }?.let { promptList ->
                PromptDisplayView(
                    promptList = promptList,
                    canScroll = false,
                    title = stringResource(
                        id = R.string.param_negative_prompt
                    ),
                    onlyShowTranslation = onlyDisplayTranslated,
                    toolbar = {
                        IconButton(onClick = {
                            onlyDisplayTranslated = !onlyDisplayTranslated
                        }) {
                            Icon(
                                ImageVector.vectorResource(id = R.drawable.ic_translate_mode),
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            isNegativePromptTranslateDialogOpen = true
                        }) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_translate),
                                contentDescription = null
                            )
                        }
                    }
                ) {
                    promptActionState.onOpenActionBottomSheet(
                        prompt = it, target = "negativePrompt"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ListItem(headlineContent = {
                Text(stringResource(id = R.string.param_model))
            }, supportingContent = {
                Text(imageMeta.model ?: "Unknown")
            })
            imageMeta.size?.let {
                val parts = it.split("x")
                ListItem(headlineContent = {
                    Text(stringResource(id = R.string.param_width))
                }, supportingContent = {
                    Text(parts[0])
                })
                ListItem(headlineContent = {
                    Text(stringResource(id = R.string.param_height))
                }, supportingContent = {
                    Text(parts[1])
                })
            }
            imageMeta.sampler?.let {
                ListItem(headlineContent = {
                    Text(stringResource(id = R.string.param_sampler))
                }, supportingContent = {
                    Text(imageMeta.sampler)
                })
            }
            ListItem(headlineContent = {
                Text(stringResource(id = R.string.param_steps))
            }, supportingContent = {
                Text(imageMeta.steps.toString())
            })
            ListItem(headlineContent = {
                Text(stringResource(id = R.string.param_cfg_scale))
            }, supportingContent = {
                Text(imageMeta.cfgScale.toString())
            })

        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.civitai_image_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        if (useDevice == DeviceType.Phone) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                ) {
                                    first()
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                second()
                            }

                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                first()
                            }
                        }
                    }
                    if (useDevice != DeviceType.Phone) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            second()
                        }
                    }

                }
                DrawBar()
            }
        }

    }

}