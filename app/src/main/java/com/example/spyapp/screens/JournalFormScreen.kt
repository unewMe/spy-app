package com.example.spyapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spyapp.models.JournalNote
import com.example.spyapp.models.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalFormScreen(
    initialNote: JournalNote?,
    isEditMode: Boolean,
    onSave: (JournalNote) -> Unit,
    onClose: () -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var selectedPersons by remember { mutableStateOf<List<Person>>(emptyList()) }
    var showPersonDialog by remember { mutableStateOf(false) }
    var currentHashtagIndex by remember { mutableStateOf<Int?>(null) }
    var processedHashtags by remember { mutableStateOf(mutableSetOf<Int>()) }

    // Aktualizujemy stan wybranych osób – jeśli w treści usunięto wcześniej dodane tagi
    selectedPersons = selectedPersons.filter { person ->
        content.contains("#${person.name.replace(" ", "_")} ")
    }

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
                            personIds = selectedPersons.map { it.id }
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
                onValueChange = { newValue ->
                    content = newValue
                    // Szukamy wszystkich wystąpień '#' w tekście
                    val indices = newValue.indices.filter { newValue[it] == '#' }
                    val newIndices = indices.filter { it !in processedHashtags }
                    if (newIndices.isNotEmpty()) {
                        currentHashtagIndex = newIndices.first()
                        showPersonDialog = true
                    }
                },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }

    if (showPersonDialog && currentHashtagIndex != null) {
        PersonSelectionDialog(
            onDismiss = {
                showPersonDialog = false
                currentHashtagIndex = null
            },
            onPersonSelected = { person ->
                person?.let {
                    val before = content.substring(0, currentHashtagIndex!!)
                    val after = if (currentHashtagIndex!! < content.length - 1)
                        content.substring(currentHashtagIndex!! + 1) else ""
                    // Używamy U+2060 jako niewidocznego znacznika, aby id było ukryte
                    val invisibleMarker = "\u2060"
                    val tag = "#${it.name.replace(" ", "_")}"
                    content = before + tag + after
                    processedHashtags.add(currentHashtagIndex!!)
                    selectedPersons = selectedPersons + it
                }
                showPersonDialog = false
                currentHashtagIndex = null
            },
            showAllOption = false
        )
    }
}
