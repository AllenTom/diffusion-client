package com.allentom.diffusion.ui.screens.home.tabs.draw.panels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.R
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamsModalBottomSheet(
    onDismissRequest: () -> Unit,
    onSwitchModel: (String) -> Unit,
    onSwitchVae: (String) -> Unit,
    state: SheetState? = null
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = state ?: rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        ParamsPanel(
            onSwitchModel = onSwitchModel,
            onSwitchVae = onSwitchVae
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamsPanel(
    onSwitchModel: (String) -> Unit,
    onSwitchVae: (String) -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    Column {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            LazyRow {
                item {
                    FilterChip(onClick = {
                        tabIndex = 0
                    }, label = {
                        Text(stringResource(id = R.string.param_base))
                    }, selected = tabIndex == 0)
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(onClick = {
                        tabIndex = 1
                    }, label = {
                        Text(stringResource(id = R.string.param_hires_fix))
                    }, selected = tabIndex == 1)
                    if (DrawViewModel.enableControlNetFeat) {
                        Spacer(modifier = Modifier.width(16.dp))
                        FilterChip(onClick = {
                            tabIndex = 2
                        }, label = {
                            Text(stringResource(id = R.string.param_control_net))
                        }, selected = tabIndex == 2)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(onClick = {
                        tabIndex = 3
                    }, label = {
                        Text(stringResource(id = R.string.param_img2Img))
                    }, selected = tabIndex == 3)
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(onClick = {
                        tabIndex = 4
                    }, label = {
                        Text(stringResource(R.string.reactor))
                    }, selected = tabIndex == 4)
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(onClick = {
                        tabIndex = 5
                    }, label = {
                        Text(stringResource(R.string.adetailer))
                    }, selected = tabIndex == 5)
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(onClick = {
                        tabIndex = 6
                    }, label = {
                        Text("X/Y/Z")
                    }, selected = tabIndex == 6)
                }
            }
        }
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        when (tabIndex) {
            0 -> {
                BaseInfoPanel(onSwitchModel, onSwitchVae)
            }

            1 -> {
                HiresFixPanel()
            }
            2 -> {
                ControlNetPanel(
                    onValueChange = {
                        DrawViewModel.inputControlNetParams = it
                    },
                    controlNetParam = DrawViewModel.inputControlNetParams
                )
            }
            3 -> {
                Img2ImgPanel()
            }
            4 -> {
                ReactorPanel(
                    onValueChange = {
                        DrawViewModel.reactorParam = it
                    },
                    reactorParam = DrawViewModel.reactorParam,
                    showEnableOption = true
                )
            }
            5 -> {
                AdetailerPanel(
                    onValueChange = {
                        DrawViewModel.adetailerParam = it
                    },
                    adetailerParam = DrawViewModel.adetailerParam
                )
            }
            6 -> {
                AxisPanel(

                )
            }
        }
    }
}

