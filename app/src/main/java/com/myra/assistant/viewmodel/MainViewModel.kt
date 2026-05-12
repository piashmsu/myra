package com.myra.assistant.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> = _aiResponse

    fun isDirectCommand(text: String): Boolean {
        val lower = text.lowercase().trim()
        val prefixes = listOf(
            "open app", "open ", "launch ", "start ",
            "call ", "phone ", "dial ",
            "whatsapp msg", "whatsapp message", "msg ", "message ",
            "whatsapp call", "video call",
            "youtube play", "youtube ", "play ",
            "spotify play", "spotify ",
            "flashlight on", "flashlight off", "torch on", "torch off",
            "volume up", "volume down", "louder", "quieter",
            "sleep mode", "so jao", "band karo", "stop"
        )
        return prefixes.any { lower.contains(it) }
    }

    fun processCommand(command: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = executeCommand(command)
            withContext(Dispatchers.Main) {
                _aiResponse.value = response
            }
        }
    }

    private fun executeCommand(command: String): String {
        val lower = command.lowercase().trim()

        return when {
            // Flashlight
            lower.contains("flashlight on") || lower.contains("torch on") -> {
                "Flashlight on kar di hai."
            }
            lower.contains("flashlight off") || lower.contains("torch off") -> {
                "Flashlight band kar di hai."
            }

            // Volume
            lower.contains("volume up") || lower.contains("louder") -> {
                "Volume badha di hai."
            }
            lower.contains("volume down") || lower.contains("quieter") -> {
                "Volume kam kar diya."
            }

            // Sleep/Stop
            lower.contains("sleep mode") || lower.contains("so jao") || lower.contains("band karo") -> {
                "Theek hai, so rahi hoon."
            }

            // Open App
            lower.contains("open app") || lower.contains("open ") || lower.contains("launch ") -> {
                val appName = extractAfterKeyword(lower, listOf("open app", "open ", "launch "))
                if (appName.isNotEmpty()) "Opening $appName" else "Konsa app open karna hai?"
            }

            // YouTube
            lower.contains("youtube play") || lower.contains("youtube ") -> {
                val query = extractAfterKeyword(lower, listOf("youtube play", "youtube ", "play "))
                if (query.isNotEmpty()) "YouTube pe '$query' play kar rahi hoon."
                else "YouTube pe kya play karna hai?"
            }

            // Spotify
            lower.contains("spotify play") || lower.contains("spotify ") -> {
                val query = extractAfterKeyword(lower, listOf("spotify play", "spotify ", "play "))
                if (query.isNotEmpty()) "Spotify pe '$query' play kar rahi hoon."
                else "Spotify pe kya play karna hai?"
            }

            // WhatsApp Message
            lower.contains("whatsapp msg") || lower.contains("whatsapp message") ||
            lower.contains("msg ") || lower.contains("message ") -> {
                val (name, msg) = extractNameAndMessage(command, listOf("whatsapp msg", "whatsapp message", "msg", "message"))
                if (name.isNotEmpty() && msg.isNotEmpty()) {
                    "$name ko WhatsApp message bhej rahi hoon: $msg"
                } else if (name.isNotEmpty()) {
                    "$name ko kya message bhejna hai?"
                } else {
                    "Kisko message bhejna hai?"
                }
            }

            // WhatsApp Call
            lower.contains("whatsapp call") || lower.contains("video call") -> {
                val name = extractAfterKeyword(lower, listOf("whatsapp call", "video call"))
                if (name.isNotEmpty()) "$name ko WhatsApp call kar rahi hoon."
                else "Kisko WhatsApp call karna hai?"
            }

            // Normal Call
            lower.contains("call ") || lower.contains("phone ") || lower.contains("dial ") -> {
                val name = extractAfterKeyword(lower, listOf("call", "phone", "dial"))
                if (name.isNotEmpty()) "$name ko call kar rahi hoon."
                else "Kisko call karna hai?"
            }

            // SMS
            lower.contains("sms ") -> {
                val (name, msg) = extractNameAndMessage(command, listOf("sms"))
                if (name.isNotEmpty() && msg.isNotEmpty()) {
                    "$name ko SMS bhej rahi hoon: $msg"
                } else {
                    "Kisko aur kya SMS bhejna hai?"
                }
            }

            // Default response for unknown
            else -> {
                generateFallbackResponse(command)
            }
        }
    }

    private fun extractAfterKeyword(text: String, keywords: List<String>): String {
        val lower = text.lowercase()
        for (kw in keywords) {
            val idx = lower.indexOf(kw)
            if (idx != -1) {
                val after = text.substring(idx + kw.length).trim()
                if (after.isNotEmpty()) return after
            }
        }
        return ""
    }

    private fun extractNameAndMessage(text: String, keywords: List<String>): Pair<String, String> {
        val lower = text.lowercase()
        for (kw in keywords) {
            val idx = lower.indexOf(kw)
            if (idx != -1) {
                val after = text.substring(idx + kw.length).trim()
                // Try to split by first space to get name, rest is message
                val parts = after.split(" ", limit = 2)
                val name = parts.getOrNull(0)?.trim() ?: ""
                val msg = parts.getOrNull(1)?.trim() ?: ""
                return Pair(name, msg)
            }
        }
        return Pair("", "")
    }

    private fun generateFallbackResponse(input: String): String {
        val responses = listOf(
            "Samajh gayi! '$input' ke liye main abhi setup kar rahi hoon.",
            "Theek hai, '$input' ke liye kaam kar rahi hoon.",
            "'$input' note kar liya hai.",
            "Ji, main samajh gayi. '$input' process kar rahi hoon."
        )
        return responses.random()
    }
}
