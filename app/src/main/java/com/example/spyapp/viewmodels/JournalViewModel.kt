package com.example.spyapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spyapp.api.JournalRepository
import com.example.spyapp.models.JournalNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

                if (note.personIds.isEmpty() && containsHashtags(note.content)) {
                    val personIds = extractPersonIdsFromContent(note.content)
                    val updatedNote = note.copy(personIds = personIds)
                    Log.d(
                        "JournalViewModel",
                        "Adding note with personIds: ${updatedNote.personIds}"
                    )
                    repository.addJournalNote(updatedNote)
                } else {
                    Log.d(
                        "JournalViewModel",
                        "Adding note with original personIds: ${note.personIds}"
                    )
                    repository.addJournalNote(note)
                }
                onResult(true, null)
            } catch (e: Exception) {
                Log.e("JournalViewModel", "Error adding note: ${e.message}")
                onResult(false, e.message)
            }
        }
    }

    fun updateNote(note: JournalNote, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {

                if (note.personIds.isEmpty() && containsHashtags(note.content)) {
                    val personIds = extractPersonIdsFromContent(note.content)
                    val updatedNote = note.copy(personIds = personIds)
                    Log.d(
                        "JournalViewModel",
                        "Updating note with personIds: ${updatedNote.personIds}"
                    )
                    repository.updateJournalNote(updatedNote)
                } else {
                    Log.d(
                        "JournalViewModel",
                        "Updating note with original personIds: ${note.personIds}"
                    )
                    repository.updateJournalNote(note)
                }
                onResult(true, null)
            } catch (e: Exception) {
                Log.e("JournalViewModel", "Error updating note: ${e.message}")
                onResult(false, e.message)
            }
        }
    }


    private fun containsHashtags(content: String): Boolean {
        return content.contains("#")
    }


    private fun extractPersonIdsFromContent(content: String): List<String> {


        Log.w(
            "JournalViewModel",
            "extractPersonIdsFromContent called, but can't correctly map hashtags to IDs"
        )
        return emptyList()
    }
}
