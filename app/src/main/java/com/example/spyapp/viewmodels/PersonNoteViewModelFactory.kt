package com.example.spyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PersonNoteViewModelFactory(private val personId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonNoteViewModel(personId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
