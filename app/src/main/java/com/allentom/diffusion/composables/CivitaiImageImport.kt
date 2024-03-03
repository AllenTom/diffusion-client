package com.allentom.diffusion.composables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.text.isDigitsOnly
import coil.compose.AsyncImage
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.civitai.entities.CivitaiImageItem
import com.allentom.diffusion.api.civitai.getCivitaiApiClient
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.api.entity.Model
import com.allentom.diffusion.extension.thenIf
import com.allentom.diffusion.store.prompt.LoraPrompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ImportResource(
    val id: String = Util.randomString(6),
    val type: String,
    val name: String,
    val hash: String?,
    val weight: Float? = null,
    val model: Model? = null,
    val lora: Lora? = null
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CivitaiImageImport(
    onDismiss: () -> Unit,

    ) {
    val useDevice = DetectDeviceType()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var inputSource by remember {
        mutableStateOf("")
    }
    var imageData by remember {
        mutableStateOf<CivitaiImageItem?>(null)
    }
    var prompt =
        imageData?.meta?.prompt?.split(",")?.filter { it.isNotEmpty() }?.map {
            Util.parsePrompt(it)
        } ?: emptyList()
    var negativePrompt =
        imageData?.meta?.negativePrompt?.split(",")?.filter { it.isNotEmpty() }
            ?.map {
                Util.parsePrompt(it)
            } ?: emptyList()
    var imageWidth by remember {
        mutableStateOf(0)
    }
    var imageHeight by remember {
        mutableStateOf(0)
    }
    var resources by remember {
        mutableStateOf<List<ImportResource>>(emptyList())
    }
    var selectedPrompt by remember {
        mutableStateOf(emptyList<String>())
    }
    var selectedNegativePrompt by remember {
        mutableStateOf(emptyList<String>())
    }
    var selectedParamKeys by remember {
        mutableStateOf(emptyList<ImportOptionKeys>())
    }
    var selectedResource by remember {
        mutableStateOf(emptyList<String>())
    }
    val baseParamKeys = listOf(
        ImportOptionKeys.CfgScale,
        ImportOptionKeys.Steps,
        ImportOptionKeys.SamplerName,
        ImportOptionKeys.Width,
        ImportOptionKeys.Height,
        ImportOptionKeys.DenoisingStrength
    )
    val idPattern = Regex("civitai.com/images/(\\d+)")
    fun getIdFromUrl(raw: String): String? {
        if (raw.isEmpty()) return null
        if (raw.isDigitsOnly()) {
            return raw
        }
        val matchResult = idPattern.find(raw)
        return matchResult?.groupValues?.get(1)
    }

    fun fetchFromCivitai() {
        val id = getIdFromUrl(inputSource) ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val images = getCivitaiApiClient().getImageList(imageId = id)
                if (!images.isSuccessful) {
                    return@launch
                }
                imageData = images.body()?.items?.firstOrNull()
                imageData?.let { data ->
                    data.meta?.let { meta ->
                        meta.size?.split("x")?.let {
                            imageWidth = it[0].toInt()
                            imageHeight = it[1].toInt()
                        }
                        meta.resources?.forEach {
                            when (it.type) {
                                "model" -> {
                                    val model = it.hash?.let { hash ->
                                        DrawViewModel.models.find { model ->
                                            model.sha256.startsWith(hash)
                                        }
                                    }
                                    resources = resources + ImportResource(
                                        type = it.type,
                                        name = it.name,
                                        hash = it.hash,
                                        weight = it.weight,
                                        model = model,

                                        )
                                }

                                "lora" -> {
                                    val lora = DrawViewModel.loraList.find { lora ->
                                        lora.name == it.name
                                    }
                                    resources = resources + ImportResource(
                                        type = it.type,
                                        name = it.name,
                                        hash = it.hash,
                                        weight = it.weight,
                                        lora = lora
                                    )
                                }
                            }
                        }

                    }


                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pasteUrlFromClipboard() {
        val clipboardManager =
            context.getSystemService(android.content.ClipboardManager::class.java)
        val clipData = clipboardManager?.primaryClip
        val item = clipData?.getItemAt(0)
        val pasteData = item?.text.toString()
        inputSource = pasteData
    }

    fun onSelectPrompt(prompt: String) {
        if (selectedPrompt.contains(prompt)) {
            selectedPrompt = selectedPrompt.filter { it != prompt }
        } else {
            selectedPrompt = selectedPrompt + prompt
        }
    }

    fun onSelectAllPrompt() {
        selectedPrompt = prompt.map { it.text }
    }

    fun onUnselectAllPrompt() {
        selectedPrompt = emptyList()
    }

    fun onSelectNegativePrompt(prompt: String) {
        if (selectedNegativePrompt.contains(prompt)) {
            selectedNegativePrompt = selectedNegativePrompt.filter { it != prompt }
        } else {
            selectedNegativePrompt = selectedNegativePrompt + prompt
        }
    }

    fun onSelectAllNegativePrompt() {
        selectedNegativePrompt = negativePrompt.map { it.text }
    }

    fun onUnselectAllNegativePrompt() {
        selectedNegativePrompt = emptyList()
    }

    fun onParamClick(key: ImportOptionKeys) {
        if (selectedParamKeys.contains(key)) {
            selectedParamKeys = selectedParamKeys.filter { it != key }
        } else {
            selectedParamKeys = selectedParamKeys + key
        }
    }

    fun onSelectAllBaseParam() {
        selectedParamKeys = baseParamKeys
    }

    fun onUnselectAllBaseParam() {
        selectedParamKeys = emptyList()
    }


    fun onResourceClick(id: String) {
        if (selectedResource.contains(id)) {
            selectedResource = selectedResource.filter { it != id }
        } else {
            selectedResource = selectedResource + id
        }
    }

    fun onSelectAllResource() {
        selectedResource = resources.filter {
            it.model != null || it.lora != null
        }.map {
            it.id
        }
    }

    fun onUnselectAllResource() {
        selectedResource = emptyList()
    }

    fun onSelectAll() {
        onSelectAllPrompt()
        onSelectAllNegativePrompt()
        onSelectAllBaseParam()
        onSelectAllResource()
    }

    fun onUnselectAll() {
        onUnselectAllPrompt()
        onUnselectAllNegativePrompt()
        onUnselectAllBaseParam()
        onUnselectAllResource()
    }

    fun onApply() {
        var baseParam = DrawViewModel.baseParam.copy(
            promptText = prompt.filter { selectedPrompt.contains(it.text) },
            negativePromptText = negativePrompt.filter { selectedNegativePrompt.contains(it.text) },
        )

        selectedParamKeys.forEach { key ->
            when (key) {
                ImportOptionKeys.CfgScale -> {
                    imageData?.meta?.cfgScale?.let {
                        baseParam = baseParam.copy(cfgScale = it)
                    }
                }

                ImportOptionKeys.Steps -> {
                    imageData?.meta?.steps?.let {
                        baseParam = baseParam.copy(steps = it.toInt())
                    }
                }

                ImportOptionKeys.SamplerName -> {
                    imageData?.meta?.sampler?.let {
                        baseParam = baseParam.copy(samplerName = it)
                    }
                }

                ImportOptionKeys.Width -> {
                    baseParam = baseParam.copy(width = imageWidth)
                }

                ImportOptionKeys.Height -> {
                    baseParam = baseParam.copy(height = imageHeight)
                }

                else -> {

                }
            }
        }
        val newLoraList = emptyList<LoraPrompt>().toMutableList()
        selectedResource.forEach { id ->
            val res = resources.find { it.id == id }
            res?.let {
                when (it.type) {
                    "lora" -> {
                        it.lora?.let { lora ->
                            newLoraList += LoraPrompt(
                                name = lora.name,
                                weight = it.weight ?: 0.8f,
                                previewPath = lora.entity?.previewPath,
                                hash = lora.entity?.hash,
                                title = lora.entity?.title ?: "",
                                civitaiId = lora.entity?.civitaiId
                            )
                        }
                    }
                }
            }
        }
        baseParam = baseParam.copy(loraPrompt = newLoraList)
        DrawViewModel.baseParam = baseParam
        val useModel = resources.filter {
            selectedResource.contains(it.id) && it.model != null
        }.firstOrNull()
        scope.launch {
            useModel?.let {
                it.model?.let {
                    if (it.title != DrawViewModel.useModelName) {
                        DrawViewModel.switchModel(it.title)
                    }
                }
            }
            Toast.makeText(context, context.getString(R.string.params_applied), Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }
    if (DrawViewModel.isSwitchingModel) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(id = R.string.dialog_switch_model_title)) },
            text = { Text(stringResource(id = R.string.dialog_switch_model_content)) },
            confirmButton = { },
            dismissButton = { }
        )
    }

    @Composable
    fun second() {
        imageData?.let { imageItem ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.parameters),
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.W600,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = {
                        onSelectAll()
                    }) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_select_all),
                            contentDescription = "Select all"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        onUnselectAll()
                    }) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.ic_unselect_all),
                            contentDescription = "Deselect all"
                        )
                    }

                }
                Divider()
                Box(
                    modifier = Modifier
                        .thenIf(useDevice != DeviceType.Phone, Modifier.weight(1f))
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .thenIf(
                                useDevice != DeviceType.Phone,
                                Modifier.verticalScroll(rememberScrollState())
                            )

                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.param_prompt),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = { onSelectAllPrompt() }) {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.ic_select_all),
                                    contentDescription = "Select all"
                                )

                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { onUnselectAllPrompt() }) {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.ic_unselect_all),
                                    contentDescription = "Deselect all"
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                prompt.forEach {
                                    PromptChip(
                                        prompt = it,
                                        onClickPrompt = {
                                            onSelectPrompt(it.text)
                                        },
                                        selected = selectedPrompt.contains(it.text)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.param_negative_prompt),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = {
                                selectedNegativePrompt =
                                    negativePrompt.map { it.text }
                            }) {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.ic_select_all),
                                    contentDescription = "Select all"
                                )

                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                selectedNegativePrompt = emptyList()
                            }) {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.ic_unselect_all),
                                    contentDescription = "Deselect all"
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                negativePrompt.forEach {
                                    PromptChip(
                                        prompt = it,
                                        onClickPrompt = {
                                            onSelectNegativePrompt(it.text)
                                        },
                                        selected = selectedNegativePrompt.contains(
                                            it.text
                                        )
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.param_base),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = { onSelectAllBaseParam() }) {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.ic_select_all),
                                    contentDescription = "Select all"
                                )

                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { onUnselectAllBaseParam() }) {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.ic_unselect_all),
                                    contentDescription = "Deselect all"
                                )
                            }
                        }

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            imageItem.meta?.let { meta ->
                                MetaItem(
                                    label = stringResource(id = R.string.param_cfg_scale),
                                    value = meta.cfgScale.toString(),
                                    onClick = {
                                        onParamClick(ImportOptionKeys.CfgScale)
                                    },
                                    isSelected = selectedParamKeys.contains(
                                        ImportOptionKeys.CfgScale
                                    )
                                )
                                MetaItem(
                                    label = stringResource(id = R.string.param_steps),
                                    value = meta.steps.toString(),
                                    onClick = {
                                        onParamClick(ImportOptionKeys.Steps)
                                    },
                                    isSelected = selectedParamKeys.contains(
                                        ImportOptionKeys.Steps
                                    )
                                )
                                MetaItem(
                                    label = stringResource(id = R.string.param_sampler),
                                    value = meta.sampler,
                                    onClick = {
                                        onParamClick(ImportOptionKeys.SamplerName)
                                    },
                                    isSelected = selectedParamKeys.contains(
                                        ImportOptionKeys.SamplerName
                                    )
                                )
                                MetaItem(
                                    label = stringResource(id = R.string.param_width),
                                    value = imageWidth.toString(),
                                    onClick = {
                                        onParamClick(ImportOptionKeys.Width)
                                    },
                                    isSelected = selectedParamKeys.contains(
                                        ImportOptionKeys.Width
                                    )
                                )
                                MetaItem(
                                    label = stringResource(id = R.string.param_height),
                                    value = imageHeight.toString(),
                                    onClick = {
                                        onParamClick(ImportOptionKeys.Height)
                                    },
                                    isSelected = selectedParamKeys.contains(
                                        ImportOptionKeys.Height
                                    )
                                )
                                meta.denoisingStrength?.let {
                                    MetaItem(
                                        label = stringResource(id = R.string.param_denoising_strength),
                                        value = it,
                                        onClick = {
                                            onParamClick(ImportOptionKeys.DenoisingStrength)
                                        },
                                        isSelected = selectedParamKeys.contains(
                                            ImportOptionKeys.DenoisingStrength
                                        )
                                    )
                                }
                            }

                        }
                        imageItem.meta?.resources?.let { res ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.civitai_resources),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(onClick = { onSelectAllResource() }) {
                                    Icon(
                                        ImageVector.vectorResource(R.drawable.ic_select_all),
                                        contentDescription = "Select all"
                                    )

                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onUnselectAllResource() }) {
                                    Icon(
                                        ImageVector.vectorResource(R.drawable.ic_unselect_all),
                                        contentDescription = "Deselect all"
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()

                            ) {

                                resources.forEach { resItem ->
                                    when (resItem.type) {
                                        "model" -> {
                                            if (resItem.model != null) {
                                                ListItem(
                                                    headlineContent = {
                                                        Text(resItem.type)
                                                    },
                                                    supportingContent = {
                                                        Text(resItem.model.title)
                                                    },
                                                    colors = ListItemDefaults.colors(
                                                        containerColor = if (selectedResource.contains(
                                                                resItem.id
                                                            )
                                                        ) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                                    ),
                                                    modifier = Modifier
                                                        .clip(
                                                            RoundedCornerShape(
                                                                12.dp
                                                            )
                                                        )
                                                        .clickable {
                                                            onResourceClick(
                                                                resItem.id
                                                            )
                                                        }


                                                )
                                            } else {
                                                ListItem(
                                                    headlineContent = {
                                                        Text(stringResource(R.string.not_installed) + resItem.type)
                                                    },
                                                    supportingContent = {
                                                        Text(resItem.name)
                                                    },
                                                )
                                            }

                                        }

                                        "lora" -> {
                                            if (resItem.lora != null) {
                                                ListItem(
                                                    headlineContent = {
                                                        Text(resItem.type)
                                                    },
                                                    supportingContent = {
                                                        Text(
                                                            resItem.lora.entity?.getDisplayText()
                                                                ?: ""
                                                        )
                                                    },
                                                    colors = ListItemDefaults.colors(
                                                        containerColor = if (selectedResource.contains(
                                                                resItem.id
                                                            )
                                                        ) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                                    ),
                                                    modifier = Modifier
                                                        .clip(
                                                            RoundedCornerShape(
                                                                12.dp
                                                            )
                                                        )
                                                        .clickable {
                                                            onResourceClick(
                                                                resItem.id
                                                            )
                                                        }
                                                )
                                            } else {
                                                ListItem(
                                                    headlineContent = {
                                                        Text(stringResource(R.string.not_installed) + resItem.type)
                                                    },
                                                    supportingContent = {
                                                        Text(resItem.name)
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun first() {
        imageData?.let { imageItem ->
            AsyncImage(
                model = imageItem.url,
                contentDescription = "Civitai Image",
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onApply()
            }) {
                Text(stringResource(id = R.string.apply))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(R.string.import_from_civitai_image))
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxSize(),
        text = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    OutlinedTextField(
                        value = inputSource,
                        onValueChange = {
                            inputSource = it
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = {
                                    pasteUrlFromClipboard()
                                }) {
                                    Icon(
                                        ImageVector.vectorResource(R.drawable.ic_paste),
                                        contentDescription = "Paste"
                                    )
                                }
                                IconButton(onClick = {
                                    fetchFromCivitai()
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }

                        },
                        placeholder = {
                            Text(stringResource(R.string.url_or_id_of_the_civitai_image))
                        },
                        maxLines = 1
                    )
                    imageData?.let { imageItem ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .thenIf(
                                        useDevice == DeviceType.Phone,
                                        Modifier.verticalScroll(rememberScrollState())
                                    )
                            ) {
                                if (useDevice == DeviceType.Phone) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(300.dp)
                                        ) {
                                            first()
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        second()
                                    }

                                }else{
                                    first()
                                }
                            }
                            if (useDevice != DeviceType.Phone) {
                                Box(
                                    modifier = Modifier
                                        .weight(2f)
                                        .padding(start = 16.dp, end = 16.dp)
                                        .fillMaxHeight()
                                ) {
                                    second()
                                }
                            }


                        }

                    }

                }
            }
        }
    )
}

@Composable
fun MetaItem(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean = false,
    value: String,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                onClick()
            }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
            .padding(8.dp),
    ) {
        Text(
            text = label,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
    }

}