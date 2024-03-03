package com.allentom.diffusion.service

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
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
        DrawViewModel.runningTask?.queue?.forEach {
            it.genScope = serviceScope
        }
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

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, notification)

        serviceScope.launch(Dispatchers.IO) {
                while (true) {
                    val runner = DrawViewModel.runningTask ?: break
                    val nextTask = runner.getNextUnRunTask() ?: break
                    val unRunIndex = runner.queue.indexOf(nextTask)
                    runner.currentIndex = unRunIndex
                    if (DrawViewModel.currentGenTaskId == null || DrawViewModel.pinRunningTask) {
                        DrawViewModel.currentGenTaskId = nextTask.id
                    }
                    nextTask.genScope = serviceScope
                    nextTask.generateImage()
                }

            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}