package com.myra.assistant.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> = _aiResponse

    fun isDirectCommand(text: String): Boolean {
        return false
    }

    fun processCommand(command: String) {
        // TODO: Implement command processing
    }
}
