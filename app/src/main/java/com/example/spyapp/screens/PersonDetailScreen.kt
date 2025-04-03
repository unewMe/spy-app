package com.example.spyapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.spyapp.models.Person



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(person: Person, onBack: () -> Unit, onEdit: (Person) -> Unit, onGallery: () -> Unit, onRecordings: () -> Unit, onNotes: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(person) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(person.name, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("person.birthdate")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(person.email, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            ButtonList(onGallery, onRecordings, onNotes)
        }
    }
}

@Composable
private fun ButtonList(onGalleryClick: () -> Unit, onRecordingsClick: () -> Unit, onNotes: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedButton(onClick = onNotes, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.EditNote, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Notes")
        }
        ElevatedButton(onClick = onRecordingsClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Mic, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Recordings")
        }
        ElevatedButton(onClick = onGalleryClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gallery")
        }
    }
}
