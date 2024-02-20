package com.allentom.diffusion.ui.screens.style

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.IsWideWindow
import com.allentom.diffusion.composables.PromptContainer
import com.allentom.diffusion.composables.StyleEditDialog
import com.allentom.diffusion.store.prompt.PromptStyle
import com.allentom.diffusion.store.prompt.StyleStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StyleScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isAddStyleDialogShow by remember { mutableStateOf(false) }
    var isEditStyleDialogShow by remember { mutableStateOf(false) }
    var currentSelectedStyleId by remember { mutableStateOf<Long?>(null) }
    var selectedStyleId by remember { mutableStateOf<List<Long>>(emptyList()) }
    var isSelectMode by remember { mutableStateOf(false) }
    val currentSelectedStyle = StylesViewModel.styles.find { it.styleId == currentSelectedStyleId }
    var isDetailPanelShow by remember { mutableStateOf(false) }
    val isWideDisplay = IsWideWindow()
    fun refresh() {
        scope.launch(
            Dispatchers.IO
        ) {
            StylesViewModel.styles = StyleStore.getAllStyles(context)

        }
    }
    LaunchedEffect(Unit) {
        refresh()
    }
    if (isAddStyleDialogShow) {
        StyleEditDialog(
            stylePrompt = PromptStyle(
                name = "",
                prompts = listOf()
            ), onDismiss = {
                isAddStyleDialogShow = false
            }
        ) {
            scope.launch(Dispatchers.IO) {
                StyleStore.newStyle(context, name = it.name, prompts = it.prompts)
                scope.launch(
                    Dispatchers.Main
                ) {
                    Toast.makeText(context,
                        context.getString(R.string.style_added), Toast.LENGTH_SHORT).show()
                }
                refresh()
            }
            isAddStyleDialogShow = false
        }
    }
    if (isEditStyleDialogShow) {
        currentSelectedStyle?.let {
            StyleEditDialog(
                stylePrompt = it, onDismiss = {
                    isEditStyleDialogShow = false
                }
            ) {
                scope.launch(Dispatchers.IO) {
                    StyleStore.updateStyleById(
                        context = context,
                        styleId = it.styleId,
                        name = it.name,
                        prompts = it.prompts
                    )
                    scope.launch(
                        Dispatchers.Main
                    ) {
                        Toast.makeText(context,
                            context.getString(R.string.style_updated), Toast.LENGTH_SHORT).show()
                    }
                    refresh()
                }
                isEditStyleDialogShow = false
            }
        }
    }
    if (isDetailPanelShow) {
        currentSelectedStyle?.let {
            ModalBottomSheet(
                sheetState = SheetState(skipPartiallyExpanded = true),
                onDismissRequest = {
                    isDetailPanelShow = false
                }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    StyleDetailContent(
                        style = it,
                        onEdit = {
                            currentSelectedStyleId = it.styleId
                            isDetailPanelShow = false
                            isEditStyleDialogShow = true
                        }
                    )
                }

            }
        }
    }
    fun deleteSelectedStyles() {
        scope.launch(Dispatchers.IO) {
            selectedStyleId.forEach {
                StyleStore.deleteStyleById(context, it)
            }
            scope.launch(
                Dispatchers.Main
            ) {
                Toast.makeText(context,
                    context.getString(R.string.style_deleted), Toast.LENGTH_SHORT).show()
            }
            isSelectMode = false
            refresh()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectMode) {
                        Text(stringResource(R.string.selected_styles, selectedStyleId.size))
                    } else {
                        Text(stringResource(R.string.styles))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    if (isSelectMode) {
                        IconButton(onClick = {
                            deleteSelectedStyles()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(onClick = {
                            isSelectMode = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                        return@TopAppBar

                    }
                    IconButton(onClick = {
                        isAddStyleDialogShow = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            )
        },
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            LazyColumn {
                                items(StylesViewModel.styles.size) {
                                    val styleItem = StylesViewModel.styles[it]
                                    ListItem(
                                        colors = ListItemDefaults.colors(
                                            containerColor = let {
                                                if (isSelectMode) {
                                                    if (selectedStyleId.contains(styleItem.styleId)) {
                                                        return@let MaterialTheme.colorScheme.secondaryContainer
                                                    } else {
                                                        return@let MaterialTheme.colorScheme.surface
                                                    }
                                                }
                                                if (isWideDisplay && currentSelectedStyle == styleItem) {
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.surface
                                                }
                                            }
                                        ),
                                        headlineContent = {
                                            Text(text = styleItem.name)
                                        },
                                        modifier = Modifier.combinedClickable(
                                            onClick = {
                                                currentSelectedStyleId = styleItem.styleId
                                                if (isSelectMode) {
                                                    selectedStyleId =
                                                        if (selectedStyleId.contains(styleItem.styleId)) {
                                                            selectedStyleId.filter { id -> id != styleItem.styleId }
                                                        } else {
                                                            selectedStyleId + styleItem.styleId
                                                        }
                                                    if (selectedStyleId.isEmpty()) {
                                                        isSelectMode = false
                                                    }
                                                    return@combinedClickable
                                                }
                                                if (!isWideDisplay) {
                                                    isDetailPanelShow = true
                                                }
                                            },
                                            onLongClick = {
                                                isSelectMode = true
                                                selectedStyleId =
                                                    selectedStyleId + styleItem.styleId
                                            }
                                        )
                                    )
                                }
                            }
                        }
                        if (isWideDisplay) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp)
                            ) {
                                currentSelectedStyle?.let {
                                    StyleDetailContent(
                                        style = it,
                                        onEdit = {
                                            currentSelectedStyleId = it.styleId
                                            isEditStyleDialogShow = true
                                        },
                                        isSecondPanel = true
                                    )
                                }

                            }
                        }
                    }
                }
                DrawBar()
            }

        }
    }

}

@Composable
fun StyleDetailContent(
    style: PromptStyle,
    onEdit: (PromptStyle) -> Unit,
    isSecondPanel: Boolean = false
) {
    Column {
        if (!isSecondPanel) {
            Text(text = style.name, style = TextStyle(fontSize = 18.sp))
            Spacer(modifier = Modifier.padding(8.dp))
        }
        Box(
            modifier = Modifier.weight(1f)
        ) {
            PromptContainer(
                promptList = style.prompts,
                onClickPrompt = { },
            )
        }

        Spacer(modifier = Modifier.padding(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onEdit(style)
                }) {
                Text(text = stringResource(R.string.edit))
            }
        }
    }

}