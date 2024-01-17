package com.allentom.diffusion.composables

import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.store.ControlNetMeta
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

data class ControlNetMetaItem(
    val sourceUri: Uri,
    val previewUri: Uri? = null,
)

@Composable
fun ControlNetImportDialog(
    onDismissRequest: () -> Unit,
    onImport: (list: List<ControlNetMetaItem>) -> Unit
) {
    val context = LocalContext.current
    val gson = Gson()
    val scope = rememberCoroutineScope()
    var importItems by remember {
        mutableStateOf<List<ControlNetMetaItem>>(listOf())
    }
    val folderPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }
            Toast.makeText(context, uri.toString(), Toast.LENGTH_SHORT).show()
            // meta.json
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri) + "/meta.json"
            )
            scope.launch(Dispatchers.IO) {
                val resultList = mutableListOf<ControlNetMetaItem>()

                context.contentResolver.openInputStream(documentUri)?.use { inputStream ->
                    val metaJson = InputStreamReader(inputStream).readText()
                    val data = gson.fromJson(metaJson, ControlNetMeta::class.java)
                    data.list.forEach {
                        var useFileUrl: Uri? = null
                        val fileUri = DocumentsContract.buildDocumentUriUsingTree(
                            uri,
                            DocumentsContract.getTreeDocumentId(uri) + "/" + it.filename
                        )
                        useFileUrl = fileUri

                        var usePreviewUrl: Uri? = null
                        it.preview?.let { previewFilename ->
                            val previewUri = DocumentsContract.buildDocumentUriUsingTree(
                                uri,
                                DocumentsContract.getTreeDocumentId(uri) + "/" + previewFilename
                            )
                            usePreviewUrl = previewUri
                        }

                        useFileUrl?.let { fileUri ->
                            Log.d("ControlNetImportDialog", fileUri.toString())
//                            ControlNetStore.addControlNet(context, fileUri, usePreviewUrl)
                            resultList.add(ControlNetMetaItem(fileUri, usePreviewUrl))
                        }

                    }
                    importItems = resultList

                    // Do something with the data
                }
            }
            // Open an InputStream from the URI and read the data


        }
    AlertDialog(onDismissRequest = {
        onDismissRequest()
    },
        title = {
            Text(text = stringResource(R.string.import_control_net_dialog_title))
        },
        text = {
            Column {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                    folderPickerLauncher.launch(null)
                }) {
                    Text(stringResource(R.string.select_folder))
                }
                if (importItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(importItems.size) { index ->
                            val item = importItems[index]
                            Row {
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(120.dp)
                                ) {
                                    AsyncImage(
                                        model = item.sourceUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                item.previewUri?.let {
                                    Box(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(120.dp)
                                    ) {
                                        AsyncImage(
                                            model = it,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }

        },
        confirmButton = {
            Button(onClick = { onImport(importItems) }) {
                Text(stringResource(R.string.import_btn))
            }
        },
        dismissButton = {
            Button(onClick = { onDismissRequest() }) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )

}