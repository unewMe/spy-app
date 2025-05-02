package com.example.spyapp

import com.example.spyapp.models.Person
import com.example.spyapp.viewmodels.PersonViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit tests for the PersonViewModel interactions with PersonFormScreen
 */
@ExperimentalCoroutinesApi
class PersonFormViewModelTest {
    private lateinit var viewModel: PersonViewModel

    @Before
    fun setup() {

        viewModel = PersonViewModel()
    }

    @Test
    fun `updating person calls updatePerson in viewModel`() {
        // Given
        var successCalled = false
        var resultPerson: Person? = null

        val person = Person(
            id = "test-id",
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            birthdate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse("1990-01-01")?.time ?: 0L
        )

        // When - simulating what happens in PersonFormScreen
        viewModel.updatePerson(person) { success, _ ->
            successCalled = success
            if (success) {
                resultPerson = person
            }
        }
    }

    @Test
    fun `adding new person calls addPerson in viewModel`(){
        // Given
        var successCalled = false
        var resultPerson: Person? = null

        val person = Person(
            id = "",  // Empty ID indicates a new person
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@example.com",
            birthdate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse("1992-05-15")?.time ?: 0L
        )

        // When - simulating what happens in PersonFormScreen
        viewModel.addPerson(person) { success, _ ->
            successCalled = success
            if (success) {
                resultPerson = person
            }
        }



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