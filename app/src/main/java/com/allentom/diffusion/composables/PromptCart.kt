package com.allentom.diffusion.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptCart() {
    var selectedTabIndex by remember {
        mutableStateOf(0)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            FilterChip(
                selected = selectedTabIndex == 0,
                onClick = {
                    selectedTabIndex = 0
                },
                label = {
                    Text(text = "Prompt")
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = selectedTabIndex == 1,
                onClick = {
                    selectedTabIndex = 1
                },
                label = {
                    Text(text = "Negative prompt")
                }
            )
        }
        LazyColumn {
            item {
                when(selectedTabIndex){
                    0 -> DrawViewModel.baseParam.promptText.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                        ) {
                            Row {
                                Text(text = prompt.text, modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(onClick = { DrawViewModel.removeInputPrompt(prompt.text) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            }
                        }
                    }
                    1 -> DrawViewModel.baseParam.negativePromptText.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                        ) {
                            Row {
                                Text(text = prompt.text, modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(onClick = { DrawViewModel.removeInputNegativePrompt(prompt.text) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}