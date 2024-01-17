package com.allentom.diffusion.ui.screens.civitai.images

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.composables.ActionItem
import com.allentom.diffusion.composables.BottomActionSheet
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.ImageUriPreviewDialogWrapper
import com.allentom.diffusion.composables.PromptAction
import com.allentom.diffusion.composables.PromptActionState
import com.allentom.diffusion.composables.PromptDisplayView
import com.allentom.diffusion.composables.rememberImagePreviewDialogState
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable()
fun CivitaiImageDetailScreen(id: Long) {
    var imageItem = CivitaiImageListViewModel.imageList.find { it.id == id }
    var isPromptActionVisible by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    var promptTarget by remember {
        mutableStateOf("prompt")
    }
    var selectedPromptList by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    val promptList = imageItem?.meta?.prompt?.split(",")?.filter { it.isNotEmpty() }?.map {
        Util.parsePrompt(it.trim())
    }?.toList() ?: emptyList()

    val negativePromptList =
        imageItem?.meta?.negativePrompt?.split(",")?.filter { it.isNotEmpty() }?.map {
            Util.parsePrompt(it.trim())
        }?.toList() ?: emptyList()
    var selectedNegativePromptList by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    val promptActionState = PromptActionState()
    PromptAction(actionState = promptActionState)
    val previewDialogState = rememberImagePreviewDialogState()
    ImageUriPreviewDialogWrapper(state = previewDialogState)
    if (isPromptActionVisible) {
        BottomActionSheet(items = listOf(
            ActionItem(
                text = stringResource(R.string.append_to_with_target, promptTarget),
                onAction = {
                    if (promptTarget == "prompt") {
                        val selectedPrompt = selectedPromptList.map { selectedText ->
                            promptList.find {
                                it.text == selectedText
                            }
                        }.filterNotNull()
                        DrawViewModel.inputPromptText =
                            DrawViewModel.inputPromptText.filter { usedPrompt ->
                                selectedPrompt.find { it.text == usedPrompt.text } == null
                            } + selectedPrompt
                        Toast.makeText(
                            context,
                            context.getString(R.string.append_to_prompt), Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (promptTarget == "Negative Prompt") {
                        val selectedPrompt = selectedNegativePromptList.map { selectedText ->
                            negativePromptList.find {
                                it.text == selectedText
                            }
                        }.filterNotNull()
                        DrawViewModel.inputNegativePromptText =
                            DrawViewModel.inputNegativePromptText.filter { usedPrompt ->
                                selectedPrompt.find { it.text == usedPrompt.text } == null
                            } + selectedPrompt
                        Toast.makeText(
                            context,
                            context.getString(R.string.append_to_negative_prompt_success),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            ),
            ActionItem(
                text = stringResource(R.string.assign_to_with_target, promptTarget),
                onAction = {
                    if (promptTarget == "prompt") {
                        val selectedPrompt = selectedPromptList.map { selectedText ->
                            promptList.find {
                                it.text == selectedText
                            }
                        }.filterNotNull()
                        DrawViewModel.inputPromptText = selectedPrompt
                        Toast.makeText(
                            context,
                            context.getString(R.string.assign_to_prompt_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (promptTarget == "Negative Prompt") {
                        val selectedPrompt = selectedNegativePromptList.map { selectedText ->
                            negativePromptList.find {
                                it.text == selectedText
                            }
                        }.filterNotNull()
                        DrawViewModel.inputNegativePromptText = selectedPrompt
                        Toast.makeText(
                            context,
                            context.getString(R.string.assign_to_negative_prompt_success),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            ),
        )) {
            isPromptActionVisible = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.civitai_image_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
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
                    imageItem?.let {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(16.dp)
                            ) {
                                AsyncImage(
                                    model = imageItem.url,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        previewDialogState.openPreview(imageItem.url)
                                    },
                                    contentDescription = "image"
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Column {
                                promptList.takeIf { it.isNotEmpty() }?.let {
                                    PromptDisplayView(promptList = it, canScroll = false) {
                                        promptActionState.onOpenActionBottomSheet(
                                            prompt = it, target = "prompt"
                                        )

                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            negativePromptList.takeIf { it.isNotEmpty() }?.let {
                                PromptDisplayView(promptList = it, canScroll = false) {
                                    promptActionState.onOpenActionBottomSheet(
                                        prompt = it, target = "negativePrompt"
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            imageItem.meta?.sampler?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                ListItem(headlineContent = {
                                    Text(text = stringResource(id = R.string.param_sampler))
                                }, supportingContent = {
                                    Text(text = it)
                                })
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            imageItem.meta?.cfgScale?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                ListItem(headlineContent = {
                                    Text(text = stringResource(id = R.string.param_cfg_scale))
                                }, supportingContent = {
                                    Text(text = it.toString())
                                })
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            imageItem.meta?.steps?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                ListItem(headlineContent = {
                                    Text(text = stringResource(id = R.string.param_steps))
                                }, supportingContent = {
                                    Text(text = it.toString())
                                })
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            imageItem.meta?.seed?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                ListItem(headlineContent = {
                                    Text(text = stringResource(id = R.string.param_seed))
                                }, supportingContent = {
                                    Text(text = it.toString())
                                })
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                DrawBar()
            }

        }
    }
}
