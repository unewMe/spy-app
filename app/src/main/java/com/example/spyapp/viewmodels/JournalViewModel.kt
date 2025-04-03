package com.example.spyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spyapp.api.JournalRepository
import com.example.spyapp.models.JournalNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class JournalViewModel : ViewModel() {
    private val repository = JournalRepository()
    private val _notes = MutableStateFlow<List<JournalNote>>(emptyList())
    val notes: StateFlow<List<JournalNote>> = _notes
    init {
        viewModelScope.launch {
            repository.getJournalNotes().collect { notes ->
                _notes.value = notes.sortedByDescending { it.timestamp }
            }
        }
    }
    fun addNote(note: JournalNote, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addJournalNote(note)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
    fun updateNote(note: JournalNote, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateJournalNote(note)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
