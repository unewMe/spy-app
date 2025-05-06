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
    
    
    var inHashtagMode by remember { mutableStateOf(false) }
    var hashtagStartIndex by remember { mutableStateOf(-1) }
    
    
    var completedHashtags by remember { mutableStateOf(mutableMapOf<Int, String>()) }
    
    
    val personViewModel: PersonViewModel = viewModel()
    val allPeople by personViewModel.people.collectAsState()
    
    
    var selectedPersonIds by remember { mutableStateOf<List<String>>(initialNote?.personIds ?: emptyList()) }
    
    
    var selectedPersons by remember(allPeople, selectedPersonIds) { 
        mutableStateOf(
            allPeople.filter { it.id in selectedPersonIds }
        )
    }
    
    
    LaunchedEffect(initialNote, allPeople) {
        if (initialNote != null && initialNote.personIds.isNotEmpty()) {
            selectedPersonIds = initialNote.personIds
            selectedPersons = allPeople.filter { person -> person.id in selectedPersonIds }
            
            
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

    
    LaunchedEffect(content) {
        if (content != previousContent) {
            
            if (!inHashtagMode && content.length > previousContent.length) {
                
                val addedChars = content.length - previousContent.length
                for (i in 0 until content.length) {
                    if (i >= previousContent.length || content[i] != previousContent[i]) {
                        
                        if (content[i] == '#' && (i == 0 || content[i-1].isWhitespace())) {
                            
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
            
            
            val existingHashtagsInContent = mutableSetOf<String>()
            selectedPersons.forEach { person ->
                val hashtag = "#${person.name.replace(" ", "_")}"
                if (content.contains(hashtag)) {
                    existingHashtagsInContent.add(hashtag)
                }
            }
            
            
            val newCompletedHashtags = mutableMapOf<Int, String>()
            completedHashtags.forEach { (pos, tag) ->
                val newPos = content.indexOf(tag)
                if (newPos >= 0) {
                    newCompletedHashtags[newPos] = tag
                }
            }
            completedHashtags = newCompletedHashtags
            
            
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
                    
                    IconButton(onClick = { 
                        inHashtagMode = false  
                        hashtagStartIndex = -1 
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
                            personIds = selectedPersonIds.distinct() 
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
                            
                            selectedPersons = selectedPersons.filter { it.id != person.id }
                            selectedPersonIds = selectedPersonIds.filter { it != person.id }
                            
                            
                            val hashtag = "#${person.name.replace(" ", "_")}"
                            val startIndex = content.indexOf(hashtag)
                            if (startIndex >= 0) {
                                val newContent = StringBuilder(content)
                                newContent.replace(startIndex, startIndex + hashtag.length, "")
                                content = newContent.toString().trim()
                                
                                
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
                        
                        val tag = "#${it.name.replace(" ", "_")}"
                        
                        var endIndex = hashtagStartIndex + 1
                        while (endIndex < content.length && !content[endIndex].isWhitespace()) {
                            endIndex++
                        }
                        
                        val before = content.substring(0, hashtagStartIndex)
                        val after = if (endIndex < content.length) content.substring(endIndex) else ""
                        content = before + tag + " " + after
                        
                        
                        completedHashtags[hashtagStartIndex] = tag
                    } else {
                        
                        val tag = "#${it.name.replace(" ", "_")}"
                        if (!content.contains(tag)) {
                            val newTag = if (content.isEmpty() || content.endsWith(" ")) tag else " $tag"
                            content += newTag
                            completedHashtags[content.length - newTag.length] = tag
                        }
                    }
                    
                    
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


private fun isPositionInsideExistingHashtag(position: Int, completedHashtags: Map<Int, String>): Boolean {
    for ((startPos, tag) in completedHashtags) {
        if (position >= startPos && position <= startPos + tag.length) {
            return true
        }
    }
    return false
}
