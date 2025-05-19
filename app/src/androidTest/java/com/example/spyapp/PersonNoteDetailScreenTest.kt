package com.example.spyapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.spyapp.models.JournalNote
import com.example.spyapp.screens.PersonNoteDetailScreen
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PersonNoteDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestJournalNote(
        id: String = "test-note-id",
        userId: String = "test-user-id",
        timestamp: Long = System.currentTimeMillis(),
        title: String = "Default Test Note Title",
        content: String = "Default content for the test note.",
        personIds: List<String> = emptyList()
    ): JournalNote {
        return JournalNote(
            id = id,
            userId = userId,
            timestamp = timestamp,
            title = title,
            content = content,
            personIds = personIds
        )
    }

    @Test
    fun testNullNoteShowsNotFoundMessageAndNoEditAction() {
        val onBackMock: () -> Unit = mock()
        val onEditMock: (JournalNote) -> Unit = mock()

        composeTestRule.setContent {
            PersonNoteDetailScreen(
                note = null,
                onBack = onBackMock,
                onEdit = onEditMock
            )
        }

        composeTestRule.onNodeWithText("Note Detail").assertExists()
        composeTestRule.onNodeWithText("Notatka nie została znaleziona").assertExists()
        composeTestRule.onNodeWithContentDescription("Back").assertExists()
        composeTestRule.onNodeWithContentDescription("Edit").assertDoesNotExist()
    }

    @Test
    fun testValidNoteShowsDetailsAndAllowsActions() {
        val timestamp = System.currentTimeMillis()
        val testNote = createTestJournalNote(
            timestamp = timestamp,
            title = "Specific Test Note Title",
            content = "This is the specific content of the test note."
        )
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val expectedDateString = dateFormat.format(Date(timestamp))

        val onBackMock: () -> Unit = mock()
        val onEditMock: (JournalNote) -> Unit = mock()

        composeTestRule.setContent {
            PersonNoteDetailScreen(
                note = testNote,
                onBack = onBackMock,
                onEdit = onEditMock
            )
        }

        composeTestRule.onNodeWithText("Note Detail").assertExists()
        composeTestRule.onNodeWithText(testNote.title).assertExists()
        composeTestRule.onNodeWithText(expectedDateString).assertExists()
        composeTestRule.onNodeWithText(testNote.content).assertExists()
        composeTestRule.onNodeWithContentDescription("Back").assertExists()
        composeTestRule.onNodeWithContentDescription("Edit").assertExists()

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        verify(onBackMock).invoke()

        composeTestRule.onNodeWithContentDescription("Edit").performClick()
        verify(onEditMock).invoke(testNote)
    }
}
