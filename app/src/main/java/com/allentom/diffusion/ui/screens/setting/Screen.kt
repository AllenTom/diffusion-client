package com.allentom.diffusion.ui.screens.setting

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.MainActivity
import com.allentom.diffusion.R
import com.allentom.diffusion.api.translate.TranslateHelper
import com.allentom.diffusion.composables.BaiduTranslateOptionsDialog
import com.allentom.diffusion.composables.TextPickUpItem
import com.allentom.diffusion.store.AppConfigStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen() {
    val context = LocalContext.current
    var translateEngine by remember { mutableStateOf(AppConfigStore.config.translateEngine) }
    fun onLogOut(context: Context) {
        AppConfigStore.config = AppConfigStore.config.copy(sdwUrl = "")
        AppConfigStore.saveData(context)
        Toast.makeText(context, context.getString(R.string.logout_success), Toast.LENGTH_SHORT)
            .show()
        (context as MainActivity).finish()
    }

    var isBaiduOptionsDialogShow by remember { mutableStateOf(false) }
    if (isBaiduOptionsDialogShow) {
        BaiduTranslateOptionsDialog(
            initialAppKey = AppConfigStore.config.baiduTranslateAppId,
            initialSecretKey = AppConfigStore.config.baiduTranslateSecretKey,
            onDismiss = {
                isBaiduOptionsDialogShow = false
            },
            onConfirm = { appKey, secretKey ->
                AppConfigStore.updateAndSave(context) {
                    it.copy(baiduTranslateAppId = appKey, baiduTranslateSecretKey = secretKey)
                }
                isBaiduOptionsDialogShow = false
                TranslateHelper.initTranslator()
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_screen_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                TextPickUpItem(
                    label = stringResource(R.string.translate_engine),
                    value = translateEngine,
                    options = listOf("Google", "Baidu"),
                    onValueChange = {
                        AppConfigStore.updateAndSave(context) {config ->
                            config.copy(translateEngine = it)
                        }
                        translateEngine = it
                        TranslateHelper.initTranslator()
                    }
                )
                if (translateEngine == "Baidu") {
                    ListItem(
                        modifier = Modifier.clickable {
                            isBaiduOptionsDialogShow = true
                        },
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.baidu_translate_option),
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.padding(top = 16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onLogOut(context)
                    }) {
                    Text(text = stringResource(R.string.logout))
                }

            }

        }

    }

}