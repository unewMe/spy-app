package com.example.spyapp.models

data class JournalNote(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val personIds: List<String> = emptyList()
)
