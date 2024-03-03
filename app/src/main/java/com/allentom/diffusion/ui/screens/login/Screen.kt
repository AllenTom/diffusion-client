package com.allentom.diffusion.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.allentom.diffusion.R
import com.allentom.diffusion.Screens
import com.allentom.diffusion.api.ApiHelper
import com.allentom.diffusion.api.entity.SDWError
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Credentials

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var urlInput by remember { mutableStateOf("") }
    var urlList by remember { mutableStateOf(emptyList<String>()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    var optionModalOpen by remember { mutableStateOf(false) }
    var disableSSLVerification by remember { mutableStateOf(AppConfigStore.config.disbaleSSLVerification) }
    var enableAuth by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun completeUrl(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "http://$url"
        }
    }

    suspend fun onGoToUrl(url: String) {
        isLoading = true
        val apiUrl = completeUrl(url)
        var auth: String? = null
        AppConfigStore.config.saveAuths.get(apiUrl)?.let {
            auth = it
        }
        if (enableAuth) {
            auth = Credentials.basic(username, password)
        }
        ApiHelper.createTestInstance(apiUrl, auth)
        try {
            val options = getApiClient().getOptions()
            if (!options.isSuccessful) {
                options.errorBody()?.let {
                    val gson = Gson()
                    val error = gson.fromJson(it.string(), SDWError::class.java)
                    throw Exception(error.detail)
                }
                throw Exception("Connection error")
            }

            AppConfigStore.config = AppConfigStore.config.copy(
                sdwUrl = apiUrl,
                saveUrls = AppConfigStore.config.saveUrls.toMutableList().filter { existUrl ->
                    existUrl != apiUrl
                }.toMutableList().apply {
                    add(0, apiUrl)
                },
                saveAuths = AppConfigStore.config.saveAuths.toMutableMap().apply {
                    auth?.let {
                        put(apiUrl, it)
                    }
                }
            )
            AppConfigStore.saveData(context)
            ApiHelper.createInstance(apiUrl, auth)
            scope.launch(Dispatchers.IO) {
                DrawViewModel.initViewModel(context)
                scope.launch(Dispatchers.Main) {
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Login.route) {
                            inclusive = true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
            snackbarHostState.showSnackbar(
                message ="Error: " + e.message.toString(),
            )
            return
        } finally {

        }

    }
    LaunchedEffect(Unit) {
        urlList = AppConfigStore.config.saveUrls
        val lastUrl = AppConfigStore.config.sdwUrl
        if (lastUrl != null && lastUrl != "") {
            onGoToUrl(lastUrl)
        } else {
            isLoading = false
        }
    }

    if (optionModalOpen) {
        ModalBottomSheet(onDismissRequest = {
            optionModalOpen = false
        }) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.options),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.disable_ssl_verification),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = disableSSLVerification,
                        onCheckedChange = {
                            AppConfigStore.updateAndSave(context) {
                                disableSSLVerification = it.disbaleSSLVerification.not()
                                it.copy(disbaleSSLVerification = it.disbaleSSLVerification.not())
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.enable_auth),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = enableAuth,
                        onCheckedChange = {
                            enableAuth = it
                        }
                    )
                }
            }
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            Text(text = "Diffusion", fontSize = 36.sp, color = MaterialTheme.colorScheme.primary)
            if (isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.loading), fontSize = 18.sp)
                }
            } else {
                Column(
                    Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text(stringResource(R.string.login_url)) },
                        trailingIcon = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                IconButton(onClick = {
                                    optionModalOpen = true
                                }) {
                                    Icon(Icons.Filled.Settings, contentDescription = null)
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        onGoToUrl(urlInput)
                                    }
                                }) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                }
                            }

                        },
                    )
                    if (enableAuth) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = username,
                            onValueChange = { username = it },
                            label = { Text(stringResource(R.string.username)) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(R.string.password)) },
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.login_history_url),
                        fontWeight = FontWeight.W400
                    )
                    LazyColumn {
                        items(urlList.size) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = urlList[it],
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            onGoToUrl(urlList[it])
                                        }
                                    },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

    }
}