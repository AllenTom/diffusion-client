package com.allentom.diffusion.composables

import android.widget.Toast
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.api.civitai.entities.CivitaiModelVersion
import com.allentom.diffusion.api.civitai.getCivitaiApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivitaiModelSelectDialog(
    onDismiss: () -> Unit,
    onApply: (CivitaiModelVersion) -> Unit
) {
    var result by remember {
        mutableStateOf<CivitaiModelVersion?>(null)
    }
    var fetchId by remember {
        mutableStateOf("")
    }
    var currentPreviewIndex by remember {
        mutableStateOf(0)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun fetchModel() {
        scope.launch(Dispatchers.IO) {
            try {
                val response = getCivitaiApiClient().getModelVersionById(fetchId)
                response.body()?.let {
                    currentPreviewIndex = 0
                    result = it
                }
            } catch (e: Exception) {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = stringResource(R.string.fetch_civitai_model_dialog_title))
        },
        confirmButton = {
            Button(onClick = {
                result?.let {
                    onApply(it)
                }
                onDismiss()
            }) {
                Text(text = stringResource(R.string.apply))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TextField(value = fetchId, onValueChange = {
                        fetchId = it
                    }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = {
                        fetchModel()
                    }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
                result?.let { model ->
                    Spacer(modifier = Modifier.width(16.dp))
                    model.images.takeIf { it.isNotEmpty() }?.let {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .height(200.dp)
                                .fillMaxWidth()
                        ) {
                            val currentPreviewImage = it[currentPreviewIndex]
                            AsyncImage(
                                model = currentPreviewImage.url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LazyRow {
                            items(it.size) { index ->
                                val image = it[index]
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .border(
                                            width = 2.dp,
                                            color = if (index == currentPreviewIndex) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            currentPreviewIndex = index
                                        }
                                ) {
                                    AsyncImage(
                                        model = image.url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ListItem(headlineContent = {
                        Text(text = stringResource(R.string.name))
                    }, supportingContent = {
                        Text(model.model.name)
                    })
                    ListItem(headlineContent = {
                        Text(text = stringResource(R.string.model_name))
                    }, supportingContent = {
                        Text(model.name)
                    })
                }
            }
        }
    )

}