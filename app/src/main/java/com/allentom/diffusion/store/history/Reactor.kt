package com.allentom.diffusion.store.history

import com.allentom.diffusion.ui.screens.home.tabs.draw.ReactorParam

fun HistoryWithRelation.toReactorParam(): ReactorParam {
    return ReactorParam(
        enabled = historyEntity.reactorEnabled ?: false,
        singleImageResult = historyEntity.reactorSingleImagePath,
        singleImageResultFilename = historyEntity.reactorSingleImageResultFilename,
        codeFormerWeightFidelity = historyEntity.reactorCodeFormerWeightFidelity ?: 0.5f,
        genderDetectionSource = historyEntity.reactorGenderDetectionSource ?: 0,
        genderDetectionTarget = historyEntity.reactorGenderDetectionTarget ?: 0,
        restoreFace = historyEntity.reactorRestoreFace ?: "None",
        restoreFaceVisibility = historyEntity.reactorRestoreFaceVisibility ?: 1f,
        postprocessingOrder = historyEntity.reactorPostprocessingOrder ?: true,
        scaleBy = historyEntity.reactorScaleBy ?: 1f,
        upscaler = historyEntity.reactorUpscaler ?: "None",
        upscalerVisibility = historyEntity.reactorUpscalerVisibility ?: 1f,
    )
}