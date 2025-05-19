package com.example.spyapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.spyapp.screens.JournalFormScreen
import com.example.spyapp.models.JournalNote
import com.example.spyapp.viewmodels.PersonViewModel
import org.mockito.kotlin.mock
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.whenever

class JournalFormScreenScreenshot {

    @Preview(showBackground = true, name = "Add Mode")
    @Composable
    fun JournalFormScreenAddModePreview() {
        val mockPersonViewModel: PersonViewModel = mock()

         whenever(mockPersonViewModel.people).thenReturn(MutableStateFlow(emptyList()))

        JournalFormScreen(
            initialNote = null,
            isEditMode = false,
            onSave = {},
            onClose = {},
            personViewModel = mockPersonViewModel
        )
    }

    @Preview(showBackground = true, name = "Edit Mode")
    @Composable
    fun JournalFormScreenEditModePreview() {
        val mockPersonViewModel: PersonViewModel = mock()

         whenever(mockPersonViewModel.people).thenReturn(MutableStateFlow(emptyList()))


        val sampleNote = JournalNote(
            id = "sample-id",
            title = "Sample Note Title",
            content = "This is some sample content for the journal note.",
            timestamp = System.currentTimeMillis(),
            personIds = emptyList()
        )
        JournalFormScreen(
            initialNote = sampleNote,
            isEditMode = true,
            onSave = {},
            onClose = {},
            personViewModel = mockPersonViewModel
        )
    }


}
