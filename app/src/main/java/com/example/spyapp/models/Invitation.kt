package com.example.spyapp.models

data class Invitation(
    val id: String = "",
    val fromUserId: String = "",      // nadawca
    val fromEmail: String = "",
    val toEmail: String = "",         // adres e-mail odbiorcy
    val timestamp: Long = 0L
)
