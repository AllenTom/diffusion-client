package com.allentom.diffusion.ui.screens.prompt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.R
import com.allentom.diffusion.store.SavePrompt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptActionBottomSheet(
    contextPrompt: SavePrompt?,
    onDismissRequest: () -> Unit,
    onAddToPrompt: (() -> Unit)?,
    onAddToNegativePrompt: (() -> Unit)?
) {
    contextPrompt?.let {
        ModalBottomSheet(onDismissRequest = onDismissRequest) {
            Column {
                onAddToPrompt?.let {
                    ListItem(
                        modifier = Modifier.clickable {
                            onAddToPrompt()
                            onDismissRequest()
                        },
                        headlineContent = {
                        Text(text = stringResource(id = R.string.add_to_prompt))
                    })
                }
                onAddToNegativePrompt?.let {
                    ListItem(
                        modifier = Modifier.clickable {
                            onAddToNegativePrompt()
                            onDismissRequest()
                        },
                        headlineContent = {
                        Text(text = stringResource(R.string.add_to_negative_prompt))
                    })
                }
            }
        }
    }
}