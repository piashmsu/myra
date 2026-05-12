package com.myra.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.myra.assistant.R

/**
 * FIXED MyraOverlayService — Foreground service with proper notification
 * Shows a persistent notification so Android doesn't kill it.
 */
class MyraOverlayService : Service() {

    companion object {
        private const val TAG = "MYRA_OVERLAY"
        private const val NOTIF_ID = 1001
        private const val CHANNEL_ID = "myra_overlay_channel"

        fun updateState(context: Context, speaking: Boolean) {
            // TODO: Implement overlay state update (visual indicator)
            Log.d(TAG, "Overlay state: speaking=$speaking")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MyraOverlayService created")
        startForegroundCompat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure foreground notification is active
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, buildNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        }
        return START_STICKY
    }

    private fun startForegroundCompat() {
        createNotificationChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MYRA Voice Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps MYRA voice assistant running in background"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MYRA Active")
            .setContentText("Voice assistant is running...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "MyraOverlayService destroyed")
        super.onDestroy()
    }
}
