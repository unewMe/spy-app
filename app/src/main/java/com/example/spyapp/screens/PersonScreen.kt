package com.example.spyapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.spyapp.R
import com.example.spyapp.components.PersonCard
import com.example.spyapp.models.Person
import com.example.spyapp.viewmodels.PersonViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PersonScreen(
    onPersonSelected: (Person) -> Unit,
    onAddPerson: () -> Unit,
    viewModel: PersonViewModel = viewModel()
) {
    val peopleState = viewModel.people.collectAsState()
    val people = peopleState.value

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search people") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.SwapVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Last update",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.logo2),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(people.size) { index ->
                    PersonCard(person = people[index]) { person ->
                        onPersonSelected(person)
                    }
                }
            }
        }

        // FloatingActionButton w prawym dolnym rogu
        FloatingActionButton(
            onClick = onAddPerson,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Person")
        }
    }
}
