package com.allentom.diffusion.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class ActionItem(
    val text: String,
    val onAction: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomActionSheet(
    items: List<ActionItem>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = {
        onDismiss()
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            items.forEach { item ->
                ListItem(
                    headlineContent = {
                        Text(text = item.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            item.onAction()
                            onDismiss()
                        }
                )
            }
        }
    }
}
