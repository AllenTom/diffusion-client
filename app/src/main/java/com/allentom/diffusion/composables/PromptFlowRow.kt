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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.R
import com.allentom.diffusion.store.Prompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.RegionPromptParam

@Composable
fun PromptFlowRow(
    promptList: List<Prompt>,
    onClickPrompt: ((Prompt) -> Unit)? = {},
    useTranslate: Boolean = false,
    regionPromptParam: RegionPromptParam? = null
) {
    if (regionPromptParam != null) {
        Column {
            for (i in 0 until regionPromptParam.getTotalRegionCount()) {
                Text(text = stringResource(R.string.region, i.toString()))
                Spacer(modifier = Modifier.height(4.dp))
                PromptContainer(
                    promptList = promptList.filter { it.regionIndex == i },
                    onClickPrompt = onClickPrompt,
                    useTranslate = useTranslate
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }else{
        PromptContainer(
            promptList = promptList,
            onClickPrompt = onClickPrompt,
            useTranslate = useTranslate
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptContainer(
    promptList: List<Prompt>,
    onClickPrompt: ((Prompt) -> Unit)? = {},
    useTranslate: Boolean = false,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        promptList.forEach {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme.primaryContainer
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .clickable {
                        onClickPrompt?.invoke(it)
                    }
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    if (it.piority != 0) {
                        Text(
                            text = it.piority.toString(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = it.getTranslationText(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = it.text,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }

}