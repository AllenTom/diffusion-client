package com.allentom.diffusion

sealed class Screens(val route: String) {
    object Login : Screens("login_screen")
    object Home : Screens("home_screen")
    object History : Screens("history_screen")
    object ImageDetail : Screens("image_detail_screen/{id}")
    object Tagger : Screens("tagger_screen")
    object ControlNetList : Screens("control_net_list_screen")
    object ControlNetPreprocess : Screens("control_net_preprocess_screen")
    object PromptList : Screens("prompt_list_screen")
    object DrawMask : Screens("draw_mask_screen")
    object ExtraImage : Screens("extra_image_screen")

    object PromptDetail : Screens("prompt_detail_screen/{promptId}")
    object PromptSearch : Screens("prompt_search_screen")
    object PromptCategory : Screens("prompt_category_screen/{promptName}")
    object HistoryDetail : Screens("history_detail_screen/{id}")

    object ModelList : Screens("model_list_screen")
    object CivitaiImageList : Screens("civitai_image_list_screen")

    object CivitaiImageDetail : Screens("civitai_image_detail_screen/{id}")

    object LoraPromptList : Screens("lora_prompt_list_screen")
    object LoraPromptDetail : Screens("lora_prompt_detail_screen/{id}")

    object CivitaiModelImageScreen : Screens("civitai_model_image_screen")

    object ModelDetailScreen : Screens("model_detail_screen/{modelId}")
    object SettingsScreen : Screens("settings_screen")
}