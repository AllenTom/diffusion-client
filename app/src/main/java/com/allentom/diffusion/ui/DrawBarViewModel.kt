package com.allentom.diffusion.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.allentom.diffusion.api.InterrogateRequest
import com.allentom.diffusion.api.OptionsRequestBody
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

object DrawBarViewModel {
    var imageBase64 by mutableStateOf<String?>(null)
    var isCaptioning by mutableStateOf(false)
    var selectedCaption by mutableStateOf<List<String>>(emptyList())
    var useTaggerName by mutableStateOf("deepdanbooru")
    var danbooruCaptionThreshold by mutableStateOf(0.5f)
    var caption by mutableStateOf<List<String>>(emptyList())

    suspend fun onCaption() {
        val image = imageBase64 ?: return
        val currentOption = DrawViewModel.options?.interrogateDeepbooruScoreThreshold ?: 0.5f
        if (useTaggerName == "deepdanbooru" && danbooruCaptionThreshold != currentOption) {
            // change the threshold
            getApiClient().setOptions(OptionsRequestBody(
                interrogateDeepbooruScoreThreshold = danbooruCaptionThreshold
            ))
            val newOptions = getApiClient().getOptions()
            DrawViewModel.options = newOptions.body()
        }
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

}