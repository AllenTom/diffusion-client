package com.allentom.diffusion.ui.screens.civitai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.ImageUriPreviewDialogWrapper
import com.allentom.diffusion.composables.PromptAction
import com.allentom.diffusion.composables.PromptActionState
import com.allentom.diffusion.composables.PromptDisplayView
import com.allentom.diffusion.composables.rememberImagePreviewDialogState

@OptIn(ExperimentalMaterial3Api::class)
@Composable()
fun CivitaiModelImageScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var prompt =
        CivitaiImageViewModel.image?.meta?.prompt?.split(",")?.filter { it.isNotEmpty() }?.map {
            Util.parsePrompt(it)
        } ?: emptyList()
    var negativePrompt =
        CivitaiImageViewModel.image?.meta?.negativePrompt?.split(",")?.filter { it.isNotEmpty() }
            ?.map {
                Util.parsePrompt(it)
            } ?: emptyList()
    val promptActionState = PromptActionState()
    PromptAction(actionState = promptActionState)

    val previewDialogState = rememberImagePreviewDialogState()
    ImageUriPreviewDialogWrapper(state = previewDialogState)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CivitaiImageViewModel.image?.let { civitaiImage ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            ) {
                                AsyncImage(
                                    model = civitaiImage.url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clickable {
                                        previewDialogState.openPreview(civitaiImage.url)
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            civitaiImage.meta?.let { imageMeta ->
                                prompt.takeIf { it.isNotEmpty() }?.let { promptList ->
                                    PromptDisplayView(promptList = promptList, canScroll = false) {
                                        promptActionState.onOpenActionBottomSheet(
                                            prompt = it, target = "prompt"
                                        )

                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                negativePrompt.takeIf { it.isNotEmpty() }?.let { promptList ->
                                    PromptDisplayView(promptList = promptList, canScroll = false) {
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

                    }
                }
                DrawBar()
            }


        }

    }

}