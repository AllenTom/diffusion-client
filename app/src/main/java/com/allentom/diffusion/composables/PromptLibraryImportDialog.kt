package com.allentom.diffusion.composables

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
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
import com.allentom.diffusion.R
import com.allentom.diffusion.store.prompt.PromptStore
import com.allentom.diffusion.store.prompt.SavePrompt
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlMap
import com.charleskorn.kaml.yamlScalar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ScanResult(
    val category: String,
    val name: String,
    val translation: String
)

data class ImportProgress(
    val total: Int,
    val current: Int,
    val currentScanResult: ScanResult,
)

@Composable
fun PromptLibraryImportDialog(
    onDismiss: () -> Unit,
    onCompleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scanResultList by remember {
        mutableStateOf(null as List<ScanResult>?)
    }
    var isReading by remember {
        mutableStateOf(false)
    }
    var importProgress by remember {
        mutableStateOf(null as ImportProgress?)
    }

    fun importResult() {
        scanResultList?.let { resultList ->
            scope.launch(Dispatchers.IO) {
                importProgress = ImportProgress(
                    resultList.size,
                    0,
                    if (resultList.isNotEmpty()) resultList[0] else ScanResult("", "", "")
                )
                resultList.forEachIndexed { index, scanResult: ScanResult ->
                    importProgress = ImportProgress(
                        resultList.size,
                        index,
                        scanResult
                    )
                    PromptStore.newPromptByName(
                        context,
                        SavePrompt(
                            promptId = 0,
                            text = scanResult.name,
                            nameCn = scanResult.translation,
                            category = scanResult.category,
                            count = 0,
                            time = System.currentTimeMillis()
                        )
                    )
                }
                importProgress = null
                onDismiss()
                onCompleted()
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    isReading = true
                    scanResultList = null
                    var resultList = mutableListOf<ScanResult>()
                    val inputStream = context.contentResolver.openInputStream(it)
                    inputStream?.let {
                        val raw = Yaml.default.parseToYamlNode(inputStream)
                        val packageContent = raw.yamlMap.get<YamlList>("content") ?: return@let
                        packageContent.items.forEach { catContent ->
                            val catName = catContent.yamlMap.get<YamlScalar>("name")?.content
                                ?: return@forEach
                            val itemContent =
                                catContent.yamlMap.get<YamlMap>("content") ?: return@forEach
                            itemContent.entries.forEach { ent ->
                                val promptName = ent.key.yamlScalar.content
                                val nameCn = ent.value.yamlMap.get<YamlScalar>("name")?.content
                                if (nameCn != null) {
                                    resultList.add(ScanResult(catName, promptName, nameCn))
                                }
                            }
                        }
                        scanResultList = resultList
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle exception
                } finally {
                    isReading = false
                }
            }


        }
    }
    AlertDialog(
        onDismissRequest = {
            if (isReading || importProgress != null) {
                return@AlertDialog
            }
            onDismiss()
        },
        title = {
            Text(text = stringResource(R.string.import_prompt_library_dialog_title))
        },
        confirmButton = {

        },
        text = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                Button(
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    },
                    enabled = !isReading && importProgress == null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isReading) {
                        Text(text = stringResource(R.string.reading))
                    } else {
                        Text(text = stringResource(R.string.pickup_a_library_file))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                scanResultList?.let {
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.total_prompts, it.size.toString()))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            importResult()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = importProgress == null

                    ) {
                        Text(text = stringResource(id = R.string.import_btn))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    importProgress?.let { progress ->
                        Text(
                            text = progress.currentScanResult.name,
                        )
                        Text(text = stringResource(
                            R.string.importing,
                            progress.current.toString(),
                            progress.total.toString()
                        ))
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = progress.current.toFloat() / (if (progress.total == 0) 1 else progress.total),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                }
            }
        }
    )

}