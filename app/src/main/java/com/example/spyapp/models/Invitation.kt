package com.example.spyapp.models

data class Invitation(
    val id: String = "",
    val fromUserId: String = "",      
    val fromEmail: String = "",
    val toEmail: String = "",         
    val timestamp: Long = 0L
)
