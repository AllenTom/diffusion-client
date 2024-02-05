package com.allentom.diffusion.composables

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allentom.diffusion.store.prompt.Prompt

@Composable
fun SmallPrompt(
    prompt: Prompt,
    onClickPrompt: ((Prompt) -> Unit)? = {},
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable {
                onClickPrompt?.invoke(prompt)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (prompt.piority != 0) {
                Text(
                    text = prompt.piority.toString(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column {
                Text(
                    text = prompt.getTranslationText(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Text(
                    text = prompt.text,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}