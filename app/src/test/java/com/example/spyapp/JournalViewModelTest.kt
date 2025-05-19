package com.example.spyapp

import com.example.spyapp.api.JournalRepository
import com.example.spyapp.models.JournalNote
import com.example.spyapp.viewmodels.JournalViewModel
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
class JournalViewModelTest {
    private lateinit var viewModel: JournalViewModel
    private lateinit var repository: JournalRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock<JournalRepository>()
        whenever(repository.getJournalNotes()).thenReturn(flowOf(emptyList()))
        viewModel = JournalViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNotes populates notes`() = testScope.runTest {
        val expectedNotes = listOf(
            JournalNote(id = "note1", title = "Title 1", content = "Content 1", timestamp = System.currentTimeMillis()),
            JournalNote(id = "note2", title = "Title 2", content = "Content 2", timestamp = System.currentTimeMillis() - 1000)
        )
        whenever(repository.getJournalNotes()).thenReturn(flowOf(expectedNotes))

        // Re-initialize viewModel to trigger collection of notes
        viewModel = JournalViewModel(repository)
        advanceUntilIdle()

        assertEquals(expectedNotes.sortedByDescending { it.timestamp }, viewModel.notes.value)
    }

    @Test
    fun `addNote calls addJournalNote and handles success`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val note = JournalNote(id = "newNote", title = "New Note", content = "New Content", timestamp = System.currentTimeMillis())

        viewModel.addNote(note) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        verify(repository).addJournalNote(note)
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `addNote handles failure`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val note = JournalNote(id = "newNote", title = "New Note", content = "New Content", timestamp = System.currentTimeMillis())
        val exception = RuntimeException("Add failed")
        whenever(repository.addJournalNote(note)).thenThrow(exception)

        viewModel.addNote(note) { success, message ->
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
        val note = JournalNote(id = "note1", title = "Updated Title", content = "Updated Content", timestamp = System.currentTimeMillis())

        viewModel.updateNote(note) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        verify(repository).updateJournalNote(note)
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `updateNote handles failure`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val note = JournalNote(id = "note1", title = "Updated Title", content = "Updated Content", timestamp = System.currentTimeMillis())
        val exception = RuntimeException("Update failed")
        whenever(repository.updateJournalNote(note)).thenThrow(exception)

        viewModel.updateNote(note) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Update failed", errorMessage)
    }
}
