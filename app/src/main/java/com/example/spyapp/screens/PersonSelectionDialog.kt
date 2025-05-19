package com.example.spyapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spyapp.models.Person
import com.example.spyapp.viewmodels.PersonViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun PersonSelectionDialog(
    onDismiss: () -> Unit,
    onPersonSelected: (Person?) -> Unit,
    showAllOption: Boolean = false,
    personViewModel: PersonViewModel = viewModel()
) {
    val persons by personViewModel.people.collectAsState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Person") },
        text = {
            LazyColumn {
                if (showAllOption) {
                    item {
                        Text(
                            text = "All",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPersonSelected(null) }
                                .padding(16.dp)
                        )
                    }
                }
                items(persons) { person ->
                    Text(
                        text = person.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPersonSelected(person) }
                            .padding(16.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
