package com.example.spyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spyapp.api.PersonNoteRepository
import com.example.spyapp.models.PersonNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PersonNoteViewModel(private val personId: String) : ViewModel() {
    private val repository = PersonNoteRepository()
    private val _notes = MutableStateFlow<List<PersonNote>>(emptyList())
    val notes: StateFlow<List<PersonNote>> = _notes

    init {
        viewModelScope.launch {
            repository.getPersonNotes(personId).collect { notes ->
                _notes.value = notes.sortedByDescending { it.timestamp }
            }
        }
    }

    fun addNote(note: PersonNote, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addPersonNote(note)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun updateNote(note: PersonNote, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updatePersonNote(note)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
