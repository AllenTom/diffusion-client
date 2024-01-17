package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.store.Prompt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptFlowRow(
    promptList:List<Prompt>,
    onClickPrompt: ((Prompt) -> Unit)? = {},
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
                Row {
                    if (it.piority != 0) {
                        Text(text = it.piority.toString(),color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(text = it.text, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }

}