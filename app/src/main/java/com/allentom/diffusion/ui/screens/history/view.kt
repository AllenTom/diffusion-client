package com.allentom.diffusion.ui.screens.history

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.Util
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.store.HistoryStore
import com.allentom.diffusion.store.SaveHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListView(navController: NavController) {
    val context = LocalContext.current
    var historyList by remember {
        mutableStateOf(listOf<SaveHistory>())
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        historyList.takeIf { it.isEmpty() }?.let {
            scope.launch(Dispatchers.IO) {
                historyList = HistoryStore.getAllHistory(context)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.screen_history_list_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues = paddingValues)

        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyList.size, key = {
                        historyList[it].id
                    }) { historyItem ->
                        val historyItem = historyList[historyItem]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        Screens.HistoryDetail.route.replace(
                                            "{id}",
                                            historyItem.id.toString()
                                        )
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Column {
                                        Text(text = Util.formatUnixTime(historyItem.time))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Row {
                                            for (img in historyItem.imagePaths) {
                                                val imgRatio =
                                                    historyItem.width.toFloat() / historyItem.height.toFloat()
                                                AsyncImage(
                                                    model = img.path, contentDescription = null,
                                                    modifier = Modifier
                                                        .width((120 * imgRatio).dp)
                                                        .height(120.dp),
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            DrawBar()
        }

    }

}

