package com.example.spyapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.spyapp.components.PersonCard
import com.example.spyapp.models.Person

class PersonCardPreviewScreenshot {
    @Preview(showBackground = true)
    @Composable
    fun PersonCardPreview() {
        PersonCard(
            person = Person(
                firstName = "John",
                lastName = "Doe",
                photoUrl = "https://example.com/avatar.jpg",
                updatedAt = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}