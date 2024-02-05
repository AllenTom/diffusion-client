package com.allentom.diffusion.ui.screens.prompt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.allentom.diffusion.Screens
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.PromptCart
import com.allentom.diffusion.store.prompt.PromptStore
import com.allentom.diffusion.store.prompt.SavePrompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PromptSearchScreen(navController: NavController) {
    var promptList by rememberSaveable {
        mutableStateOf<List<SavePrompt>>(emptyList())
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var inputSearchText by rememberSaveable {
        mutableStateOf("")
    }
    var searchJob: Job? by remember { mutableStateOf(null) }
    var contextPrompt by remember {
        mutableStateOf<SavePrompt?>(null)
    }

    fun refreshSearch() {
        coroutineScope.launch(Dispatchers.IO) {
            promptList = PromptStore.searchPrompt(context, inputSearchText)
        }
    }

    var isPromptCartOpen by remember {
        mutableStateOf(false)
    }
    if (isPromptCartOpen) {
        ModalBottomSheet(onDismissRequest = {
            isPromptCartOpen = false
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                PromptCart()
            }

        }
    }
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Prompts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(corner = CornerSize(16.dp))
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                                BasicTextField(
                                    value = inputSearchText,
                                    onValueChange = {
                                        inputSearchText = it
                                        searchJob?.cancel()
                                        searchJob = coroutineScope.launch {
                                            delay(300L)  // delay for 300ms
                                            refreshSearch()
                                        }
                                    },

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 18.sp
                                    ),
                                )
                            }

                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            promptList.forEach { prompt ->
                                ListItem(
                                    modifier = Modifier
                                        .combinedClickable(
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
                                    headlineContent = {
                                        Text(prompt.text)
                                    },
                                    supportingContent = {
                                        if (prompt.text != prompt.nameCn) {
                                            Text(prompt.nameCn)
                                        }
                                    },
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
                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                }
                DrawBar()
            }

        }
    }
}