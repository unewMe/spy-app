package com.example.spyapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spyapp.models.JournalNote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonNoteFormScreen(
    personId: String,
    initialNote: JournalNote?,
    isEditMode: Boolean,
    onSave: (JournalNote) -> Unit,
    onClose: () -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                title = { Text(if (isEditMode) "Edit Note" else "Add Note") },
                actions = {
                    TextButton(onClick = {
                        val note = JournalNote(
                            id = initialNote?.id ?: "",
                            title = title,
                            content = content,
                            timestamp = System.currentTimeMillis(),
                            personIds = initialNote?.personIds?.toList() ?: listOf(personId)
                        )
                        onSave(note)
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}
