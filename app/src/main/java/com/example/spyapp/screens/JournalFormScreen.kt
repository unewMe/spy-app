package com.example.spyapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spyapp.models.JournalNote
import com.example.spyapp.models.Person
import com.example.spyapp.viewmodels.PersonViewModel

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
    var previousContent by remember { mutableStateOf(content) }
    
    // For hashtag tracking
    var inHashtagMode by remember { mutableStateOf(false) }
    var hashtagStartIndex by remember { mutableStateOf(-1) }
    
    // Keep track of all completed hashtags - format: position -> tag name
    var completedHashtags by remember { mutableStateOf(mutableMapOf<Int, String>()) }
    
    // Keep track of selected persons and their IDs with proper initialization
    val personViewModel: PersonViewModel = viewModel()
    val allPeople by personViewModel.people.collectAsState()
    
    // Initialize selectedPersonIds with initialNote's personIds or empty list
    var selectedPersonIds by remember { mutableStateOf<List<String>>(initialNote?.personIds ?: emptyList()) }
    
    // Initialize selectedPersons with matching Person objects for the IDs
    var selectedPersons by remember(allPeople, selectedPersonIds) { 
        mutableStateOf(
            allPeople.filter { it.id in selectedPersonIds }
        )
    }
    
    // When initialNote changes and has personIds, update our selectedPersons list
    LaunchedEffect(initialNote, allPeople) {
        if (initialNote != null && initialNote.personIds.isNotEmpty()) {
            selectedPersonIds = initialNote.personIds
            selectedPersons = allPeople.filter { person -> person.id in selectedPersonIds }
            
            // Initialize hashtags for existing note with personIds
            if (content.isNotEmpty() && selectedPersons.isNotEmpty()) {
                selectedPersons.forEach { person ->
                    val hashtagText = "#${person.name.replace(" ", "_")}"
                    val index = content.indexOf(hashtagText)
                    if (index >= 0) {
                        completedHashtags[index] = hashtagText
                    }
                }
            }
            Log.d("JournalFormScreen", "Loaded ${selectedPersons.size} people for note with ${selectedPersonIds.size} personIds")
        }
    }
    
    var showPersonDialog by remember { mutableStateOf(false) }

    // Compare current content with previous content to detect hashtag changes
    LaunchedEffect(content) {
        if (content != previousContent) {
            // Check for newly added hashtags
            if (!inHashtagMode && content.length > previousContent.length) {
                // Find newly added #
                val addedChars = content.length - previousContent.length
                for (i in 0 until content.length) {
                    if (i >= previousContent.length || content[i] != previousContent[i]) {
                        // Found the first different character, check if it's #
                        if (content[i] == '#' && (i == 0 || content[i-1].isWhitespace())) {
                            // This is a new hashtag that's not inside an existing one
                            if (!isPositionInsideExistingHashtag(i, completedHashtags)) {
                                inHashtagMode = true
                                hashtagStartIndex = i
                                showPersonDialog = true
                                break
                            }
                        }
                        break
                    }
                }
            }
            
            // Update selected persons if hashtags were manually removed
            val existingHashtagsInContent = mutableSetOf<String>()
            selectedPersons.forEach { person ->
                val hashtag = "#${person.name.replace(" ", "_")}"
                if (content.contains(hashtag)) {
                    existingHashtagsInContent.add(hashtag)
                }
            }
            
            // Update completedHashtags based on current content
            val newCompletedHashtags = mutableMapOf<Int, String>()
            completedHashtags.forEach { (pos, tag) ->
                val newPos = content.indexOf(tag)
                if (newPos >= 0) {
                    newCompletedHashtags[newPos] = tag
                }
            }
            completedHashtags = newCompletedHashtags
            
            // Remove persons whose hashtags are no longer in the content
            selectedPersons.forEach { person ->
                val hashtag = "#${person.name.replace(" ", "_")}"
                if (!existingHashtagsInContent.contains(hashtag)) {
                    selectedPersons = selectedPersons.filter { it.id != person.id }
                    selectedPersonIds = selectedPersonIds.filter { it != person.id }
                }
            }
            
            previousContent = content
        }
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
                    // Add a button to manually select people to tag
                    IconButton(onClick = { 
                        inHashtagMode = false  // We're not in automatic hashtag mode
                        hashtagStartIndex = -1 // Reset hashtag position
                        showPersonDialog = true 
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Tag Person")
                    }
                    
                    TextButton(onClick = {
                        val note = JournalNote(
                            id = initialNote?.id ?: "",
                            title = title,
                            content = content,
                            timestamp = System.currentTimeMillis(),
                            personIds = selectedPersonIds.distinct() // Use the tracked personIds
                        )
                        
                        Log.d("JournalFormScreen", "Saving note with ${selectedPersonIds.size} personIds: $selectedPersonIds")
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
                },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            // Display currently tagged people
            if (selectedPersons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tagged People (${selectedPersons.size}):", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                selectedPersons.forEach { person ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(person.name)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            // Remove the person from the selections
                            selectedPersons = selectedPersons.filter { it.id != person.id }
                            selectedPersonIds = selectedPersonIds.filter { it != person.id }
                            
                            // Also remove the hashtag from content
                            val hashtag = "#${person.name.replace(" ", "_")}"
                            val startIndex = content.indexOf(hashtag)
                            if (startIndex >= 0) {
                                val newContent = StringBuilder(content)
                                newContent.replace(startIndex, startIndex + hashtag.length, "")
                                content = newContent.toString().trim()
                                
                                // Remove this hashtag from completed hashtags
                                completedHashtags = completedHashtags.filter { it.value != hashtag }.toMutableMap()
                            }
                        }) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }

    if (showPersonDialog) {
        PersonSelectionDialog(
            onDismiss = {
                showPersonDialog = false
                inHashtagMode = false
                hashtagStartIndex = -1
            },
            onPersonSelected = { person ->
                person?.let {
                    if (inHashtagMode && hashtagStartIndex >= 0) {
                        // Replace the # with the full hashtag
                        val tag = "#${it.name.replace(" ", "_")}"
                        // Find the end of the partially-typed hashtag (if any)
                        var endIndex = hashtagStartIndex + 1
                        while (endIndex < content.length && !content[endIndex].isWhitespace()) {
                            endIndex++
                        }
                        
                        val before = content.substring(0, hashtagStartIndex)
                        val after = if (endIndex < content.length) content.substring(endIndex) else ""
                        content = before + tag + " " + after
                        
                        // Add this to our completed hashtags
                        completedHashtags[hashtagStartIndex] = tag
                    } else {
                        // Person was selected from the icon button - add at the end
                        val tag = "#${it.name.replace(" ", "_")}"
                        if (!content.contains(tag)) {
                            val newTag = if (content.isEmpty() || content.endsWith(" ")) tag else " $tag"
                            content += newTag
                            completedHashtags[content.length - newTag.length] = tag
                        }
                    }
                    
                    // Add to our selections if not already there
                    if (selectedPersons.none { p -> p.id == it.id }) {
                        selectedPersons = selectedPersons + it
                        selectedPersonIds = selectedPersonIds + it.id
                        Log.d("JournalFormScreen", "Added person ${it.name} with ID ${it.id}. Total: ${selectedPersonIds.size}")
                    }
                }
                showPersonDialog = false
                inHashtagMode = false
                hashtagStartIndex = -1
            },
            showAllOption = false
        )
    }
}

// Helper function to check if a position is inside an existing hashtag
private fun isPositionInsideExistingHashtag(position: Int, completedHashtags: Map<Int, String>): Boolean {
    for ((startPos, tag) in completedHashtags) {
        if (position >= startPos && position <= startPos + tag.length) {
            return true
        }
    }
    return false
}
