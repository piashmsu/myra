package com.myra.assistant.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

class MyraOverlayService : Service() {
    companion object {
        fun updateState(context: Context, speaking: Boolean) {
            // TODO: Implement overlay state update
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
