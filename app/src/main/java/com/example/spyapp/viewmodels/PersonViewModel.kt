package com.example.spyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spyapp.api.PersonRepository
import com.example.spyapp.models.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PersonViewModel : ViewModel() {
    private val repository = PersonRepository()
    private val _people = MutableStateFlow<List<Person>>(emptyList())
    val people: StateFlow<List<Person>> = _people

    init {
        viewModelScope.launch {
            repository.getPersons().collect { persons ->
                _people.value = persons
            }
        }
    }

    fun addPerson(person: Person, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addPerson(person)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun updatePerson(person: Person, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updatePerson(person)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
    
    // Upload avatar image for a person
    fun uploadAvatar(personId: String, imageData: ByteArray, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val downloadUrl = repository.uploadPersonAvatar(personId, imageData)
                onResult(true, downloadUrl)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
