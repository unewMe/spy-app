package com.example.spyapp.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spyapp.models.Person
import com.example.spyapp.R
import com.example.spyapp.Utils.GoogleSignOutUtils
import com.example.spyapp.screens.AccountScreen
import com.example.spyapp.screens.LoginScreen
import com.example.spyapp.screens.PersonScreen
import com.example.spyapp.screens.PersonDetailScreen
import com.example.spyapp.screens.PersonFormScreen
import com.example.spyapp.viewmodels.PersonViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spyapp.screens.GalleryScreen
import com.example.spyapp.screens.JournalDetailScreen
import com.example.spyapp.screens.JournalFormScreen
import com.example.spyapp.screens.JournalScreen
import com.example.spyapp.screens.PersonNoteDetailScreen
import com.example.spyapp.screens.PersonNoteFormScreen
import com.example.spyapp.screens.PersonNotesScreen
import com.example.spyapp.screens.RecordingsScreen
import com.example.spyapp.viewmodels.JournalViewModel
import com.example.spyapp.viewmodels.PersonNoteViewModel
import com.example.spyapp.viewmodels.PersonNoteViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val isUserSignedIn = remember {
        mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
    }
    
    val startDestination = if (isUserSignedIn.value) "people_list" else "login"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != "login") {
                BottomBar(
                    currentRoute = currentRoute,
                    onNavigateToPeople = { navController.navigateSingleTop("people_list") },
                    onNavigateToJournal = { navController.navigateSingleTop("journal") },
                    onNavigateToAccount = { navController.navigateSingleTop("account") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("login") {
                LoginScreen(onLoginSuccess = { navController.navigateSingleTop("people_list") })
            }

            composable("people_list") {
                PersonScreen(
                    onPersonSelected = { person ->
                        navController.navigate("person_detail/${person.id}")
                    },
                    onAddPerson = {
                        navController.navigate("add_person")
                    }
                )
            }

            composable("person_detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                // Wyszukujemy osobę po id z ViewModelu
                val personViewModel: PersonViewModel = viewModel()
                val person = personViewModel.people.collectAsState().value.find { it.id == id }
                if (person != null) {
                    PersonDetailScreen(
                        person = person,
                        onBack = { navController.popBackStack() },
                        onEdit = { p ->
                            navController.navigate("edit_person/${p.id}")
                        },
                        onGallery = { navController.navigate("gallery") },
                        onRecordings = { navController.navigate("recordings") },
                        onNotes = { navController.navigate("person_notes/${person.id}") }

                    )
                }
            }

            composable("recordings") {
                RecordingsScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("gallery") {
                GalleryScreen(navController = navController, onBack = { navController.popBackStack() })
            }

            composable("add_person") {
                val personViewModel: PersonViewModel = viewModel()
                PersonFormScreen(
                    initialPerson = null,
                    isEditMode = false,
                    onSave = { newPerson ->
                        personViewModel.addPerson(newPerson) { success, error ->
                            if (success) {
                                navController.popBackStack()
                            } else {
                                // Możesz wyświetlić błąd np. Toast lub SnackBar
                            }
                        }
                    },
                    onClose = { navController.popBackStack() }
                )
            }

            composable("edit_person/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                val personViewModel: PersonViewModel = viewModel()
                val person = personViewModel.people.collectAsState().value.find { it.id == id }
                if (person != null) {
                    PersonFormScreen(
                        initialPerson = person,
                        isEditMode = true,
                        onSave = { updatedPerson ->
                            personViewModel.updatePerson(updatedPerson) { success, error ->
                                if (success) {
                                    navController.popBackStack()
                                } else {
                                    // Obsłuż błąd
                                }
                            }
                        },
                        onClose = { navController.popBackStack() }
                    )
                }
            }

            composable("journal") {
                var selectedPerson by remember { mutableStateOf<Person?>(null) }
                JournalScreen(
                    onNoteSelected = { note -> navController.navigate("journal_detail/${note.id}") },
                    onAddNote = { navController.navigate("add_journal") },
                    onSelectPerson = { person -> selectedPerson = person },
                    selectedPerson = selectedPerson
                )
            }

            composable("journal_detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                val journalViewModel: JournalViewModel = viewModel()
                val note = journalViewModel.notes.collectAsState().value.find { it.id == id }
                if (note != null) {
                    JournalDetailScreen(
                        note = note,
                        onBack = { navController.popBackStack() },
                        onEdit = { editedNote -> navController.navigate("edit_journal/${editedNote.id}") }

                    )
                }
            }

            composable("add_journal") {
                val journalViewModel: JournalViewModel = viewModel()
                JournalFormScreen(
                    initialNote = null,
                    isEditMode = false,
                    onSave = { newNote ->
                        journalViewModel.addNote(newNote) { success, error ->
                            if (success) { navController.popBackStack() }
                        }
                    },
                    onClose = { navController.popBackStack() }
                )
            }

            composable("edit_journal/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                val journalViewModel: JournalViewModel = viewModel()
                val note = journalViewModel.notes.collectAsState().value.find { it.id == id }
                if (note != null) {
                    JournalFormScreen(
                        initialNote = note,
                        isEditMode = true,
                        onSave = { updatedNote ->
                            journalViewModel.updateNote(updatedNote) { success, error ->
                                if (success) { navController.popBackStack() }
                            }
                        },
                        onClose = { navController.popBackStack() }
                    )
                }
            }
            composable("person_notes/{personId}") { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId") ?: ""
                PersonNotesScreen(
                    personId = personId,
                    onNoteSelected = { note ->
                        navController.navigate("person_note_detail/${personId}/${note.id}")
                    },
                    onAddNote = {
                        navController.navigate("add_person_note/$personId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

// Trasa dla dodawania notatki dla osoby
            composable("add_person_note/{personId}") { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId") ?: ""
                val personNoteViewModel: PersonNoteViewModel = viewModel(factory = PersonNoteViewModelFactory(personId))
                PersonNoteFormScreen(
                    personId = personId,
                    initialNote = null,
                    isEditMode = false,
                    onSave = { note ->
                        personNoteViewModel.addNote(note) { success, error ->
                            if (success) {
                                navController.popBackStack()
                            } else {
                                // Możesz wyświetlić komunikat o błędzie np. za pomocą Toast lub Snackbar
                                // Toast.makeText(context, error ?: "Error saving note", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onClose = { navController.popBackStack() }
                )
            }

// Trasa dla szczegółów notatki
            composable("person_note_detail/{personId}/{noteId}") { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId") ?: ""
                val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                // Pobierz notatkę z PersonNoteViewModel – należy zastosować odpowiednią logikę
                // Tutaj dla uproszczenia zakładamy, że masz już notatkę (możesz dodać podobną logikę jak w JournalDetailScreen)
                val personNoteViewModel: PersonNoteViewModel = viewModel(factory = PersonNoteViewModelFactory(personId))
                val note = personNoteViewModel.notes.collectAsState().value.find { it.id == noteId }
                if (note != null) {
                    PersonNoteDetailScreen(
                        note = note,
                        onBack = { navController.popBackStack() },
                        onEdit = { editedNote -> navController.navigate("edit_person_note/${personId}/${editedNote.id}") }
                    )
                }
            }

// Trasa dla edycji notatki dla osoby
            composable("edit_person_note/{personId}/{noteId}") { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId") ?: ""
                val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                val personNoteViewModel: PersonNoteViewModel = viewModel(factory = PersonNoteViewModelFactory(personId))
                val note = personNoteViewModel.notes.collectAsState().value.find { it.id == noteId }
                if (note != null) {
                    PersonNoteFormScreen(
                        personId = note.personId,
                        initialNote = note,
                        isEditMode = true,
                        onSave = { updatedNote ->
                            personNoteViewModel.updateNote(updatedNote) { success, error ->
                                if (success) navController.popBackStack()
                            }
                        },
                        onClose = { navController.popBackStack() }
                    )
                }
            }

            composable("account") {
                val context = LocalContext.current
                AccountScreen(onLogout = {
                    GoogleSignOutUtils.doGoogleSignOut(context, logout = { navController.navigateSingleTop("login") })
                })
            }
        }
    }
}

@Composable
fun BottomBar(
    currentRoute: String?,
    onNavigateToPeople: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "people_list"
                    || currentRoute?.startsWith("person_detail") == true
                    || currentRoute?.startsWith("add_person") == true
                    || currentRoute?.startsWith("edit_person") == true,
            onClick = onNavigateToPeople,
            icon = { Icon(Icons.Default.Person, contentDescription = "People", modifier = Modifier.size(24.dp)) },
            label = { Text("People") }
        )
        NavigationBarItem(
            selected = currentRoute == "journal",
            onClick = onNavigateToJournal,
            icon = { Icon(Icons.Default.Book, contentDescription = "Journal", modifier = Modifier.size(24.dp)) },
            label = { Text("Journal") }
        )
        NavigationBarItem(
            selected = currentRoute == "account",
            onClick = onNavigateToAccount,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.logo2),
                    contentDescription = "Account",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Account") }
        )
    }
}

fun NavHostController.navigateSingleTop(route: String) {
    this.navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}
