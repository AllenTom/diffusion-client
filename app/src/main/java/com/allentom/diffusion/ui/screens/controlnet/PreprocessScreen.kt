package com.allentom.diffusion.ui.screens.controlnet

import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.ControlNetDetectRequest
import com.allentom.diffusion.api.entity.ModuleDetail
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.ImageBase64PickupOptionItem
import com.allentom.diffusion.composables.IsWideWindow
import com.allentom.diffusion.composables.SliderOptionItem
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.store.ControlNetStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

data class ControlNetPreprocessParams(
    val processorRes: Float = 512f,
    val thresholdA: Float = 64f,
    val thresholdB: Float = 64f,
    val module: String = "none",
    val inputImageBase64: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlNetPreprocessScreen() {
    var modules by remember {
        mutableStateOf<List<String>>(emptyList())
    }
    var modulesDetailList by remember {
        mutableStateOf<Map<String, ModuleDetail>>(emptyMap())
    }

    var params by remember {
        mutableStateOf<ControlNetPreprocessParams>(ControlNetPreprocessParams())
    }
    var resultImageBase64 by remember {
        mutableStateOf<String?>(null)
    }
    val isWideDisplay = IsWideWindow()
    var isParamPanelOpen by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    fun saveResultImage() {
        resultImageBase64?.let {
            val savePath = Util.saveControlNetImageBase64ToAppData(context,it)
            scope.launch(Dispatchers.IO) {
                ControlNetStore.addControlNet(context, Uri.fromFile(File(savePath)))
            }
        }
    }
    var isPreprocessing by remember {
        mutableStateOf(false)
    }

    suspend fun loadModules() {
        val result = getApiClient().getControlNetModuleList()
        result.isSuccessful.takeIf { it }?.let {
            result.body()?.let {
                modules = it.moduleList
                modulesDetailList = it.detail
            }
        }
    }
    LaunchedEffect(Unit) {
        loadModules()
    }
    if (isParamPanelOpen) {
        ModalBottomSheet(onDismissRequest = {
            isParamPanelOpen = false
        }) {
            SecondScreen(
                params = params,
                onParamsChange = {
                    params = it
                },
                moduleList = modulesDetailList
            )
        }
    }
    if (isPreprocessing) {
        BackHandler {
            //do nothing
        }
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.control_net_preprocess_screen_title)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }

    ) { paddingValues: PaddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            FirstScreen(resultImageBase64 = resultImageBase64)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            if (!isWideDisplay && !isPreprocessing) {
                                Button(onClick = {
                                    isParamPanelOpen = true
                                }) {
                                    Text(text = stringResource(id = R.string.param))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            if (resultImageBase64 != null) {
                                Button(onClick = {
                                    saveResultImage()
                                }) {
                                    Text(text = stringResource(R.string.save))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !isPreprocessing && params.inputImageBase64 != null,
                                onClick = {
                                    scope.launch {
                                        try {
                                            isPreprocessing = true
                                            val result = getApiClient().detect(
                                                ControlNetDetectRequest(
                                                    controlNetInputImages = listOf(params.inputImageBase64!!),
                                                    controlNetModule = params.module,
                                                    controlNetProcessorRes = params.processorRes,
                                                    controlNetThresholdA = params.thresholdA,
                                                    controlNetThresholdB = params.thresholdB,
                                                )

                                            )
                                            result.isSuccessful.takeIf { it }?.let {
                                                result.body()?.let {
                                                    resultImageBase64 = it.images.first()
                                                }
                                            }
                                        }catch (e:Exception) {
                                            return@launch
                                        }finally {
                                            isPreprocessing = false
                                        }

                                    }

                                }) {
                                Text(text = stringResource(R.string.control_net_preprocess_btn))
                            }


                        }
                    }
                }
                if (isWideDisplay) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        SecondScreen(
                            params = params,
                            onParamsChange = {
                                params = it
                            },
                            moduleList = modulesDetailList
                        )
                    }
                }

            }

        }

    }
}


@Composable
fun FirstScreen(
    resultImageBase64: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                resultImageBase64?.let {
                    DisplayBase64Image(base64String = it)
                }
            }
        }
    }
}

@Composable
fun SecondScreen(
    params: ControlNetPreprocessParams,
    onParamsChange: (ControlNetPreprocessParams) -> Unit,
    moduleList: Map<String, ModuleDetail>
) {
    val moduleDetail = moduleList[params.module]
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            item {
                ImageBase64PickupOptionItem(label = stringResource(R.string.source),
                    value = params.inputImageBase64,
                    onValueChange = { fileUri, imgBase64, filename, width, height ->
                        onParamsChange(params.copy(inputImageBase64 = imgBase64))
                    })
                TextPickUpItem(
                    label = stringResource(R.string.control_net_preprocess_module),
                    value = params.module,
                    options = moduleList.keys.toList()
                ) {
                    var newParam = params.copy(module = it)
                    val selectedDetail = moduleList[it]
                    selectedDetail?.let {
                        it.sliders.forEachIndexed { idx, sl ->
                            if (sl == null) {
                                return@forEachIndexed
                            }
                            when (idx) {
                                0 -> {
                                    newParam = newParam.copy(processorRes = sl.value)
                                }
                                1 -> {
                                    newParam = newParam.copy(thresholdA = sl.value)
                                }
                                2 -> {
                                    newParam = newParam.copy(thresholdB = sl.value)
                                }
                            }
                        }
                    }
                    onParamsChange(newParam)
                }
                moduleDetail?.let {
                    moduleDetail.sliders.forEachIndexed { idx, sl ->
                        if (sl == null) {
                            return@forEachIndexed
                        }
                        when (idx) {
                            0 -> {
                                SliderOptionItem(
                                    value = params.processorRes,
                                    onValueChangeFloat = {
                                        onParamsChange(params.copy(processorRes = it))
                                    },
                                    valueRange = sl.min..sl.max,
                                    label = sl.name,
                                    steps = sl.step.let {
                                        if (it == null) {
                                            return@let 0
                                        }
                                        return@let ((sl.max - sl.min) / it).toInt()
                                    }
                                )
                            }
                            1 -> {
                                SliderOptionItem(
                                    value = params.thresholdA,
                                    onValueChangeFloat = {
                                        onParamsChange(params.copy(thresholdA = it))

                                    },
                                    valueRange = sl.min..sl.max,
                                    label = sl.name,
                                    steps = sl.step.let {
                                        if (it == null) {
                                            return@let 0
                                        }
                                        return@let ((sl.max - sl.min) / it).toInt()
                                    }
                                )
                            }
                            2 -> {
                                SliderOptionItem(
                                    value = params.thresholdB,
                                    onValueChangeFloat = {
                                        onParamsChange(params.copy(thresholdB = it))

                                    },
                                    valueRange = sl.min..sl.max,
                                    label = sl.name,
                                    steps = sl.step.let {
                                        if (it == null) {
                                            return@let 0
                                        }
                                        return@let ((sl.max - sl.min) / it).toInt()
                                    }
                                )
                            }
                        }

                    }
                }
            }

        }
    }
}