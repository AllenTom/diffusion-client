package com.allentom.diffusion.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.allentom.diffusion.R
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

class PromptActionState {
    var isActionBottomSheetOpen by mutableStateOf(false)
    var contextPrompt by mutableStateOf<List<Prompt>>(emptyList())
    var target by mutableStateOf("prompt")
    fun onOpenActionBottomSheet(prompt: List<Prompt>, target: String) {
        contextPrompt = prompt
        this.target = target
        isActionBottomSheetOpen = true
    }
}

@Composable
fun rememberPromptActionState(): PromptActionState {
    return remember { PromptActionState() }
}

@Composable()
fun PromptAction(
    actionState: PromptActionState
) {
    if (actionState.isActionBottomSheetOpen) {
        BottomActionSheet(items = listOf(
            ActionItem(
                text = stringResource(id = R.string.send_to_prompt),
                onAction = {
                    if (actionState.target == "prompt") {
                        DrawViewModel.inputPromptText = actionState.contextPrompt
                    } else {
                        DrawViewModel.inputNegativePromptText = actionState.contextPrompt
                    }
                    actionState.isActionBottomSheetOpen = false
                }
            ),
            ActionItem(
                text = stringResource(id = R.string.add_to_prompt),
                onAction = {
                    if (actionState.target == "prompt") {
                        actionState.contextPrompt.forEach {
                            DrawViewModel.addInputPrompt(it)
                        }
                    } else {
                        actionState.contextPrompt.forEach {
                            DrawViewModel.addInputNegativePrompt(it)
                        }
                    }
                    actionState.isActionBottomSheetOpen = false
                }
            ),
        )) {
            actionState.isActionBottomSheetOpen = false
        }
    }
}