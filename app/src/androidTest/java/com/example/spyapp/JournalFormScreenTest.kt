package com.example.spyapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.spyapp.api.PersonRepository
import com.example.spyapp.models.JournalNote
import com.example.spyapp.models.Person
import com.example.spyapp.screens.JournalFormScreen
import com.example.spyapp.viewmodels.PersonViewModel
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class JournalFormScreenTest {
    private lateinit var personViewModel: PersonViewModel
    private lateinit var mockPersonRepository: PersonRepository
    private val testPerson1 =
        Person(id = "p1", firstName = "Alice", lastName = "Wonder", email = "alice@example.com")
    private val testPerson2 =
        Person(id = "p2", firstName = "Bob", lastName = "Builder", email = "bob@example.com")

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        mockPersonRepository = mock<PersonRepository>()

        whenever(mockPersonRepository.getPersons()).thenReturn(flowOf(emptyList()))
        personViewModel = PersonViewModel(mockPersonRepository)
    }

    @Test
    fun journalFormScreen_displaysCorrectInitialValues_whenEditingNote() {
        val testNote = JournalNote(
            id = "note1",
            title = "Test Note Title",
            content = "Test note content with #Alice_Wonder",
            personIds = listOf(testPerson1.id)
        )

        whenever(mockPersonRepository.getPersons()).thenReturn(flowOf(listOf(testPerson1)))

        personViewModel = PersonViewModel(mockPersonRepository)

        composeTestRule.setContent {
            JournalFormScreen(
                initialNote = testNote,
                isEditMode = true,
                onSave = {},
                onClose = {},
                personViewModel = personViewModel
            )
        }

        composeTestRule.onNodeWithText("Edit Note").assertExists()
        composeTestRule.onNodeWithText("Test Note Title").assertExists()
        composeTestRule.onNodeWithText("Test note content with #Alice_Wonder").assertExists()
        composeTestRule.onNodeWithText("Tagged People (1):", substring = true).assertExists()
        composeTestRule.onNodeWithText("Alice Wonder").assertExists()
    }

    @Test
    fun journalFormScreen_displaysAddNoteTitle_whenNotInEditMode() {

        composeTestRule.setContent {
            JournalFormScreen(
                initialNote = null,
                isEditMode = false,
                onSave = {},
                onClose = {},
                personViewModel = personViewModel
            )
        }
        composeTestRule.onNodeWithText("Add Note").assertExists()
    }

    @Test
    fun journalFormScreen_savesInput_whenSaveClicked() {
        var savedNote: JournalNote? = null
        whenever(mockPersonRepository.getPersons()).thenReturn(flowOf(listOf(testPerson1)))
        personViewModel = PersonViewModel(mockPersonRepository)

        composeTestRule.setContent {
            JournalFormScreen(
                initialNote = null,
                isEditMode = false,
                onSave = { note -> savedNote = note },
                onClose = {},
                personViewModel = personViewModel
            )
        }

        composeTestRule.onNodeWithText("Title").performTextInput("New Note Title")
        composeTestRule.onNodeWithText("Content").performTextInput("New note content here.")

        composeTestRule.onNodeWithContentDescription("Tag Person").performClick()
        composeTestRule.onNodeWithText("Alice Wonder").performClick()

        composeTestRule.onNodeWithText("Save").performClick()

        assert(savedNote != null)
        assert(savedNote?.title == "New Note Title")
        assert(savedNote?.content == "New note content here. #Alice_Wonder")
        assert(savedNote?.personIds?.contains(testPerson1.id) == true)
    }

    @Test
    fun journalFormScreen_closesScreen_whenCloseButtonClicked() {
        var closeCalled = false

        composeTestRule.setContent {
            JournalFormScreen(
                initialNote = null,
                isEditMode = false,
                onSave = {},
                onClose = { closeCalled = true },
                personViewModel = personViewModel
            )
        }
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        assert(closeCalled)
    }

    @Test
    fun journalFormScreen_typingHashtag_triggersPersonSelectionAndUpdatesContent() {
        whenever(mockPersonRepository.getPersons()).thenReturn(
            flowOf(
                listOf(
                    testPerson1, testPerson2
                )
            )
        )
        personViewModel = PersonViewModel(mockPersonRepository)

        composeTestRule.setContent {
            JournalFormScreen(
                initialNote = null,
                isEditMode = false,
                onSave = {},
                onClose = {},
                personViewModel = personViewModel
            )
        }

        composeTestRule.onNodeWithText("Content").performTextInput("Hello ")
        composeTestRule.onNodeWithText("Content").performTextInput("#")
        composeTestRule.onNodeWithText("Alice Wonder").performClick()

        composeTestRule.onNodeWithText("Hello #Alice_Wonder ").assertExists()
        composeTestRule.onNodeWithText("Tagged People (1):", substring = true).assertExists()
        composeTestRule.onNodeWithText("Alice Wonder").assertExists()
    }

    @Test
    fun journalFormScreen_manualTagPerson_triggersPersonSelectionAndUpdatesContent() {
        whenever(mockPersonRepository.getPersons()).thenReturn(flowOf(listOf(testPerson1)))
        personViewModel = PersonViewModel(mockPersonRepository)

        composeTestRule.setContent {
            JournalFormScreen(
                initialNote = null,
                isEditMode = false,
                onSave = {},
                onClose = {},
                personViewModel = personViewModel
            )
        }

        composeTestRule.onNodeWithText("Content").performTextInput("Some initial text.")
        composeTestRule.onNodeWithContentDescription("Tag Person").performClick()
        composeTestRule.onNodeWithText("Alice Wonder").performClick()

        composeTestRule.onNodeWithText("Some initial text. #Alice_Wonder").assertExists()
        composeTestRule.onNodeWithText("Tagged People (1):", substring = true).assertExists()
        composeTestRule.onNodeWithText("Alice Wonder").assertExists()
    }

    @Test
    fun journalFormScreen_loadsNoteWithExistingHashtagsAndPeople() {
        val noteWithHashtags = JournalNote(
            id = "note2",
            title = "Note with Tags",
            content = "This note has #Alice_Wonder and also #Bob_Builder.",
            personIds = listOf(testPerson1.id, testPerson2.id)
        )
        whenever(mockPersonRepository.getPersons()).thenReturn(
            flowOf(
                listOf(
                    testPerson1, testPerson2
                )
            )
        )
        personViewModel = PersonViewModel(mockPersonRepository)

        composeTestRule.setContent {
            JournalFormScreen(
                initialNote = noteWithHashtags,
                isEditMode = true,
                onSave = {},
                onClose = {},
                personViewModel = personViewModel
            )
        }

        composeTestRule.onNodeWithText("Note with Tags").assertExists()
        composeTestRule.onNodeWithText("This note has #Alice_Wonder and also #Bob_Builder.")
            .assertExists()
        composeTestRule.onNodeWithText("Tagged People (2):", substring = true).assertExists()
        composeTestRule.onNodeWithText("Alice Wonder").assertExists()
        composeTestRule.onNodeWithText("Bob Builder").assertExists()
    }

    @Test
    fun journalFormScreen_removeTaggedPerson_removesHashtagFromContent() {
        val initialNote = JournalNote(
            id = "note3",
            title = "Test Remove Tag",
            content = "Content with #Alice_Wonder to be removed.",
            personIds = listOf(testPerson1.id)
        )
        whenever(mockPersonRepository.getPersons()).thenReturn(flowOf(listOf(testPerson1)))
        personViewModel = PersonViewModel(mockPersonRepository)

        composeTestRule.setContent {
            JournalFormScreen(
                initialNote = initialNote,
                isEditMode = true,
                onSave = {},
                onClose = {},
                personViewModel = personViewModel
            )
        }

        composeTestRule.onNodeWithText("Content with #Alice_Wonder to be removed.").assertExists()
        composeTestRule.onNodeWithText("Alice Wonder").assertExists()
        composeTestRule.onNode(hasParent(hasAnySibling(hasText("Alice Wonder"))) and hasText("Remove"))
            .performClick()

        composeTestRule.onNodeWithText("Content with to be removed.").assertExists()
        composeTestRule.onNodeWithText("Alice Wonder").assertDoesNotExist()
        composeTestRule.onNodeWithText("Tagged People", substring = true).assertDoesNotExist()
    }

    @Test
    fun journalFormScreen_typingHashtagWithoutPrecedingSpace_doesNotTriggerDialog() {
        whenever(mockPersonRepository.getPersons()).thenReturn(
            flowOf(
                listOf(
                    testPerson1, testPerson2
                )
            )
        )
        personViewModel = PersonViewModel(mockPersonRepository)

        composeTestRule.setContent {
            JournalFormScreen(
                initialNote = null,
                isEditMode = false,
                onSave = {},
                onClose = {},
                personViewModel = personViewModel
            )
        }

        composeTestRule.onNodeWithText("Content").performTextInput("word#")

        composeTestRule.onNodeWithText("Alice Wonder").assertDoesNotExist()
        composeTestRule.onNodeWithText("Bob Builder").assertDoesNotExist()
        composeTestRule.onNodeWithText("Tagged People", substring = true).assertDoesNotExist()
    }
}
