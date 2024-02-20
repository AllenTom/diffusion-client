package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.R
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.RegionPromptParam

@Composable
fun PromptFlowRow(
    promptList: List<Prompt>,
    onClickPrompt: ((Prompt) -> Unit)? = {},
    regionPromptParam: RegionPromptParam? = null
) {
    if (regionPromptParam != null && regionPromptParam.enable) {
        Column {
            for (i in 0 until regionPromptParam.getTotalRegionCount()) {
                if (regionPromptParam.useCommon && i == 0) {
                    Text(text = stringResource(R.string.common_region))
                } else {
                    Text(text = stringResource(R.string.region, i.toString()))
                }
                Spacer(modifier = Modifier.height(4.dp))
                PromptContainer(
                    promptList = promptList.filter { it.regionIndex == i },
                    onClickPrompt = onClickPrompt,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    } else {
        PromptContainer(
            promptList = promptList,
            onClickPrompt = onClickPrompt,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptContainer(
    promptList: List<Prompt>,
    onlyTranslate: Boolean = false,
    closeable: Boolean = false,
    onClosed: ((Prompt) -> Unit)? = {},
    onClickPrompt: ((Prompt) -> Unit)? = {},
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        promptList.forEach {
            SmallPrompt(
                prompt = it,
                onClickPrompt = onClickPrompt,
                onlyTranslate = onlyTranslate,
                closeable = closeable,
                onClosed = onClosed
            )
        }
    }

}