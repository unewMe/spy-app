package com.example.spyapp.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.spyapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object UserSession {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    init {
        
        updateUserFromFirebase(FirebaseAuth.getInstance().currentUser)
    }
    
    fun updateUserFromFirebase(firebaseUser: FirebaseUser?) {
        _currentUser.value = User.fromFirebaseUser(firebaseUser)
    }
    
    
    fun setEmail(newEmail: String) {
        val existingUser = _currentUser.value ?: User()
        _currentUser.value = existingUser.copy(email = newEmail)
    }
    
    
    val email: StateFlow<String?> = MutableStateFlow(_currentUser.value?.email)
}
