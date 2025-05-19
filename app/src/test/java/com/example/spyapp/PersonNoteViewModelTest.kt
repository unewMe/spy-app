package com.example.spyapp

import com.example.spyapp.api.JournalRepository
import com.example.spyapp.models.JournalNote
import com.example.spyapp.viewmodels.PersonNoteViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class PersonNoteViewModelTest {
    private lateinit var viewModel: PersonNoteViewModel
    private lateinit var journalRepository: JournalRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val personId = "test-person-id"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        journalRepository = mock<JournalRepository>()

        whenever(journalRepository.getJournalNotesForPerson(personId)).thenReturn(flowOf(emptyList()))

        viewModel = PersonNoteViewModel(personId, journalRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNotes populates notes`() = testScope.runTest {
        val expectedNotes = listOf(
            JournalNote(id = "note1", personIds = listOf(personId), content = "Test note 1", timestamp = System.currentTimeMillis()),
            JournalNote(id = "note2", personIds = listOf(personId), content = "Test note 2", timestamp = System.currentTimeMillis() - 1000)
        )
        whenever(journalRepository.getJournalNotesForPerson(personId)).thenReturn(flowOf(expectedNotes))

        // Re-initialize viewModel to trigger collection of new flow
        viewModel = PersonNoteViewModel(personId, journalRepository)
        advanceUntilIdle()

        assertEquals(expectedNotes.sortedByDescending { it.timestamp }, viewModel.notes.value)
    }

    @Test
    fun `addNote calls addJournalNote and handles success`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val noteContent = "New test note"
        val newNote = JournalNote(content = noteContent, personIds = emptyList(), timestamp = System.currentTimeMillis())
        val expectedNoteWithPersonId = newNote.copy(personIds = listOf(personId))


        viewModel.addNote(newNote) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        verify(journalRepository).addJournalNote(expectedNoteWithPersonId)
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `addNote handles failure`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val noteContent = "New test note"
        val newNote = JournalNote(content = noteContent, personIds = emptyList(), timestamp = System.currentTimeMillis())
        val expectedNoteWithPersonId = newNote.copy(personIds = listOf(personId))
        val exception = RuntimeException("Add failed")
        whenever(journalRepository.addJournalNote(expectedNoteWithPersonId)).thenThrow(exception)

        viewModel.addNote(newNote) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Add failed", errorMessage)
    }

    @Test
    fun `updateNote calls updateJournalNote and handles success`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val existingNote = JournalNote(id = "note1", personIds = listOf(personId), content = "Original content", timestamp = System.currentTimeMillis())
        val updatedNote = existingNote.copy(content = "Updated content")

        viewModel.updateNote(updatedNote) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        verify(journalRepository).updateJournalNote(updatedNote)
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `updateNote ensures personId is in note and handles success`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val existingNote = JournalNote(id = "note1", personIds = listOf("other-person-id"), content = "Original content", timestamp = System.currentTimeMillis())
        val updatedNote = existingNote.copy(content = "Updated content")
        val expectedNoteWithPersonId = updatedNote.copy(personIds = updatedNote.personIds + personId)


        viewModel.updateNote(updatedNote) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        verify(journalRepository).updateJournalNote(expectedNoteWithPersonId)
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }


    @Test
    fun `updateNote handles failure`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val existingNote = JournalNote(id = "note1", personIds = listOf(personId), content = "Original content", timestamp = System.currentTimeMillis())
        val updatedNote = existingNote.copy(content = "Updated content")
        val exception = RuntimeException("Update failed")
        whenever(journalRepository.updateJournalNote(updatedNote)).thenThrow(exception)

        viewModel.updateNote(updatedNote) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Update failed", errorMessage)
    }
}
