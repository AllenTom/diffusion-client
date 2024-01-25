package com.allentom.diffusion.composables

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image
import io.getstream.sketchbook.Sketchbook
import io.getstream.sketchbook.rememberSketchbookController

data class InpaintCanvasState(
    val canvasWidth: Int? = null,
    val canvasHeight: Int? = null,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val containerWidth: Int? = null,
    val containerHeight: Int? = null,
)

object SketchbookViewModel {
    var paths = mutableListOf<Path>()
    fun combinePaths() {
        val path = Path()
        paths.forEach {
            path.addPath(it)
        }
        paths = mutableListOf(path)
    }

    fun undo() {
        if (paths.size > 1) {
            paths.removeLast()
        }
    }

    fun addPath(path: Path) {
        paths.add(path)
    }

    fun clear() {
        paths = mutableListOf()
    }

}

@Composable
fun InpaintDialog(
    onDismiss: () -> Unit,
    onConfirm: (imageBitmap: ImageBitmap) -> Unit,
    backgroundImageBase64: String? = null,
) {
    val sketchbookController = rememberSketchbookController().apply {
        setPaintColor(Color.Red)
        setPaintStrokeWidth(20f)
        setPaintAlpha(1f)
        SketchbookViewModel.paths.forEach {
            addDrawPath(it)
        }
    }
    val density = LocalDensity.current.density
    var canvasState by remember {
        mutableStateOf(
            InpaintCanvasState()
        )
    }
    val undoIcon = ImageVector.vectorResource(id = R.drawable.ic_undo)
    val lineWeightIcon = ImageVector.vectorResource(id = R.drawable.ic_line_weight)
    var isLineWeightDropdownShow by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        backgroundImageBase64?.let {
            val pair = Util.getDimensionsFromBase64(it)
            canvasState = canvasState.copy(
                imageWidth = pair.first,
                imageHeight = pair.second
            )
        }
    }
    LaunchedEffect(canvasState) {
        if (
            canvasState.containerWidth != null &&
            canvasState.containerHeight != null &&
            canvasState.imageWidth != null &&
            canvasState.imageHeight != null
        ) {
            if (canvasState.canvasWidth == null || canvasState.canvasHeight == null) {
                val pair = Util.calculateActualSize(
                    canvasState.containerWidth!!,
                    canvasState.containerHeight!!,
                    canvasState.imageWidth!!,
                    canvasState.imageHeight!!
                )
                canvasState = canvasState.copy(
                    canvasWidth = pair.first,
                    canvasHeight = pair.second
                )
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            SketchbookViewModel.combinePaths()
        }
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxSize(),
        confirmButton = {
            Button(onClick = {
                val originalRaw = sketchbookController.getSketchbookBitmap().asAndroidBitmap()
                val scaledBitmap =
                    Bitmap.createScaledBitmap(
                        originalRaw,
                        canvasState.imageWidth!!,
                        canvasState.imageHeight!!,
                        true
                    )
                onConfirm(scaledBitmap.asImageBitmap())
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(R.string.inpaint_dialog_title))
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .onGloballyPositioned {
                                canvasState = canvasState.copy(
                                    containerWidth = it.size.width,
                                    containerHeight = it.size.height
                                )
                            },
                        contentAlignment = Alignment.Center,

                        ) {
                        backgroundImageBase64?.let {
                            DisplayBase64Image(base64String = backgroundImageBase64)
                        }
                        if (canvasState.canvasWidth != null && canvasState.canvasHeight != null) {
                            Sketchbook(
                                modifier = Modifier
                                    .width((canvasState.canvasWidth!! / density).dp)
                                    .height((canvasState.canvasHeight!! / density).dp),
                                controller = sketchbookController,
                                backgroundColor = Color.Blue,
                                onPathListener = {
                                    SketchbookViewModel.addPath(it)
                                },
                            )
                        }
                    }
                    Row {
                        IconButton(onClick = {
                            sketchbookController.undo()
                            SketchbookViewModel.undo()
                        }) {
                            Icon(undoIcon, contentDescription = "undo")
                        }
                        Box {
                            IconButton(onClick = {
                                isLineWeightDropdownShow = !isLineWeightDropdownShow
                            }) {
                                Icon(lineWeightIcon, contentDescription = "line weight")
                            }
                            DropdownMenu(
                                expanded = isLineWeightDropdownShow,
                                onDismissRequest = { isLineWeightDropdownShow = false }
                            ) {
                                listOf(10, 20, 30, 40, 50, 60, 70).map {
                                    DropdownMenuItem(
                                        text = { Text(it.toString()) },
                                        onClick = {
                                            sketchbookController.setPaintStrokeWidth(it.toFloat())
                                            isLineWeightDropdownShow = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )

}