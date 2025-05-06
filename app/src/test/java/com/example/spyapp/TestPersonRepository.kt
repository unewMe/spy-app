package com.example.spyapp

import com.example.spyapp.api.PersonRepository
import com.example.spyapp.models.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * A test implementation of PersonRepository for unit tests
 */
class TestPersonRepository : PersonRepository() {
    
    
    var updatePersonCalled = false
    var addPersonCalled = false
    
    
    var lastUpdatedPerson: Person? = null
    var lastAddedPerson: Person? = null
    
    
    private val personsFlow = MutableStateFlow<List<Person>>(emptyList())
    
    
    var shouldSucceed = true
    var errorMessage = "Test error"
    
    override fun getPersons(): Flow<List<Person>> {
        return personsFlow
    }
    
    override suspend fun updatePerson(person: Person) {
        updatePersonCalled = true
        lastUpdatedPerson = person
        
        if (!shouldSucceed) {
            throw Exception(errorMessage)
        }
        
        
        val currentList = personsFlow.value
        val updatedList = currentList.map { 
            if (it.id == person.id) person else it 
        }
        personsFlow.value = updatedList
    }
    
    override suspend fun addPerson(person: Person) {
        addPersonCalled = true
        lastAddedPerson = person
        
        if (!shouldSucceed) {
            throw Exception(errorMessage)
        }
        
        
        val currentList = personsFlow.value.toMutableList()
        
        val personWithId = if (person.id.isEmpty()) {
            person.copy(id = "generated-test-id")
        } else {
            person
        }
        currentList.add(personWithId)
        personsFlow.value = currentList
    }
    
    
    fun reset() {
        updatePersonCalled = false
        addPersonCalled = false
        lastUpdatedPerson = null
        lastAddedPerson = null
        shouldSucceed = true
        errorMessage = "Test error"
        personsFlow.value = emptyList()
    }
}