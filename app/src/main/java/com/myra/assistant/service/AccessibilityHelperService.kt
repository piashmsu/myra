package com.myra.assistant.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent

class AccessibilityHelperService : AccessibilityService() {
    companion object {
        var instance: AccessibilityHelperService? = null

        fun isEnabled(context: Context): Boolean {
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
