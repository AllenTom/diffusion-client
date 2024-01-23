package com.allentom.diffusion.ui.screens.prompt

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.allentom.diffusion.Screens
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.PromptLibraryImportDialog
import com.allentom.diffusion.store.PromptStore
import com.allentom.diffusion.store.SavePrompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PromptCategoryScreen(
    navController: NavController,
    promptName: String,
    isSecondDisplay: Boolean? = false
) {
    var promptList by remember {
        mutableStateOf<List<SavePrompt>>(emptyList())
    }
    val coroutineScope = rememberCoroutineScope()
    val context: Context = LocalContext.current
    var isPromptCartOpen by remember {
        mutableStateOf(false)
    }
    var contextPrompt by remember {
        mutableStateOf<SavePrompt?>(null)
    }

    fun refresh() {
        coroutineScope.launch(Dispatchers.IO) {
            promptList =
                PromptStore.getPromptByCategory(context, CategoryScreenViewModel.categoryName)
        }
    }
    LaunchedEffect(promptName) {
        if (promptName != "") {
            CategoryScreenViewModel.categoryName = promptName
        }
        refresh()
    }
    PromptCartModal(isPromptCartOpen = isPromptCartOpen, onDismissRequest = {
        isPromptCartOpen = false
    })
    contextPrompt?.let {
        PromptActionBottomSheet(
            contextPrompt = contextPrompt,
            onDismissRequest = {
                contextPrompt = null
            },
            onAddToPrompt = {
                DrawViewModel.addInputPrompt(it.text)
            },
            onAddToNegativePrompt = {
                DrawViewModel.addInputNegativePrompt(it.text)
            }
        )
    }
    @Composable
    fun Content() {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(promptList, key = {
                it.promptId
            }) { prompt ->
                ListItem(
                    headlineContent = {
                        Text(text = prompt.text)
                    },
                    supportingContent = {
                        prompt.nameCn.takeIf { it != prompt.text }?.let {
                            Text(text = it)
                        }
                    },
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            navController.navigate(
                                Screens.PromptDetail.route.replace(
                                    "{promptId}",
                                    prompt.promptId.toString()
                                )
                            )
                        },
                        onLongClick = {
                            contextPrompt = prompt
                        }
                    ),
                    trailingContent = {
                        IconButton(onClick = {
                            DrawViewModel.addInputPrompt(prompt.text)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

        }
    }
    if (isSecondDisplay == true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Content()

                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            isSecondDisplay.takeIf { it == false }?.let {
                TopAppBar(
                    title = { Text(text = promptName) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Content()

                }
                if (isSecondDisplay != true) {
                    DrawBar()
                }

            }
        }
    }

}