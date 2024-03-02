package com.allentom.diffusion.ui.screens.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.composables.CivitaiImageImport
import com.allentom.diffusion.composables.HistoryImportDialog
import com.allentom.diffusion.composables.IsWideWindow
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawScreen
import com.allentom.diffusion.ui.screens.home.tabs.gallery.GalleryView
import com.allentom.diffusion.ui.screens.home.tabs.tools.TabTools

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavController) {
    val state = HomeViewModel
    val historyIcon = ImageVector.vectorResource(id = R.drawable.ic_history)
    val galleryIcon = ImageVector.vectorResource(id = R.drawable.ic_photo_album)
    val toolsIcon = ImageVector.vectorResource(id = R.drawable.ic_tools)
    val imageFitIcon = ImageVector.vectorResource(id = R.drawable.ic_image_fit)
    val imageCropIcon = ImageVector.vectorResource(id = R.drawable.ic_image_crop)
    val downloadFromCivitaiImage = ImageVector.vectorResource(id = R.drawable.ic_download_web)
    var isImportHistoryDialogOpen by remember { mutableStateOf(false) }
    var isImportCivitaiImageDialogOpen by remember { mutableStateOf(false) }
    val iconsMapping = listOf(
        Icons.Filled.Create,
        galleryIcon,
        toolsIcon
    )
    val isWideDisplay = IsWideWindow()
    if (isImportHistoryDialogOpen) {
        HistoryImportDialog(onDismiss = {
            isImportHistoryDialogOpen = false
        })
    }
    if (isImportCivitaiImageDialogOpen) {
        CivitaiImageImport {
            isImportCivitaiImageDialogOpen = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Diffusion")
                },
                actions = {
                    if (state.selectedIndex == 0) {
                        IconButton(
                            onClick = {
                                isImportCivitaiImageDialogOpen = true
                            }
                        ) {
                            Icon(
                                downloadFromCivitaiImage,
                                contentDescription = "Menu",
                            )
                        }
                        IconButton(
                            onClick = {
                                isImportHistoryDialogOpen = true
                            }
                        ) {
                            Icon(
                                ImageVector.vectorResource(R.drawable.ic_import_history),
                                contentDescription = "Menu",
                            )
                        }
                        IconButton(
                            onClick = {
                                navController.navigate(Screens.History.route)
                            }
                        ) {
                            Icon(
                                historyIcon,
                                contentDescription = "Menu",
                            )
                        }
                    }
                    if (state.selectedIndex == 1) {
                        IconButton(
                            onClick = {
                                if (HomeViewModel.galleryItemImageFit == 0) {
                                    HomeViewModel.galleryItemImageFit = 1
                                } else {
                                    HomeViewModel.galleryItemImageFit = 0
                                }
                            }
                        ) {
                            Icon(
                                when (HomeViewModel.galleryItemImageFit) {
                                    1 -> imageCropIcon
                                    0 -> imageFitIcon
                                    else -> imageFitIcon
                                },
                                contentDescription = "Menu",
                            )
                        }
                        if (!HomeViewModel.gallerySelectMode) {
                            IconButton(
                                onClick = {
                                    HomeViewModel.gallerySelectMode =
                                        !HomeViewModel.gallerySelectMode
                                }
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Select",
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            isWideDisplay.takeIf { !it }?.let {
                NavigationBar {
                    BottomNavigationItem()
                        .bottomNavigationItems().forEachIndexed { index, navigationItem ->
                            NavigationBarItem(
                                selected = state.selectedIndex == index,
                                label = {
                                    Text(navigationItem.label)
                                },
                                icon = {
                                    Icon(
                                        iconsMapping[index],
                                        contentDescription = navigationItem.label
                                    )
                                },
                                onClick = {
                                    state.selectedIndex = index
                                }
                            )
                        }
                }
            }

        },
    ) { paddingValues ->
        Row(
            modifier = Modifier.padding(paddingValues)
        ) {
            isWideDisplay.takeIf { it }?.let {
                Row {
                    NavigationRail {
                        NavigationRailItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "") },
                            selected = state.selectedIndex == 0,
                            onClick = { state.selectedIndex = 0 }
                        )
                        NavigationRailItem(
                            icon = { Icon(galleryIcon, contentDescription = "") },
                            selected = state.selectedIndex == 1,
                            onClick = { state.selectedIndex = 1 }
                        )
                        NavigationRailItem(
                            icon = { Icon(toolsIcon, contentDescription = "") },
                            selected = state.selectedIndex == 2,
                            onClick = { state.selectedIndex = 2 }
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                when (state.selectedIndex) {
                    0 -> DrawScreen()
                    1 -> GalleryView(navController)
                    2 -> TabTools(navController = navController)
                }
            }
        }

    }

}
