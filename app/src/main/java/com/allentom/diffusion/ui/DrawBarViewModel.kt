package com.allentom.diffusion.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.allentom.diffusion.api.InterrogateRequest
import com.allentom.diffusion.api.getApiClient

object DrawBarViewModel {
    var imageBase64 by mutableStateOf<String?>(null)
    var isCaptioning by mutableStateOf(false)
    var selectedCaption by mutableStateOf<List<String>>(emptyList())
    var useTaggerName by mutableStateOf("deepdanbooru")
    var caption by mutableStateOf<List<String>>(emptyList())

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

}