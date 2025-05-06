package com.example.spyapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spyapp.models.JournalNote
import com.example.spyapp.models.Person
import com.example.spyapp.viewmodels.JournalViewModel
import com.example.spyapp.viewmodels.PersonViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNoteSelected: (JournalNote) -> Unit,
    onAddNote: () -> Unit,
    onSelectPerson: (Person?) -> Unit,
    selectedPerson: Person? = null
) {
    val journalViewModel: JournalViewModel = viewModel()
    val personViewModel: PersonViewModel = viewModel()
    val allNotes by journalViewModel.notes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showPersonDialog by remember { mutableStateOf(false) }
    var localSelectedPerson by remember { mutableStateOf(selectedPerson) }
    val filteredNotes = allNotes.filter { note ->
        val matchesQuery = note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
        val matchesPerson = localSelectedPerson?.let { it.id in note.personIds } ?: true
        matchesQuery && matchesPerson
    }
    if (showPersonDialog) {
        
        PersonSelectionDialog(
            onDismiss = { showPersonDialog = false },
            onPersonSelected = { person ->
                localSelectedPerson = person
                onSelectPerson(person)
                showPersonDialog = false
            },
            showAllOption = true
        )
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Journal") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNote,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search notes") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter by person: ", style = MaterialTheme.typography.bodyMedium)
                if (localSelectedPerson != null) {
                    Text(localSelectedPerson!!.name, style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text("All", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showPersonDialog = true }) {
                    Text("Select Person")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredNotes) { note ->
                    NoteCard(note = note, onClick = { onNoteSelected(note) })
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: JournalNote, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(note.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(dateFormat.format(Date(note.timestamp)), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        }
    }
}
