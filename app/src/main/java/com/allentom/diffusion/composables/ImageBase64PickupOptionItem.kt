package com.allentom.diffusion.composables

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R
import com.allentom.diffusion.ui.screens.home.tabs.draw.DisplayBase64Image

@Composable
fun ImageBase64PickupOptionItem(
    label: String,
    value: String?,
    onValueChange: (fileUri:Uri,imgBase64:String,filename:String,width:Int,height:Int) -> Unit = {
        _,_,_,_,_ ->
    }
) {

    val context = LocalContext.current
    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                val imageBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                val width = bitmap.width
                val height = bitmap.height
                if (imageBase64 != null) {
                    onValueChange(it,imageBase64,it.lastPathSegment.toString(),width,height)
                }
                // Use the base64 string
            }
        }


    fun pickImageFromGalleryAndConvertToBase64() {
        pickImageLauncher.launch("image/*")
    }
    ListItem(
        modifier = Modifier.clickable {
            pickImageFromGalleryAndConvertToBase64()
        },
        headlineContent = { Text(text = label) },
        trailingContent = {
            if (value != null) {
                Box(
                    Modifier
                        .height(120.dp)
                        .width(120.dp)
                ) {
                    DisplayBase64Image(base64String = value)
                }
            }
        },
        supportingContent = {
            if (value == null) {
                Text(text = stringResource(id = R.string.click_to_pick_image))
            }
        }
    )
}