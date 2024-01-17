package com.allentom.diffusion.service

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GenerateImageService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "GenerateImageServiceChannel",
                "Generate Image Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val context = this
        DrawViewModel.genScope = serviceScope
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, "GenerateImageServiceChannel")
                .setContentTitle("Generate Image Service")
                .setSmallIcon(R.drawable.ic_dialog_info)
                .build()
        } else {
            NotificationCompat.Builder(this)
                .setContentTitle("Generate Image Service")
                .setSmallIcon(R.drawable.ic_dialog_info)
                .build()
        }

        startForeground(1, notification)
        serviceScope.launch(Dispatchers.IO) {
            val refreshIndex = intent?.getIntExtra("refreshIndex", -1)?.let {
                if (it == -1) {
                    null
                } else {
                    it
                }
            }

            DrawViewModel.generateImage(context, refreshIndex = refreshIndex)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}