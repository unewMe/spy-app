package com.example.spyapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.spyapp.models.Person
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(person: Person, onBack: () -> Unit, onEdit: (Person) -> Unit, onGallery: () -> Unit, onRecordings: () -> Unit, onNotes: () -> Unit) {
    val navController = rememberNavController()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
            // Display avatar image if available, otherwise show a placeholder
            if (person.photoUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(person.photoUrl),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(person.name, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                // Format the birthdate properly
                val birthdateText = if (person.birthdate > 0) {
                    dateFormat.format(Date(person.birthdate))
                } else {
                    "Not specified"
                }
                Text(birthdateText, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = person.email.ifEmpty { "No email" }, 
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
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
