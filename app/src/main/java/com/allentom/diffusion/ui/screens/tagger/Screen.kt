package com.allentom.diffusion.ui.screens.tagger

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R
import com.allentom.diffusion.api.InterrogateRequest
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.composables.ActionItem
import com.allentom.diffusion.composables.BottomActionSheet
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaggerScreen() {
    val context = LocalContext.current
    var imageBase64 by remember { mutableStateOf<String?>(null) }
    var caption by remember { mutableStateOf<List<String>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var isCaptioning by remember { mutableStateOf(false) }
    var selectedCaption by remember { mutableStateOf<List<String>>(emptyList()) }
    var useTaggerName by remember { mutableStateOf("deepdanbooru") }
    var isParamOpen by remember { mutableStateOf(false) }
    var sendOptionOpen by remember { mutableStateOf(false) }
    suspend fun onCaption() {
        val image = imageBase64 ?: return
        selectedCaption = emptyList()
        isCaptioning = true
        val result = getApiClient().interrogate(
            request = InterrogateRequest(
                image = image,
                model = useTaggerName
            )
        )
        caption = result.body()?.caption?.split(",") ?: emptyList()
        isCaptioning = false
    }

    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)

                if (imageBase64 != null) {
                    scope.launch {
                        onCaption()
                    }
                }
                // Use the base64 string
            }
        }


    fun pickImageFromGalleryAndConvertToBase64() {
        pickImageLauncher.launch("image/*")

    }
    if (isParamOpen) {
        ModalBottomSheet(onDismissRequest = {
            isParamOpen = false
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                TextPickUpItem(
                    label = stringResource(R.string.tagger_mode),
                    value = useTaggerName,
                    options = listOf("deepdanbooru", "clip"),
                    onValueChange = {
                        useTaggerName = it
                    })
            }
        }
    }
    if (sendOptionOpen) {
        BottomActionSheet(items = listOf(
            ActionItem(
                text = stringResource(R.string.add_to_prompt),
                onAction = {
                    DrawViewModel.baseParam = DrawViewModel.baseParam.copy(
                        promptText = DrawViewModel.baseParam.promptText.filter {
                            it.text !in selectedCaption
                        } + selectedCaption.map { captionText ->
                            Prompt(captionText, 0)
                        }
                    )

                    Toast.makeText(
                        context,
                        context.getString(R.string.added_to_prompt, selectedCaption.size.toString()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ),
            ActionItem(
                text = stringResource(R.string.assign_to_prompt),
                onAction = {
                    DrawViewModel.baseParam = DrawViewModel.baseParam.copy(
                        promptText = selectedCaption.map { captionText ->
                            Prompt(captionText, 0)
                        }
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.assigned_to_prompt, selectedCaption.size.toString()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ),
        )) {
            sendOptionOpen = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.tagger_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                Box(Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            imageBase64?.let { base64 ->
                                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = stringResource(R.string.selected_image),
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column {
                                caption.takeIf { it.isNotEmpty() }?.let {
                                    Row {
                                        Text(
                                            text = stringResource(R.string.prompts),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = if (selectedCaption.isEmpty()) stringResource(R.string.select_all) else stringResource(
                                                R.string.deselect_all
                                            ),
                                            modifier = Modifier.clickable {
                                                if (selectedCaption.isEmpty()) {
                                                    selectedCaption = caption
                                                } else {
                                                    selectedCaption = emptyList()
                                                }
                                            })
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        caption.forEach {
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                            ) {
                                                FilterChip(
                                                    onClick = {
                                                        if (it in selectedCaption) {
                                                            selectedCaption =
                                                                selectedCaption.filter { selected -> selected != it }
                                                        } else {
                                                            selectedCaption += it
                                                        }
                                                    },
                                                    label = {
                                                        Text(it)
                                                    },
                                                    selected = it in selectedCaption,

                                                    )
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            if (caption.isNotEmpty() && selectedCaption.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        sendOptionOpen = true
                                    },
                                    enabled = !isCaptioning,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    Text(stringResource(R.string.send_to_prompt))
                                }
                            }
                            if (selectedCaption.isEmpty()) {
                                Button(
                                    onClick = {
                                        isParamOpen = true
                                    },
                                    enabled = !isCaptioning,
                                    modifier = Modifier
                                ) {
                                    Text(stringResource(R.string.param))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Button(
                                    onClick = {
                                        pickImageFromGalleryAndConvertToBase64()
                                    },
                                    enabled = !isCaptioning,
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(stringResource(R.string.select_image))
                                }
                            }

                        }
                    }
                }
                DrawBar()
            }
        }
    }
}
