package com.example.spyapp.models

data class Person(
    val id: String = "",
    val userId: String = "", 
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val birthdate: Long = 0L,    
    val updatedAt: Long = 0L,    
    val photoUrl: String = ""
) {
    val name: String
        get() = "$firstName $lastName"

    val updated: String
        get() = "Updated: " + updatedAt.toString() 
}
