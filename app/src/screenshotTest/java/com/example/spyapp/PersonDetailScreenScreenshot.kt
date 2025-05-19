package com.example.spyapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.spyapp.models.Person
import com.example.spyapp.screens.PersonDetailScreen

class PersonDetailScreenScreenshot {
    @Preview(showBackground = true)
    @Composable
    fun PersonDetailScreenPreview() {
        PersonDetailScreen(
            person = Person(
                id = "person1",
                firstName = "Jane",
                lastName = "Doe",
                photoUrl = "https://example.com/avatar2.jpg",
                birthdate = System.currentTimeMillis() - 30L * 365 * 24 * 60 * 60 * 1000, // Approx 30 years ago
                email = "jane.doe@example.com",
                updatedAt = System.currentTimeMillis()
            ),
            onBack = {},
            onEdit = {},
            onGallery = {},
            onRecordings = {},
            onNotes = {}
        )
    }
}
