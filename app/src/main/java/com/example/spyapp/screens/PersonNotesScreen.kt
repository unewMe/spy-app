package com.example.spyapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spyapp.models.PersonNote
import com.example.spyapp.viewmodels.PersonNoteViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spyapp.viewmodels.PersonNoteViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonNotesScreen(
    personId: String,
    onNoteSelected: (PersonNote) -> Unit,
    onAddNote: () -> Unit,
    onBack: () -> Unit
) {
    // Uzyskujemy ViewModel z parametrem personId – tu zakładamy, że mamy fabrykę
    val personNoteViewModel: PersonNoteViewModel = viewModel(factory = PersonNoteViewModelFactory(personId))
    val notes by personNoteViewModel.notes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Person Notes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                }
            )
        },
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(notes.filter { note ->
                    note.title.contains(searchQuery, ignoreCase = true)
                }) { note ->
                    PersonNoteItem(note = note, dateFormat = dateFormat, onClick = { onNoteSelected(note) })
                }
            }
        }
    }
}

@Composable
fun PersonNoteItem(note: PersonNote, dateFormat: SimpleDateFormat, onClick: () -> Unit) {
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
        }
    }
}
