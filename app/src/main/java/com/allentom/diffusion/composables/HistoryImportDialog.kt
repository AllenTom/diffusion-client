package com.allentom.diffusion.composables

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.api.entity.Lora
import com.allentom.diffusion.store.export.ExportHistory
import com.allentom.diffusion.store.export.ExportLoraPrompt
import com.allentom.diffusion.store.prompt.LoraPrompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.ControlNetSlot
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ImportOptionKeys {
    Prompt,
    NegativePrompt,
    LoraPrompt,
    Steps,
    SamplerName,
    Width,
    Height,
    CfgScale,
    ReactorParam,
    ControlNetSlotEnable,
    ControlNetSlotInputImage,
    ControlNetSlotGuidanceEnd,
    ControlNetSlotGuidanceStart,
    ControlNetSlotWeight,
    ControlNetSlotModel,
    ControlNetSlotControlMode,
    ControlNetSlotControlType,
    ControlNetSlotPreprocessor,
    ControlNetSlotResizeMode,
    ControlNetSlotThresholdA,
    ControlNetSlotThresholdB,
    ControlNetSlotProcessorRes,
}

data class ImportLoraPrompt(
    val importLora: ExportLoraPrompt,
    val lora: Lora?
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryImportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isReading by remember {
        mutableStateOf(false)
    }
    var exportHistory by remember {
        mutableStateOf(null as ExportHistory?)
    }
    var importProgress by remember {
        mutableStateOf(null as ImportProgress?)
    }
    var selectedOption by remember {
        mutableStateOf<List<ImportOptionKeys>>(emptyList())
    }
    var selectedSlotOption by remember {
        mutableStateOf<List<Pair<Int, ImportOptionKeys>>>(emptyList())
    }
    var importLoraPrompt by remember {
        mutableStateOf<List<ImportLoraPrompt>>(emptyList())
    }
    val baseParamKeys = listOf(
        ImportOptionKeys.Prompt,
        ImportOptionKeys.NegativePrompt,
        ImportOptionKeys.LoraPrompt,
        ImportOptionKeys.Steps,
        ImportOptionKeys.SamplerName,
        ImportOptionKeys.Width,
        ImportOptionKeys.Height,
        ImportOptionKeys.CfgScale
    )
    val controlSlotKeys = listOf(
        ImportOptionKeys.ControlNetSlotEnable,
        ImportOptionKeys.ControlNetSlotGuidanceStart,
        ImportOptionKeys.ControlNetSlotGuidanceEnd,
        ImportOptionKeys.ControlNetSlotControlMode,
        ImportOptionKeys.ControlNetSlotWeight,
        ImportOptionKeys.ControlNetSlotModel,
        ImportOptionKeys.ControlNetSlotControlType,
        ImportOptionKeys.ControlNetSlotPreprocessor,
        ImportOptionKeys.ControlNetSlotProcessorRes,
        ImportOptionKeys.ControlNetSlotThresholdA,
        ImportOptionKeys.ControlNetSlotThresholdB,
        ImportOptionKeys.ControlNetSlotResizeMode,
        ImportOptionKeys.ControlNetSlotInputImage
    )


    fun onOptionClick(key: ImportOptionKeys) {
        if (selectedOption.contains(key)) {
            selectedOption = selectedOption.toMutableList().apply {
                remove(key)
            }
        } else {
            selectedOption = selectedOption.toMutableList().apply {
                add(key)
            }
        }
    }

    fun onControlNetSlotClick(slotIndex: Int, optionKeys: ImportOptionKeys) {
        if (selectedSlotOption.contains(slotIndex to optionKeys)) {
            selectedSlotOption = selectedSlotOption.toMutableList().apply {
                remove(slotIndex to optionKeys)
            }
        } else {
            selectedSlotOption = selectedSlotOption.toMutableList().apply {
                add(slotIndex to optionKeys)
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
                    val inputStream = context.contentResolver.openInputStream(it)
                    inputStream?.let {
                        val text = it.bufferedReader().use { reader -> reader.readText() }
                        exportHistory = ExportHistory.readFromRaw(text)
                        exportHistory?.let { exportHistory ->
                            importLoraPrompt = exportHistory.loraPrompt?.map { exportLora ->
                                ImportLoraPrompt(
                                    importLora = exportLora,
                                    lora = DrawViewModel.loraList.find { it.name == exportLora.name }
                                )
                            } ?: emptyList()

                        }
                    }
                } catch (e: Exception) {
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                    // Handle exception
                } finally {
                    isReading = false
                }
            }
        }
    }

    fun selectAllBaseParam() {
        selectedOption = (selectedOption + baseParamKeys).toSet().toList()
    }

    fun unSelectAllBaseParam() {
        selectedOption = selectedOption.filter { !baseParamKeys.contains(it) }
    }

    fun isAllSelectedBaseParam(): Boolean {
        return baseParamKeys.all { selectedOption.contains(it) }
    }

    fun onBaseParamClick() {
        if (isAllSelectedBaseParam()) {
            unSelectAllBaseParam()
        } else {
            selectAllBaseParam()
        }
    }

    fun selectAllSlotByIndex(idx: Int) {
        selectedSlotOption =
            selectedSlotOption.filter { it.first != idx } + controlSlotKeys.map { idx to it }
    }

    fun unSelectAllSlotByIndex(idx: Int) {
        selectedSlotOption = selectedSlotOption.filter { it.first != idx }
    }

    fun isAllSelectedSlotByIndex(idx: Int): Boolean {
        return controlSlotKeys.all { selectedSlotOption.contains(idx to it) }
    }

    fun onSlotClick(idx: Int) {
        if (isAllSelectedSlotByIndex(idx)) {
            unSelectAllSlotByIndex(idx)
        } else {
            selectAllSlotByIndex(idx)
        }
    }

    fun selectAllSlot() {
        val newSelectedSlotOption = mutableListOf<Pair<Int, ImportOptionKeys>>()
        for (slotIndex in exportHistory?.controlNetParam?.slots?.indices ?: emptyList()) {
            newSelectedSlotOption.addAll(controlSlotKeys.map { slotIndex to it })
        }
        selectedSlotOption = newSelectedSlotOption
    }

    fun unSelectAllSlot() {
        selectedSlotOption = emptyList()
    }

    fun applyImport() {
        scope.launch {
            selectedOption.forEach {
                var newBaseParam = DrawViewModel.baseParam.copy()
                when (it) {
                    ImportOptionKeys.Prompt -> {
                        exportHistory?.prompt?.let {
                            newBaseParam = newBaseParam.copy(promptText = it.map { it.toPrompt() })
                        }
                    }

                    ImportOptionKeys.NegativePrompt -> {
                        exportHistory?.negativePrompt?.let {
                            newBaseParam =
                                newBaseParam.copy(negativePromptText = it.map { it.toPrompt() })
                        }
                    }

                    ImportOptionKeys.Steps -> {
                        exportHistory?.steps?.let {
                            newBaseParam = newBaseParam.copy(steps = it)
                        }
                    }

                    ImportOptionKeys.SamplerName -> {
                        exportHistory?.samplerName?.let {
                            newBaseParam = newBaseParam.copy(samplerName = it)
                        }
                    }

                    ImportOptionKeys.Width -> {
                        exportHistory?.width?.let {
                            newBaseParam = newBaseParam.copy(width = it)
                        }
                    }

                    ImportOptionKeys.Height -> {
                        exportHistory?.height?.let {
                            newBaseParam = newBaseParam.copy(height = it)
                        }
                    }

                    ImportOptionKeys.CfgScale -> {
                        exportHistory?.cfgScale?.let {
                            newBaseParam = newBaseParam.copy(cfgScale = it)
                        }
                    }

                    ImportOptionKeys.LoraPrompt -> {
                        val loraList = mutableListOf<LoraPrompt>()
                        importLoraPrompt.forEach { importLoraPromptItem ->
                            val ent = importLoraPromptItem.lora
                            val loraEnt = ent?.entity
                            if (ent == null || loraEnt == null) {
                                return@forEach
                            }
                            loraList.add(
                                loraEnt.copy(
                                    weight = importLoraPromptItem.importLora.weight,
                                    prompts = importLoraPromptItem.importLora.prompts.map { it.toPrompt() }
                                )
                            )
                        }
                        newBaseParam = newBaseParam.copy(loraPrompt = loraList)
                    }

                    else -> {
                        // do nothing
                    }
                }
                DrawViewModel.baseParam = newBaseParam
            }
            // group by slot index
            // 1,2,3 slot
            if (selectedSlotOption.isNotEmpty()) {
                val newSlots = mutableListOf<ControlNetSlot>()
                for (slotIndex in selectedSlotOption.groupBy { it.first }.keys) {
                    exportHistory?.controlNetParam?.slots?.get(slotIndex)?.let { slot ->
                        val selectedSlotKeys =
                            selectedSlotOption.filter { it.first == slotIndex }.map { it.second }
                        var newSlot = ControlNetSlot()
                        selectedSlotKeys.forEach { slotKey ->
                            when (slotKey) {
                                ImportOptionKeys.ControlNetSlotEnable -> {
                                    slot.enabled?.let {
                                        newSlot = newSlot.copy(enabled = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotGuidanceStart -> {
                                    slot.guidanceStart?.let {
                                        newSlot = newSlot.copy(guidanceStart = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotGuidanceEnd -> {
                                    slot.guidanceEnd?.let {
                                        newSlot = newSlot.copy(guidanceEnd = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotControlMode -> {
                                    slot.controlMode?.let {
                                        newSlot = newSlot.copy(controlMode = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotWeight -> {
                                    slot.weight?.let {
                                        newSlot = newSlot.copy(weight = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotModel -> {
                                    slot.model?.let {
                                        newSlot = newSlot.copy(model = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotControlType -> {
                                    slot.controlType?.let {
                                        newSlot = newSlot.copy(controlType = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotPreprocessor -> {
                                    slot.preprocessor?.let {
                                        newSlot = newSlot.copy(preprocessor = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotProcessorRes -> {
                                    slot.processorRes?.let {
                                        newSlot = newSlot.copy(processorRes = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotThresholdA -> {
                                    slot.thresholdA?.let {
                                        newSlot = newSlot.copy(thresholdA = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotThresholdB -> {
                                    slot.thresholdB?.let {
                                        newSlot = newSlot.copy(thresholdB = it)
                                    }
                                }

                                ImportOptionKeys.ControlNetSlotResizeMode -> {
                                    slot.resizeMode?.let {
                                        newSlot = newSlot.copy(resizeMode = it)
                                    }
                                }
                                ImportOptionKeys.ControlNetSlotInputImage -> {
                                    slot.inputImage?.let {
                                        newSlot = newSlot.copy(inputImage = it)
                                    }
                                }

                                else -> {
                                    // do nothing
                                }
                            }
                        }
                        newSlots.add(newSlot)
                    }
                }
                DrawViewModel.inputControlNetParams =
                    DrawViewModel.inputControlNetParams.copy(slots = newSlots)
            }
        }
        onDismiss()
    }
    AlertDialog(
        onDismissRequest = {
            if (isReading || importProgress != null) {
                return@AlertDialog
            }
            onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.import_history), modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    selectAllBaseParam()
                    selectAllSlot()
                }) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_select_all),
                        contentDescription = null
                    )
                }
                IconButton(onClick = {
                    unSelectAllBaseParam()
                    selectAllSlot()
                }) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_unselect_all),
                        contentDescription = null
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    },
                    enabled = !isReading && importProgress == null,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isReading) {
                        Text(text = stringResource(R.string.reading))
                    } else {
                        Text(text = stringResource(R.string.pick_up_a_file))
                    }
                }
                if (exportHistory != null) {
                    Button(
                        onClick = {
                            scope.launch {
                                applyImport()
                            }
                        },
                        enabled = !isReading && importProgress == null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.apply))
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    exportHistory?.let { exportHistory ->
                        with(exportHistory) {
                            Text(
                                stringResource(id = R.string.param_base),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        onBaseParamClick()
                                    },
                                fontWeight = FontWeight.W600
                            )
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            prompt?.let {
                                if (prompt.isNullOrEmpty()) {
                                    return@let
                                }
                                OptionSelectItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    label = stringResource(id = R.string.param_prompt),
                                    isSelected = selectedOption.contains(ImportOptionKeys.Prompt),
                                    value = it.joinToString(",") { it.toPrompt().getPromptText() }
                                ) {
                                    onOptionClick(ImportOptionKeys.Prompt)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            negativePrompt?.let {
                                if (negativePrompt.isNullOrEmpty()) {
                                    return@let
                                }
                                OptionSelectItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    label = stringResource(id = R.string.param_negative_prompt),
                                    isSelected = selectedOption.contains(ImportOptionKeys.NegativePrompt),
                                    value = it.joinToString(",") { it.toPrompt().getPromptText() }
                                ) {
                                    onOptionClick(ImportOptionKeys.NegativePrompt)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                steps?.let {
                                    OptionSelectItem(
                                        label = stringResource(id = R.string.param_steps),
                                        isSelected = selectedOption.contains(ImportOptionKeys.Steps),
                                        value = it.toString()
                                    ) {
                                        onOptionClick(ImportOptionKeys.Steps)
                                    }
                                }
                                samplerName?.let {
                                    OptionSelectItem(
                                        label = stringResource(id = R.string.param_sampler),
                                        isSelected = selectedOption.contains(ImportOptionKeys.SamplerName),
                                        value = it
                                    ) {
                                        onOptionClick(ImportOptionKeys.SamplerName)
                                    }
                                }
                                width?.let {
                                    OptionSelectItem(
                                        label = stringResource(id = R.string.param_width),
                                        isSelected = selectedOption.contains(ImportOptionKeys.Width),
                                        value = it.toString()
                                    ) {
                                        onOptionClick(ImportOptionKeys.Width)
                                    }
                                }
                                height?.let {
                                    OptionSelectItem(
                                        label = stringResource(id = R.string.param_height),
                                        isSelected = selectedOption.contains(ImportOptionKeys.Height),
                                        value = it.toString()
                                    ) {
                                        onOptionClick(ImportOptionKeys.Height)
                                    }
                                }
                                cfgScale?.let {
                                    OptionSelectItem(
                                        label = stringResource(id = R.string.param_scale_by),
                                        isSelected = selectedOption.contains(ImportOptionKeys.CfgScale),
                                        value = it.toString()
                                    ) {
                                        onOptionClick(ImportOptionKeys.CfgScale)
                                    }
                                }

                            }
                            if (importLoraPrompt.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onOptionClick(ImportOptionKeys.LoraPrompt)
                                        }
                                        .background(
                                            if (selectedOption.contains(ImportOptionKeys.LoraPrompt)) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                        )
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(text = "Lora")
                                        importLoraPrompt.let { importLoraPrompt ->
                                            importLoraPrompt.forEach {
                                                Box {
                                                    Column {
                                                        Text(text = it.importLora.name)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        FlowRow(
                                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                8.dp
                                                            )
                                                        ) {
                                                            it.importLora.prompts.forEach { loraPrompt ->
                                                                Box(
                                                                    modifier = Modifier
                                                                        .border(
                                                                            width = 1.dp,
                                                                            color = MaterialTheme.colorScheme.primary.copy(
                                                                                alpha = 0.8f
                                                                            ),
                                                                            shape = RoundedCornerShape(8.dp)
                                                                        )
                                                                        .padding(8.dp)
                                                                ) {
                                                                    Text(text = loraPrompt.text)
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

                            exportHistory.controlNetParam?.let { controlNetParam ->
                                if (controlNetParam.slots.isNotEmpty()) {
                                    Text(
                                        "ControlNet",
                                        modifier = Modifier.padding(8.dp),
                                        fontWeight = FontWeight.W600
                                    )
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    controlNetParam.slots.forEachIndexed { idx, slot ->
                                        Text(
                                            text = stringResource(id = R.string.slot, idx + 1),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .clickable {
                                                    onSlotClick(idx)
                                                }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        slot.inputImage?.let {
                                            Column(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .fillMaxWidth()
                                                    .background(if (selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotInputImage)) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                                    .clickable {
                                                        onControlNetSlotClick(
                                                            idx,
                                                            ImportOptionKeys.ControlNetSlotInputImage
                                                        )
                                                    }
                                                    .padding(8.dp)

                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(120.dp)
                                                        .height(120.dp)
                                                ) {
                                                    DisplayBase64Image(base64String = it)
                                                }
                                            }

                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {

                                            slot.enabled?.let {
                                                OptionSelectItem(
                                                    label = stringResource(id = R.string.enable),
                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotEnable),
                                                    value = it.toString()
                                                ) {
                                                    onControlNetSlotClick(
                                                        idx,
                                                        ImportOptionKeys.ControlNetSlotEnable
                                                    )
                                                }
                                            }
                                            slot.guidanceStart?.let {
                                                OptionSelectItem(
                                                    label = stringResource(id = R.string.controlnet_guidance_start),
                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotGuidanceStart),
                                                    value = it.toString()
                                                ) {
                                                    onControlNetSlotClick(
                                                        idx,
                                                        ImportOptionKeys.ControlNetSlotGuidanceStart
                                                    )
                                                }
                                            }
                                            slot.guidanceEnd?.let {
                                                OptionSelectItem(
                                                    label = stringResource(id = R.string.controlnet_guidance_end),
                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotGuidanceEnd),
                                                    value = it.toString()
                                                ) {
                                                    onControlNetSlotClick(
                                                        idx,
                                                        ImportOptionKeys.ControlNetSlotGuidanceEnd
                                                    )
                                                }
                                            }
                                            slot.controlMode?.let {
                                                OptionSelectItem(
                                                    label = stringResource(id = R.string.param_control_mode),
                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotControlMode),
                                                    value = ConstValues.ControlNetModeList[it]
                                                ) {
                                                    onControlNetSlotClick(
                                                        idx,
                                                        ImportOptionKeys.ControlNetSlotControlMode
                                                    )
                                                }
                                            }
                                            slot.weight?.let {
                                                OptionSelectItem(
                                                    label = stringResource(id = R.string.controlnet_weight),
                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotWeight),
                                                    value = it.toString()
                                                ) {
                                                    onControlNetSlotClick(
                                                        idx,
                                                        ImportOptionKeys.ControlNetSlotWeight
                                                    )
                                                }
                                            }
                                            slot.model?.let {
                                                OptionSelectItem(
                                                    label = stringResource(id = R.string.param_control_model),
                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotModel),
                                                    value = it
                                                ) {
                                                    onControlNetSlotClick(
                                                        idx,
                                                        ImportOptionKeys.ControlNetSlotModel
                                                    )
                                                }
                                            }
                                            slot.controlType?.let {
                                                OptionSelectItem(
                                                    label = stringResource(R.string.controlnet_controltype),
                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotControlType),
                                                    value = it
                                                ) {
                                                    onControlNetSlotClick(
                                                        idx,
                                                        ImportOptionKeys.ControlNetSlotControlType
                                                    )
                                                }
                                            }
                                            slot.preprocessor?.let {
                                                OptionSelectItem(
                                                    label = stringResource(R.string.controlnet_preprocessor),
                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotPreprocessor),
                                                    value = it
                                                ) {
                                                    onControlNetSlotClick(
                                                        idx,
                                                        ImportOptionKeys.ControlNetSlotPreprocessor
                                                    )
                                                }
                                            }
                                            DrawViewModel.modulesDetailList.get(slot.preprocessor)?.let { detail ->
                                                detail.sliders.forEachIndexed {index, slider ->
                                                    if (slider == null ){
                                                        return@forEachIndexed
                                                    }
                                                    when(index) {
                                                        0 ->
                                                            slot.processorRes?.let {
                                                                OptionSelectItem(
                                                                    label = slider.name,
                                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotProcessorRes),
                                                                    value = it.toString()
                                                                ) {
                                                                    onControlNetSlotClick(
                                                                        idx,
                                                                        ImportOptionKeys.ControlNetSlotProcessorRes
                                                                    )
                                                                }
                                                            }
                                                        1 ->
                                                            slot.thresholdA?.let {
                                                                OptionSelectItem(
                                                                    label = slider.name,
                                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotThresholdA),
                                                                    value = it.toString()
                                                                ) {
                                                                    onControlNetSlotClick(
                                                                        idx,
                                                                        ImportOptionKeys.ControlNetSlotThresholdA
                                                                    )
                                                                }
                                                            }
                                                        2 ->
                                                            slot.thresholdB?.let {
                                                                OptionSelectItem(
                                                                    label = slider.name,
                                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotThresholdB),
                                                                    value = it.toString()
                                                                ) {
                                                                    onControlNetSlotClick(
                                                                        idx,
                                                                        ImportOptionKeys.ControlNetSlotThresholdB
                                                                    )
                                                                }
                                                            }
                                                    }
                                                }

                                            }
                                            slot.resizeMode?.let {
                                                OptionSelectItem(
                                                    label = stringResource(R.string.controlnet_resizemode),
                                                    isSelected = selectedSlotOption.contains(idx to ImportOptionKeys.ControlNetSlotResizeMode),
                                                    value = it.toString()
                                                ) {
                                                    onControlNetSlotClick(
                                                        idx,
                                                        ImportOptionKeys.ControlNetSlotResizeMode
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    )

}

@Composable
fun OptionSelectItem(
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