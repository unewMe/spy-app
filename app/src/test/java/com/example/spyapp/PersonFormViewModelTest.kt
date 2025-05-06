package com.example.spyapp

import com.example.spyapp.models.Person
import com.example.spyapp.viewmodels.PersonViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit tests for the PersonViewModel interactions with PersonFormScreen
 */
@ExperimentalCoroutinesApi
class PersonFormViewModelTest {
    private lateinit var viewModel: PersonViewModel
    private lateinit var testRepository: TestPersonRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Create a test repository instead of a mock
        testRepository = TestPersonRepository()
        viewModel = PersonViewModel(testRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updating person calls updatePerson in viewModel`() = testScope.runTest {
        // Given
        var successCalled = false
        var errorMessage: String? = null

        val person = Person(
            id = "test-id",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            birthdate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse("1990-01-01")?.time ?: 0L
        )

        // When
        viewModel.updatePerson(person) { success, message ->
            successCalled = success
            errorMessage = message
        }

        // Then
        // Advance the dispatcher to allow the coroutine to execute
        advanceUntilIdle()
        
        // Verify the repository method was called with the correct parameter
        assertTrue("updatePerson should be called", testRepository.updatePersonCalled)
        assertEquals("Person parameter should match", person, testRepository.lastUpdatedPerson)
        
        // Check that our callback was triggered with success
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `updating person handles failures`() = testScope.runTest {
        // Given
        var successCalled = false
        var errorMessage: String? = null

        val person = Person(
            id = "test-id",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            birthdate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse("1990-01-01")?.time ?: 0L
        )
        
        // Configure repository to fail
        testRepository.shouldSucceed = false
        testRepository.errorMessage = "Update failed"

        // When
        viewModel.updatePerson(person) { success, message ->
            successCalled = success
            errorMessage = message
        }

        // Then
        // Advance the dispatcher to allow the coroutine to execute
        advanceUntilIdle()
        
        // Verify the repository method was called
        assertTrue("updatePerson should be called", testRepository.updatePersonCalled)
        
        // Check that our callback was triggered with failure
        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Update failed", errorMessage)
    }

    @Test
    fun `adding new person calls addPerson in viewModel`() = testScope.runTest {
        // Given
        var successCalled = false
        var errorMessage: String? = null

        val person = Person(
            id = "",  // Empty ID indicates a new person
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            birthdate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse("1992-05-15")?.time ?: 0L
        )

        // When
        viewModel.addPerson(person) { success, message ->
            successCalled = success
            errorMessage = message
        }

        // Then
        // Advance the dispatcher to allow the coroutine to execute
        advanceUntilIdle()
        
        // Verify the repository method was called with the correct parameter
        assertTrue("addPerson should be called", testRepository.addPersonCalled)
        assertEquals("Person parameter should match", person, testRepository.lastAddedPerson)
        
        // Check that our callback was triggered with success
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `adding new person handles failures`() = testScope.runTest {
        // Given
        var successCalled = false
        var errorMessage: String? = null

        val person = Person(
            id = "",
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            birthdate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse("1992-05-15")?.time ?: 0L
        )
        
        // Configure repository to fail
        testRepository.shouldSucceed = false
        testRepository.errorMessage = "Add failed"

        // When
        viewModel.addPerson(person) { success, message ->
            successCalled = success
            errorMessage = message
        }

        // Then
        // Advance the dispatcher to allow the coroutine to execute
        advanceUntilIdle()
        
        // Verify the repository method was called
        assertTrue("addPerson should be called", testRepository.addPersonCalled)
        
        // Check that our callback was triggered with failure
        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Add failed", errorMessage)
    }

    @Test
    fun `parsing valid date string returns correct timestamp`() {
        // Given
        val dateString = "1990-01-01"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // When
        val timestamp = try {
            dateFormat.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }

        // Then
        assertNotNull("Date should be parsed successfully", timestamp)

        // Convert back to string to verify
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp!!))
        assertEquals("1990-01-01", formattedDate)
    }

    @Test
    fun `parsing invalid date string returns null`() {
        // Given
        val invalidDateString = "not-a-date"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // When
        val timestamp = try {
            dateFormat.parse(invalidDateString)?.time
        } catch (e: Exception) {
            null
        }

        // Then
        assertNull("Invalid date should return null", timestamp)
    }
}