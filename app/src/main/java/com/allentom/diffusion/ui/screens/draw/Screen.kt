package com.allentom.diffusion.ui.screens.draw

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import io.getstream.sketchbook.Sketchbook
import io.getstream.sketchbook.rememberSketchbookController

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DrawMaskScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Draw") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            DrawOnImageComposable()
        }
    }
}

@Composable
fun DrawOnImageComposable() {
    val sketchbookController = rememberSketchbookController()
    LaunchedEffect(Unit) {
        sketchbookController.setBackgroundColor(Color.Blue)
        sketchbookController.setPaintColor(Color.Red)
    }
    val context = LocalContext.current
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            sketchbookController.setImageBitmap(bitmap.asImageBitmap())
        }
    }
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
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Sketchbook(
                    modifier = Modifier.fillMaxSize(),
                    controller = sketchbookController,
                    backgroundColor = Color.Blue,

                    )
            }
            Button(onClick = {
                pickImageLauncher.launch("image/*")
            }) {
                Text(text = "Pick from gallery")
            }

        }
//        Image(bitmap = imageBitmap, contentDescription = null)

    }
}