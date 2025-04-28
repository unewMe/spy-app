package com.example.spyapp.models

import com.google.firebase.auth.FirebaseUser

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSignInTime: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromFirebaseUser(firebaseUser: FirebaseUser?): User {
            return if (firebaseUser != null) {
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    isEmailVerified = firebaseUser.isEmailVerified
                )
            } else {
                User()
            }
        }
    }
}