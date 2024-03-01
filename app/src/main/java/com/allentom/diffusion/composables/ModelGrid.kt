package com.allentom.diffusion.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.api.entity.Model
import com.allentom.diffusion.extension.thenIf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModelGrid(
    modifier: Modifier = Modifier,
    columnCount: Int,
    modelList: List<Model>,
    itemImageFit: String = "Fit",
    onCLick: (model: Model) -> Unit = {},
) {
    val modelIcon = ImageVector.vectorResource(id = R.drawable.ic_model)
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(columnCount),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(modelList.size) { idx ->
            val model = modelList[idx]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            onCLick(model)
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .height(220.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .thenIf(itemImageFit == "Fit", Modifier.blur(16.dp))
                    ) {
                        if (model.entity.coverPath != null) {
                            AsyncImage(
                                model = model.entity.coverPath,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .height(150.dp)
                                .padding(8.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {

                            if (model.entity.coverPath != null) {
                                if (itemImageFit == "Fit") {
                                    AsyncImage(
                                        model = model.entity.coverPath,
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            } else {
                                Icon(
                                    modelIcon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(48.dp)
                                        .height(48.dp),

                                    )

                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.7f
                                    )
                                )
                                .padding(8.dp),
                        ) {
                            Column {
                                Text(
                                    text = model.entity.title ?: model.title,
                                    fontSize = 16.sp,
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }

                    }

                }

            }
        }

    }
}