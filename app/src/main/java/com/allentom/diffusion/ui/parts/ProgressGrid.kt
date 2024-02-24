package com.allentom.diffusion.ui.parts

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R
import com.allentom.diffusion.composables.ImageBase64PreviewDialog
import com.allentom.diffusion.extension.thenIf
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.GenImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max


@Composable
fun GenProgressGrid(
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    var isImagePreviewerOpen by remember { mutableStateOf(false) }
    var isActionMenuShow by remember { mutableStateOf(false) }
    val context = LocalContext.current
    if (isImagePreviewerOpen && DrawViewModel.displayResultIndex < DrawViewModel.genItemList.size) {
        val displayItem = DrawViewModel.genItemList[DrawViewModel.displayResultIndex]
        displayItem.getDisplayImageBase64()?.let {
            ImageBase64PreviewDialog(
                imageBase64 = it,
                isOpen = isImagePreviewerOpen,
                onDismissRequest = {
                    isImagePreviewerOpen = false
                })
        }
    }
    fun saveImageToDeviceGallery(imageItem: GenImageItem) {
        scope.launch {
            withContext(Dispatchers.IO) {
                DrawViewModel.favouriteImage(context, imageItem)
            }
        }
        Toast.makeText(
            context,
            context.getString(R.string.saved_to_device_gallery), Toast.LENGTH_SHORT
        ).show()
    }
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)

        ) {
            if (DrawViewModel.genItemList.isNotEmpty()) {
                val imgItem =
                    DrawViewModel.genItemList[DrawViewModel.displayResultIndex]
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (imgItem.error != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(
                                        Alignment.Center
                                    )
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Error",
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(text = imgItem.error.error)
                                    Text(text = imgItem.error.errors)
                                }
                            }
                        } else {
                            val imageBase64 = imgItem.getDisplayImageBase64()
                            if (imageBase64 != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .clickable {
                                                scope.launch {
                                                    isImagePreviewerOpen = true
                                                }
                                            }
                                    ) {
                                        DisplayBase64Image(imageBase64)
                                    }
                                }

                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(
                                            Alignment.Center
                                        )
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.align(Alignment.Center)
                                    ) {
                                        if (imgItem.isInterrupted) {
                                            Text(text = stringResource(R.string.interrupted))
                                        } else {
                                            CircularProgressIndicator(
                                                modifier = Modifier.width(32.dp),
                                                color = MaterialTheme.colorScheme.secondary,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                progress = imgItem.progress?.progress
                                                    ?: 0f
                                            )
                                            Text(text = stringResource(id = R.string.generating))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (DrawViewModel.genItemList.isNotEmpty()) {
            if (!DrawViewModel.isGenerating) {
                val imgItem =
                    DrawViewModel.genItemList[DrawViewModel.displayResultIndex]
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = imgItem.seed.toString(),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Box() {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable {
                                    isActionMenuShow = true
                                },
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        DropdownMenu(
                            expanded = isActionMenuShow,
                            onDismissRequest = { isActionMenuShow = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_photo_album),
                                        contentDescription = null
                                    )
                                },
                                text = { Text(stringResource(R.string.add_to_gallery)) },
                                onClick = {
                                    isActionMenuShow = false
                                    saveImageToDeviceGallery(imgItem)
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null
                                    )
                                },
                                text = { Text(stringResource(R.string.regenerate)) },
                                onClick = {
                                    isActionMenuShow = false
                                    scope.launch {
                                        DrawViewModel.startGenerate(
                                            context,
                                            refreshIndex = DrawViewModel.displayResultIndex
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null
                                    )

                                },
                                text = { Text(stringResource(R.string.send_to_image_to_image)) },
                                onClick = {
                                    isActionMenuShow = false
                                    scope.launch {
                                        DrawViewModel.img2ImgParam = DrawViewModel.img2ImgParam.copy(
                                            imgBase64 = imgItem.getDisplayImageBase64(),
                                            width = DrawViewModel.baseParam.width,
                                            height = DrawViewModel.baseParam.height
                                        )
                                        imgItem.seed.let {
                                            DrawViewModel.baseParam = DrawViewModel.baseParam.copy(
                                                seed = it
                                            )
                                        }
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.image_sent_to_image_to_image),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (DrawViewModel.genItemList.isNotEmpty()) {
                val displayXYZ = DrawViewModel.genXYZ
                if (displayXYZ == null) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(DrawViewModel.genItemList.size) {
                            GenItem(index = it)
                        }
                    }
                } else {
                    val yAxisCount = max(displayXYZ.yAxisValues.size, 1)
                    LazyColumn {
                        item {
                            for (i in 0 until yAxisCount) {
                                val yAxisValue = displayXYZ.yAxisValues.getOrNull(i)
                                yAxisValue?.let {
                                    Text(
                                        text = "${displayXYZ.yAxisName} = $yAxisValue"
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                ) {
                                    val xAxisCount = max(displayXYZ.xAxisValues.size, 1)
                                    for (j in 0 until (xAxisCount)) {
                                        val currentIndex = i * (xAxisCount) + j
                                        Box(
                                            modifier = Modifier.width(120.dp)
                                        ) {
                                            GenItem(
                                                currentIndex
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                }
//                                LazyVerticalGrid(
//                                    columns = GridCells.Fixed(3),
//                                    modifier = Modifier
//                                        .fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
//                                    verticalArrangement = Arrangement.spacedBy(4.dp),
//                                ) {
//                                    items(xyzParam.xAxis!!.getGenCount()) {
//                                        GenItem(index = it * i)
//                                    }
//                                }
                            }
                        }

                    }
                }

            }
        }
    }

}

@Composable
fun GenItem(index: Int) {
    Box(
        modifier = Modifier
            .aspectRatio(DrawViewModel.baseParam.width.toFloat() / DrawViewModel.baseParam.height.toFloat())
            .sizeIn(maxHeight = 90.dp, maxWidth = 90.dp)
            .border(
                DrawViewModel.displayResultIndex.let { currentIndex ->
                    if (currentIndex == index) 4.dp else 0.dp
                },
                DrawViewModel.displayResultIndex.let { currentIndex ->
                    if (currentIndex == index) MaterialTheme.colorScheme.secondary
                    else Color.Transparent
                },
                RoundedCornerShape(8.dp)
            )
            .clickable {

                DrawViewModel.displayResultIndex = index

            }
            .clip(RoundedCornerShape(8.dp))
    ) {
        val imageItem = DrawViewModel.genItemList[index]
        if (imageItem.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(8.dp)
                    .align(
                        Alignment.Center
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Error"
                    )
                    Text(text = stringResource(id = R.string.error))
                }
            }
        } else {
            val imageBase64 = imageItem.getDisplayImageBase64()
            if (imageBase64 != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()

                ) {
                    DisplayBase64Image(imageBase64)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(8.dp)
                        .align(
                            Alignment.Center
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        if (imageItem.isInterrupted) {
                            Text(text = stringResource(R.string.interrupted))
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.width(32.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                progress = imageItem.progress?.progress
                                    ?: 0f
                            )
                            Text(text = stringResource(id = R.string.generating))
                        }
                    }
                }
            }
        }
    }
}