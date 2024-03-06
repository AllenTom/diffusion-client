package com.allentom.diffusion.ui.screens.home.tabs.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.composables.DrawBar
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel

@Composable
fun TabTools(navController: NavController) {
    val controlNetIcon = ImageVector.vectorResource(id = R.drawable.ic_control_net)
    val controlNetPreprocessIcon =
        ImageVector.vectorResource(id = R.drawable.ic_control_net_preprocess)
    val promptIcon = ImageVector.vectorResource(id = R.drawable.ic_prompt_library)
    val extraImageIcon = ImageVector.vectorResource(id = R.drawable.ic_extra_image)
    val modelIcon = ImageVector.vectorResource(id = R.drawable.ic_model)
    val swapIcon = ImageVector.vectorResource(id = R.drawable.ic_swap)
    val promptStyleIcon = ImageVector.vectorResource(id = R.drawable.ic_style)
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        if (DrawViewModel.enableControlNetFeat) {
            ListItem(
                modifier = Modifier.clickable {
                    navController.navigate(Screens.ControlNetList.route)
                },
                headlineContent = { Text(text = stringResource(id = R.string.tools_control_net)) },
                leadingContent = { Icon(controlNetIcon, contentDescription = "Control net") }
            )
            ListItem(
                modifier = Modifier.clickable {
                    navController.navigate(Screens.ControlNetPreprocess.route)
                },
                headlineContent = { Text(text = stringResource(id = R.string.tools_control_net_preprocess)) },
                leadingContent = {
                    Icon(
                        controlNetPreprocessIcon,
                        contentDescription = "ControlNet Preprocess"
                    )
                }
            )
        }

        ListItem(
            modifier = Modifier.clickable {
                navController.navigate(Screens.ExtraImage.route)
            },
            headlineContent = { Text(text = stringResource(id = R.string.tools_extra_image)) },
            leadingContent = { Icon(extraImageIcon, contentDescription = "Extra image") }
        )
        if (DrawViewModel.enableReactorFeat) {
            ListItem(
                modifier = Modifier.clickable {
                    navController.navigate(Screens.ReactorScreen.route)
                },
                headlineContent = { Text(text = stringResource(id = R.string.reactor)) },
                leadingContent = { Icon(swapIcon, contentDescription = "Reactor") }
            )
        }

        ListItem(
            modifier = Modifier.clickable {
                navController.navigate(Screens.Tagger.route)
            },
            headlineContent = { Text(text = stringResource(id = R.string.tools_caption)) },
            leadingContent = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_caption),
                    contentDescription = "Caption"
                )
            }
        )
        Divider()
        ListItem(
            modifier = Modifier.clickable {
                navController.navigate(Screens.PromptList.route)
            },
            headlineContent = { Text(text = stringResource(id = R.string.tools_prompt_library)) },
            leadingContent = { Icon(promptIcon, contentDescription = "Prompt library") }
        )
        ListItem(
            modifier = Modifier.clickable {
                navController.navigate(Screens.StylesScreen.route)
            },
            headlineContent = { Text(stringResource(R.string.styles_library)) },
            leadingContent = { Icon(promptStyleIcon, contentDescription = "Styles") }
        )
        ListItem(
            modifier = Modifier.clickable {
                navController.navigate(Screens.ModelList.route)
            },
            headlineContent = { Text(text = stringResource(id = R.string.tools_model)) },
            leadingContent = { Icon(modelIcon, contentDescription = "Model") }
        )

        ListItem(
            modifier = Modifier.clickable {
                navController.navigate(Screens.LoraPromptList.route)
            },
            headlineContent = { Text(text = stringResource(id = R.string.tools_lora)) },
            leadingContent = { Icon(modelIcon, contentDescription = "Lora") }
        )
        Divider()
        ListItem(
            modifier = Modifier.clickable {
                navController.navigate(Screens.SettingsScreen.route)
            },
            headlineContent = {
                Text(text = stringResource(id = R.string.settings_screen_title))
            }, leadingContent = {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        DrawBar()
    }
}