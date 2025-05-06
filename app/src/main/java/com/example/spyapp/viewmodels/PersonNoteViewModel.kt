package com.example.spyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spyapp.api.JournalRepository
import com.example.spyapp.models.JournalNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PersonNoteViewModel(private val personId: String) : ViewModel() {
    private val repository = JournalRepository()
    private val _notes = MutableStateFlow<List<JournalNote>>(emptyList())
    val notes: StateFlow<List<JournalNote>> = _notes

    init {
        viewModelScope.launch {
            repository.getJournalNotesForPerson(personId).collect { notes ->
                _notes.value = notes.sortedByDescending { it.timestamp }
            }
        }
    }

    fun addNote(note: JournalNote, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                
                val noteWithPersonId = if (personId !in note.personIds) {
                    note.copy(personIds = note.personIds + personId)
                } else {
                    note
                }
                repository.addJournalNote(noteWithPersonId)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun updateNote(note: JournalNote, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                
                val noteWithPersonId = if (personId !in note.personIds) {
                    note.copy(personIds = note.personIds + personId)
                } else {
                    note
                }
                repository.updateJournalNote(noteWithPersonId)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
