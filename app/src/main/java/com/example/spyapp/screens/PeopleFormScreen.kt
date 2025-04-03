package com.example.spyapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spyapp.models.Person
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonFormScreen(
    initialPerson: Person?,
    isEditMode: Boolean,
    onSave: (Person) -> Unit, // callback wywoływany po zatwierdzeniu formularza
    onClose: () -> Unit
) {
    // Jeśli edytujemy, prefillujemy pola; przy dodawaniu pozostają puste
    var firstName by remember { mutableStateOf(initialPerson?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initialPerson?.lastName ?: "") }
    var email by remember { mutableStateOf(initialPerson?.email ?: "") }

    // Format daty: "yyyy-MM-dd"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var birthday by remember {
        mutableStateOf(
            if (initialPerson != null && initialPerson.birthdate != 0L) dateFormat.format(Date(initialPerson.birthdate))
            else ""
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                title = { Text(if (isEditMode) "Edit person" else "Add person") },
                actions = {
                    // Po kliknięciu Save budujemy obiekt Person i wywołujemy onSave
                    TextButton(onClick = {
                        val parsedBirthday = try {
                            dateFormat.parse(birthday)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                        val person = Person(
                            id = initialPerson?.id ?: "", // przy edycji zachowujemy ID, przy dodawaniu puste
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            birthdate = parsedBirthday,
                            updatedAt = System.currentTimeMillis(),
                            photoUrl = "" // bez zdjęcia
                        )
                        onSave(person)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AccountBox,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            TextButton(onClick = { /* Placeholder: dodawanie zdjęcia - pomijamy */ }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add photo")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isEditMode) {
                Text("${firstName} ${lastName}", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = birthday,
                onValueChange = { birthday = it },
                label = { Text("Birthday (yyyy-MM-dd)") },
                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
