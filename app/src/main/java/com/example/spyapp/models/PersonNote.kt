package com.example.spyapp.models

data class PersonNote(
    val id: String = "",
    val personId: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)
