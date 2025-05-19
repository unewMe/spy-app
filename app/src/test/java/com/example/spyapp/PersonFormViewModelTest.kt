package com.example.spyapp

import com.example.spyapp.api.PersonRepository
import com.example.spyapp.models.Person
import com.example.spyapp.viewmodels.PersonViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@ExperimentalCoroutinesApi
class PersonFormViewModelTest {
    private lateinit var viewModel: PersonViewModel
    private lateinit var testRepository: PersonRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testRepository = mock<PersonRepository>()
        whenever(testRepository.getPersons()).thenReturn(flowOf(listOf<Person>()))
        viewModel = PersonViewModel(testRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updating person calls updatePerson in viewModel`() = testScope.runTest {

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


        viewModel.updatePerson(person) { success, message ->
            successCalled = success
            errorMessage = message
        }



        advanceUntilIdle()

        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `updating person handles failures`() = testScope.runTest {

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

        whenever(testRepository.updatePerson(person)).thenThrow(RuntimeException("Update failed"))

        viewModel.updatePerson(person) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Update failed", errorMessage)
    }

    @Test
    fun `adding new person calls addPerson in viewModel`() = testScope.runTest {

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

        viewModel.addPerson(person) { success, message ->
            successCalled = success
            errorMessage = message
        }


        advanceUntilIdle()

        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `adding new person handles failures`() = testScope.runTest {
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

        whenever(testRepository.addPerson(person)).thenThrow(RuntimeException("Add failed"))

        viewModel.addPerson(person) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Add failed", errorMessage)
    }

    @Test
    fun `parsing valid date string returns correct timestamp`() {
        val dateString = "1990-01-01"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


        val timestamp = try {
            dateFormat.parse(dateString)?.time
        } catch (_: Exception) {
            null
        }


        assertNotNull("Date should be parsed successfully", timestamp)


        val formattedDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp!!))
        assertEquals("1990-01-01", formattedDate)
    }

    @Test
    fun `parsing invalid date string returns null`() {
        val invalidDateString = "not-a-date"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val timestamp = try {
            dateFormat.parse(invalidDateString)?.time
        } catch (e: Exception) {
            null
        }

        assertNull("Invalid date should return null", timestamp)
    }
}