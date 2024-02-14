package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RegionDisplayView(
    regionTree: Region,
    modifier: Modifier,
    selectedRegionId: String? = null,
    selectMode: String? = null,
    onSelectChange: ((Region) -> Unit)? = null,
) {
    Column(
        modifier = modifier
    ) {
        regionTree.subRegions.forEachIndexed { idx, region ->
            Box(
                modifier = Modifier
                    .weight(region.ratio.toFloat())
                    .fillMaxWidth()
                    .background(Color(region.color))
                    .border(
                        width = 2.dp,
                        color = if (selectedRegionId == region.id) Color.Red else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (region.subRegions.isNotEmpty()) {
                    Row {
                        region.subRegions.forEach { subRegion ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(subRegion.ratio.toFloat())
                                    .background(Color(subRegion.color))
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedRegionId == subRegion.id) Color.Red else Color.Transparent
                                    )
                                    .clickable {
                                        if (selectMode == null) {
                                            return@clickable
                                        }
                                        if (selectMode == "column" && subRegion.parent != null) {
                                            subRegion.parent?.let {
                                                onSelectChange?.invoke(it)
                                            }
                                        } else {
                                            onSelectChange?.invoke(subRegion)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier.background(Color(0x80000000))
                                ) {
                                    Text(subRegion.index.toString(), color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                Color.Black
                            )
                            .padding(4.dp)
                    ) {
                        Text("$idx", color = Color.White)
                    }
                }
            }
        }
    }
}