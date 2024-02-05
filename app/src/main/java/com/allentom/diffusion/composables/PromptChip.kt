package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.store.prompt.Prompt

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PromptChip(
    prompt: Prompt,
    onClickPrompt: ((Prompt) -> Unit)? = {},
    selected: Boolean = false,
    tail: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        leadingIcon = {
            if (prompt.piority != 0) {
                Text(text = prompt.piority.toString())
            }
        },
        onClick = {
            onClickPrompt?.invoke(prompt)
        },
        label = {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = prompt.getTranslationText(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(text = prompt.text)
            }
        },
        trailingIcon = {
            tail?.invoke()
        }
    )
}