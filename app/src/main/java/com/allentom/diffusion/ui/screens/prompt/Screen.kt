package com.allentom.diffusion.ui.screens.prompt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.allentom.diffusion.Screens
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.IsWideWindow
import com.allentom.diffusion.store.PromptStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PromptScreen(navController: NavController) {
    var categoryList by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var currentSelectCategoryName by remember {
        mutableStateOf<String?>(null)
    }


    fun refreshSearch() {
        coroutineScope.launch(Dispatchers.IO) {
            categoryList = PromptStore.getAllCategory(context)
            if (currentSelectCategoryName == null) {
                currentSelectCategoryName = categoryList.firstOrNull()
            }
        }
    }
    LaunchedEffect(Unit) {
        refreshSearch()
    }
    val isWideDisplay = IsWideWindow()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Prompts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable {
                                navController.navigate(Screens.PromptSearch.route)
                            }
                    )
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        LazyColumn {
                            items(categoryList.size, key = {
                                categoryList[it]
                            }) { idx ->
                                val category = categoryList[idx]
                                ListItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isWideDisplay) {
                                                currentSelectCategoryName = category
                                                return@clickable
                                            }
                                            navController.navigate(
                                                Screens.PromptCategory.route.replace(
                                                    "{promptName}",
                                                    category
                                                )
                                            )
                                        },
                                    headlineContent = {
                                        Text(category)
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = if (currentSelectCategoryName == category && isWideDisplay) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                )
                            }
                        }
                    }
                    isWideDisplay.takeIf { it }?.let {
                        currentSelectCategoryName?.let { currentSelectCategoryName ->
                            Box(
                                modifier = Modifier
                                    .weight(2f)
                            ) {
                                PromptCategoryScreen(
                                    navController = navController,
                                    promptName = currentSelectCategoryName,
                                    isSecondDisplay = isWideDisplay
                                )
                            }
                        }
                    }

                }
                DrawBar()
            }

        }
    }

}