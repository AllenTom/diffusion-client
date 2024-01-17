package com.allentom.diffusion.ui.screens.setting

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.allentom.diffusion.MainActivity
import com.allentom.diffusion.R
import com.allentom.diffusion.store.AppConfigStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen() {
    val context = LocalContext.current
    fun onLogOut(context: Context) {
        AppConfigStore.config = AppConfigStore.config.copy(sdwUrl = "")
        AppConfigStore.saveData(context)
        Toast.makeText(context, context.getString(R.string.logout_success), Toast.LENGTH_SHORT)
            .show()
        (context as MainActivity).finish()
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