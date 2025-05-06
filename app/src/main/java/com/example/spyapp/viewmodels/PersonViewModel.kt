package com.example.spyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spyapp.api.PersonRepository
import com.example.spyapp.models.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PersonViewModel(private val repository: PersonRepository = PersonRepository()) : ViewModel() {
    private val _people = MutableStateFlow<List<Person>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    
    
    val filteredPeople = combine(_people, _searchQuery) { people, query ->
        if (query.isBlank()) {
            people
        } else {
            people.filter { person -> 
                person.firstName.contains(query, ignoreCase = true) || 
                person.lastName.contains(query, ignoreCase = true) ||
                person.name.contains(query, ignoreCase = true) ||
                person.email.contains(query, ignoreCase = true)
            }
        }
    }
    
    
    val people: StateFlow<List<Person>> = _people

    init {
        viewModelScope.launch {
            repository.getPersons().collect { persons ->
                _people.value = persons
            }
        }
    }
    
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    
    val searchQuery: StateFlow<String> = _searchQuery

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
