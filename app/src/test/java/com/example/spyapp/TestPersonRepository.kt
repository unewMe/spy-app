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
    
    // Tracking calls for verification
    var updatePersonCalled = false
    var addPersonCalled = false
    
    // Track the most recently used person in operations
    var lastUpdatedPerson: Person? = null
    var lastAddedPerson: Person? = null
    
    // Override persons list for testing
    private val personsFlow = MutableStateFlow<List<Person>>(emptyList())
    
    // Success flags to control callback behavior
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
        
        // Update the person in the list if we want to simulate a real repository
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
        
        // Add the person to our simulated list
        val currentList = personsFlow.value.toMutableList()
        // In a real repository, this would generate an ID
        val personWithId = if (person.id.isEmpty()) {
            person.copy(id = "generated-test-id")
        } else {
            person
        }
        currentList.add(personWithId)
        personsFlow.value = currentList
    }
    
    // Reset state between tests
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