package com.example.spyapp.api

import com.example.spyapp.models.JournalNote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JournalRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    fun getJournalNotes() = callbackFlow<List<JournalNote>> {
        val userId = auth.currentUser?.uid ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        val journalCollection = firestore.collection("users").document(userId).collection("journal")
        val subscription = journalCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val notes = snapshot.toObjects(JournalNote::class.java).mapIndexed { index, note ->
                    note.copy(id = snapshot.documents[index].id)
                }
                trySend(notes).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }
    suspend fun addJournalNote(note: JournalNote) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        firestore.collection("users").document(userId).collection("journal").add(note).await()
    }
    suspend fun updateJournalNote(note: JournalNote) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        if (note.id.isEmpty()) throw Exception("Note id is empty")
        firestore.collection("users").document(userId).collection("journal").document(note.id).set(note).await()
    }
}
