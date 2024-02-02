package com.allentom.diffusion.ui.screens.extra

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.allentom.diffusion.api.ExtraImageRequest
import com.allentom.diffusion.api.entity.Upscale
import com.allentom.diffusion.store.AppConfigStore
import java.io.Serializable

//{
//    "resize_mode": 0,
//    "show_extras_results": true,
//    "gfpgan_visibility": 0,
//    "codeformer_visibility": 0,
//    "codeformer_weight": 0,
//    "upscaling_resize": 2,
//    "upscaling_resize_w": 512,
//    "upscaling_resize_h": 512,
//    "upscaling_crop": true,
//    "upscaler_1": "None",
//    "upscaler_2": "None",
//    "extras_upscaler_2_visibility": 0,
//    "upscale_first": false,
//    "image": ""
//}
data class InputImageItem(
    val uri: Uri,
    val name: String,
    val resultImage: String? = null,
    val isGenerating: Boolean = false,
    val isExport: Boolean = false,
)
data class ExtraImageParam(
    val resizeMode: Int = 0,
    val showExtrasResults: Boolean = true,
    val gfpganVisibility: Float = 0f,
    val codeformerVisibility: Float = 0f,
    val codeformerWeight: Float = 0f,
    val upscalingResize: Float = 2f,
    val upscalingResizeW: Int = 512,
    val upscalingResizeH: Int = 512,
    val upscalingCrop: Boolean = true,
    val upscaler1: String = "None",
    val upscaler2: String = "None",
    val extrasUpscaler2Visibility: Int = 0,
    val upscaleFirst: Boolean = false,
    val image: String? = null,
) : Serializable {
    fun toExtraImageRequestBody(): ExtraImageRequest {
        assert(image != null)
        return ExtraImageRequest(
            resizeMode = resizeMode,
            showExtrasResults = showExtrasResults,
            gfpganVisibility = gfpganVisibility,
            codeformerVisibility = codeformerVisibility,
            codeformerWeight = codeformerWeight,
            upscalingResize = upscalingResize,
            upscalingResizeW = upscalingResizeW,
            upscalingResizeH = upscalingResizeH,
            upscalingCrop = upscalingCrop,
            upscaler1 = upscaler1,
            upscaler2 = upscaler2,
            extrasUpscaler2Visibility = extrasUpscaler2Visibility,
            upscaleFirst = upscaleFirst,
            image = image!!
        )
    }
}

object ExtraViewModel : ViewModel() {
    var extraParam by mutableStateOf(ExtraImageParam())
    var imageName by mutableStateOf(null as String?)
    var upscalerList by mutableStateOf<List<Upscale>>(emptyList())
    var isProcessing by mutableStateOf(false)
    var inputImages by mutableStateOf(emptyList<InputImageItem>())
    var stopFlag by mutableStateOf(false)
    fun saveToCache(context: Context) {
        AppConfigStore.config = AppConfigStore.config.copy(extraImageHistory = extraParam.copy(image = null))
        AppConfigStore.saveData(context)
    }

    fun onStartGenerating(onlyIndex: Int? = null) {
        inputImages = inputImages.mapIndexed { index, inputImageItem ->
            if (onlyIndex != null) {
                if (index != onlyIndex) {
                    return@mapIndexed inputImageItem
                }
                return@mapIndexed inputImageItem.copy(isGenerating = true, resultImage = null)
            }
            inputImageItem.copy(isGenerating = true, resultImage = null)
        }
    }
}
