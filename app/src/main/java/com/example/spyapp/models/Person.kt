package com.example.spyapp.models

data class Person(
    val id: String = "",
    val userId: String = "", // Added userId field for top-level collection
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val birthdate: Long = 0L,    // zapisany jako timestamp (millis)
    val updatedAt: Long = 0L,    // timestamp aktualizacji
    val photoUrl: String = ""
) {
    val name: String
        get() = "$firstName $lastName"

    val updated: String
        get() = "Updated: " + updatedAt.toString() // tutaj możesz sformatować datę
}
