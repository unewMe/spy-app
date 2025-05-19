package com.example.spyapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.spyapp.models.JournalNote
import com.example.spyapp.screens.JournalDetailScreen

class JournalDetailScreenScreenshot {
    @Preview(showBackground = true)
    @Composable
    fun JournalDetailScreenPreview() {
        JournalDetailScreen(
            note = JournalNote(
                id = "1",
                title = "Test Note",
                content = "This is the content of the test note.",
                timestamp = System.currentTimeMillis(),
                personIds = listOf()
            ),
            onBack = {},
            onEdit = {}
        )
    }
}
