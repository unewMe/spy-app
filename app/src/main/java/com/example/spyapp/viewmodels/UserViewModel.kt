package com.example.spyapp.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object UserSession {
    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email

    fun setEmail(new: String) {
        _email.value = new
    }
}
