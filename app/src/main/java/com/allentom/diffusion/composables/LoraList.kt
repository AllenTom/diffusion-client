package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.api.entity.Lora

@Composable
fun LoraList(
    modifier: Modifier = Modifier,
    loraList: List<Lora>,
    onAddLora: (Lora) -> Unit = {},
){
    LazyColumn(
        modifier = modifier
    ) {
        items(loraList.size) {
            val lora = loraList[it]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(8.dp)
                    .clickable {
                        onAddLora(lora)
                    }
            ) {
                if (lora.entity?.title != null && lora.entity.title.isNotEmpty()) {
                    Text(lora.entity.title, modifier = Modifier.weight(1f))
                } else {
                    Text(lora.name, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                val previewPath = lora.entity?.previewPath
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .width(120.dp)
                ) {
                    if (previewPath != null && previewPath.isNotEmpty()) {
                        AsyncImage(
                            model = previewPath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.no_preview))
                        }
                    }
                }

            }
        }
    }
}