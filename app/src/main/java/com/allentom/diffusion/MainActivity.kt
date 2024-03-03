package com.allentom.diffusion

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.allentom.diffusion.api.civitai.CivitaiApiHelper
import com.allentom.diffusion.api.getApiClient
import com.allentom.diffusion.modifier.registerModifier
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.ControlNetStore
import com.allentom.diffusion.ui.screens.civitai.CivitaiModelImageScreen
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImageDetailScreen
import com.allentom.diffusion.ui.screens.civitai.images.CivitaiImagesScreen
import com.allentom.diffusion.ui.screens.controlnet.ControlNetPreprocessScreen
import com.allentom.diffusion.ui.screens.controlnet.ControlNetScreen
import com.allentom.diffusion.ui.screens.draw.DrawMaskScreen
import com.allentom.diffusion.ui.screens.extra.ExtraScreen
import com.allentom.diffusion.ui.screens.history.HistoryListView
import com.allentom.diffusion.ui.screens.historydetail.HistoryDetailScreen
import com.allentom.diffusion.ui.screens.home.HomePage
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.ModifierLibrary
import com.allentom.diffusion.ui.screens.imagedetail.ImageDetail
import com.allentom.diffusion.ui.screens.login.LoginScreen
import com.allentom.diffusion.ui.screens.lora.detail.LoraDetailScreen
import com.allentom.diffusion.ui.screens.lora.list.LoraListScreen
import com.allentom.diffusion.ui.screens.model.detail.ModelDetailScreen
import com.allentom.diffusion.ui.screens.model.list.ModelListScreen
import com.allentom.diffusion.ui.screens.prompt.PromptCategoryScreen
import com.allentom.diffusion.ui.screens.prompt.PromptScreen
import com.allentom.diffusion.ui.screens.prompt.PromptSearchScreen
import com.allentom.diffusion.ui.screens.promptdetail.PromptDetailScreen
import com.allentom.diffusion.ui.screens.reactor.ReactorScreen
import com.allentom.diffusion.ui.screens.setting.SettingScreen
import com.allentom.diffusion.ui.screens.style.StyleScreen
import com.allentom.diffusion.ui.screens.tagger.TaggerScreen
import com.allentom.diffusion.ui.theme.DiffusionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MainActivity : ComponentActivity() {

    fun handleSSLHandshake() {
        try {
            var trustAllCerts:Array<TrustManager> = arrayOf<TrustManager>(object: X509TrustManager {
                override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {

                }

                override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {

                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    val arrayOfNulls = arrayOfNulls<X509Certificate?>(0)
                    return arrayOfNulls
                }

            })

            val sc: SSLContext = SSLContext.getInstance("TLS")
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())

            HttpsURLConnection.setDefaultHostnameVerifier(object : HostnameVerifier {
                override fun verify(p0: String?, p1: SSLSession?): Boolean {
                    return true
                }

            })

        }catch (e:Exception){

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleSSLHandshake()
        AppConfigStore.refresh(this)
        ConstValues.initValues(this)
        setContent {
            DiffusionTheme {
                DiffusionApp()
            }
        }

        ControlNetStore.refresh(this)
        DrawViewModel.init(this)
        val scope = lifecycleScope
        if (ContextCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.requestPermissions(arrayOf(POST_NOTIFICATIONS), 1)
            }
        }
        CivitaiApiHelper.createInstance(
            "https://civitai.com/"
        )
        ImageCacheHelper.initImageCache(this)

        // create folder
        scope.launch(Dispatchers.IO) {
            val sdCardRoot = Environment.getExternalStorageDirectory()
            val appPath = sdCardRoot.absolutePath + "/Diffusion"
            val appPathFile = File(appPath)
            if (!appPathFile.exists()) {
                appPathFile.mkdir()
            }
            val controlNetPath = sdCardRoot.absolutePath + "/Diffusion/ControlNet"
            val controlNetPathFile = File(controlNetPath)
            if (!controlNetPathFile.exists()) {
                controlNetPathFile.mkdir()
            }
        }
        ModifierLibrary.init(this)
        registerModifier(this)
    }
}

@Composable
fun DiffusionApp() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    if (DrawViewModel.isGenerating) {
        LaunchedEffect(Unit) {
            while (true) {
                if (!DrawViewModel.isGenerating) {
                    return@LaunchedEffect
                }
                DrawViewModel.progress = getApiClient().getProgress().body()
                val progress = DrawViewModel.progress
                if (progress !== null) {
                    DrawViewModel.runningTask?.run {
                        updateCurrentTask {
                            it.updateGenItemByIndex(it.currentGenIndex) {
                                it.copy(progress = progress)
                            }
                            it
                        }
                    }
                }
                delay(500)
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = Screens.Login.route,

        ) {
        composable(Screens.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screens.Home.route) {
            HomePage(navController = navController)
        }
        composable(Screens.History.route) {
            HistoryListView(navController = navController)
        }
        composable(Screens.ImageDetail.route) {
            val id = backStackEntry.value?.arguments?.getString("id")
            ImageDetail(id = id ?: "", navController = navController)
        }
        composable(Screens.Tagger.route) {
            TaggerScreen()
        }
        composable(Screens.ControlNetList.route) {
            ControlNetScreen(navController)
        }
        composable(Screens.PromptList.route) {
            PromptScreen(navController)
        }
        composable(Screens.DrawMask.route) {
            DrawMaskScreen()
        }
        composable(Screens.ExtraImage.route) {
            ExtraScreen()
        }
        composable(Screens.PromptDetail.route) {
            val id = backStackEntry.value?.arguments?.getString("promptId")?.toLong()
            PromptDetailScreen(id = id ?: 0)
        }
        composable(Screens.PromptSearch.route) {
            PromptSearchScreen(navController)
        }
        composable(Screens.PromptCategory.route) {
            val promptName = backStackEntry.value?.arguments?.getString("promptName")
            PromptCategoryScreen(navController, promptName ?: "")
        }
        composable(Screens.HistoryDetail.route) {
            val id = backStackEntry.value?.arguments?.getString("id")?.toLong()
            HistoryDetailScreen(navController, historyId = id ?: 0)
        }
        composable(Screens.ControlNetPreprocess.route) {
            ControlNetPreprocessScreen()
        }
        composable(Screens.ModelList.route) {
            ModelListScreen(navController)
        }

        composable(Screens.CivitaiImageList.route) {
            CivitaiImagesScreen(navController)
        }
        composable(Screens.CivitaiImageDetail.route) {
            val id = backStackEntry.value?.arguments?.getString("id")?.toLong()
            CivitaiImageDetailScreen(id ?: 0)
        }
        composable(Screens.LoraPromptList.route) {
            LoraListScreen(navController)
        }
        composable(Screens.LoraPromptDetail.route) {
            val id = backStackEntry.value?.arguments?.getString("id")?.toLong()
            LoraDetailScreen(navController, id ?: 0)
        }
        composable(Screens.CivitaiModelImageScreen.route) {
            CivitaiModelImageScreen()
        }
        composable(Screens.ModelDetailScreen.route) {
            val id = backStackEntry.value?.arguments?.getString("modelId")?.toLong()
            ModelDetailScreen(navController, id ?: 0)
        }
        composable(Screens.SettingsScreen.route) {
            SettingScreen()
        }
        composable(Screens.ReactorScreen.route) {
            ReactorScreen()
        }
        composable(Screens.StylesScreen.route) {
            StyleScreen()
        }
    }
}
