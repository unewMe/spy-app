package com.example.spyapp.models

data class PersonNote(
    val id: String = "",
    val userId: String = "", // Added userId field for top-level collection
    val personId: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)
