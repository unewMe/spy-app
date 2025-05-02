package com.example.spyapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.spyapp.models.Person
import com.example.spyapp.screens.PersonFormScreen
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class PeopleFormScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun personFormScreen_displaysCorrectInitialValues() {
        // Given
        val testPerson = Person(
            id = "test-id",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            birthdate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse("1990-01-01")?.time ?: 0L,
            photoUrl = ""
        )
        
        // When
        composeTestRule.setContent {
            PersonFormScreen(
                initialPerson = testPerson,
                isEditMode = true,
                onSave = {},
                onClose = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Edit person").assertExists()
        composeTestRule.onNodeWithText("John").assertExists()
        composeTestRule.onNodeWithText("Doe").assertExists()
        composeTestRule.onNodeWithText("john.doe@example.com").assertExists()
        composeTestRule.onNodeWithText("1990-01-01").assertExists()
    }
    
    @Test
    fun personFormScreen_showsAddPersonTitle_whenNotInEditMode() {
        // When
        composeTestRule.setContent {
            PersonFormScreen(
                initialPerson = null,
                isEditMode = false,
                onSave = {},
                onClose = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Add person").assertExists()
    }
    
    @Test
    fun personFormScreen_showsNameInHeader_whenInEditMode() {
        // Given
        val testPerson = Person(
            id = "test-id",
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com"
        )
        
        // When
        composeTestRule.setContent {
            PersonFormScreen(
                initialPerson = testPerson,
                isEditMode = true,
                onSave = {},
                onClose = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Jane Smith").assertExists()
    }
    
    @Test
    fun personFormScreen_savesInput_whenSaveClicked() {
        // Given
        var savedPerson: Person? = null
        
        composeTestRule.setContent {
            PersonFormScreen(
                initialPerson = null,
                isEditMode = false,
                onSave = { person ->
                    savedPerson = person
                },
                onClose = {}
            )
        }
        
        // When
        composeTestRule.onNodeWithText("First name").performTextInput("Alex")
        composeTestRule.onNodeWithText("Last name").performTextInput("Johnson")
        composeTestRule.onNodeWithText("Email").performTextInput("alex.j@example.com")
        composeTestRule.onNodeWithText("Birthday (yyyy-MM-dd)").performTextInput("1985-05-15")
        
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Then
        assert(savedPerson != null)
        assert(savedPerson?.firstName == "Alex")
        assert(savedPerson?.lastName == "Johnson")
        assert(savedPerson?.email == "alex.j@example.com")
        
        // We should verify the date was parsed correctly
        val expectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .parse("1985-05-15")?.time
        assert(savedPerson?.birthdate?.toString()?.startsWith(expectedDate.toString().substring(0, 8)) == true)
    }
    
    @Test
    fun personFormScreen_closesScreen_whenCloseButtonClicked() {
        // Given
        var closeCalled = false
        
        composeTestRule.setContent {
            PersonFormScreen(
                initialPerson = null,
                isEditMode = false,
                onSave = {},
                onClose = { closeCalled = true }
            )
        }
        
        // When
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        
        // Then
        assert(closeCalled)
    }
}