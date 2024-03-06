package com.allentom.diffusion.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.extension.thenIf
import com.allentom.diffusion.store.prompt.Prompt


@Composable
fun PromptChip(
    prompt: Prompt,
    onClickPrompt: ((Prompt) -> Unit)? = {},
    selected: Boolean = false,
    tail: (@Composable () -> Unit)? = null,
    onlyShowTranslation: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))

            .thenIf(selected, Modifier.background(MaterialTheme.colorScheme.primaryContainer))
            .thenIf(
                !selected,
                Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
            )
            .clickable {
                onClickPrompt?.invoke(prompt)
            }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box {
                if (prompt.piority != 0) {
                    Text(text = prompt.piority.toString(), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (onlyShowTranslation) {
                    Text(text = prompt.getTranslationText())
                } else {
                    if (prompt.getTranslationText() != prompt.text) {
                        Text(
                            text = prompt.getTranslationText(),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    } else {
                        Text(
                            "No translation",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }

                    Text(text = prompt.text)
                }
            }
            tail?.invoke()
        }
    }
}