package com.example.spyapp.api

import com.example.spyapp.models.PersonNote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PersonNoteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getPersonNotes(personId: String) = callbackFlow<List<PersonNote>> {
        val userId = auth.currentUser?.uid ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        // Zakładamy, że notatki dotyczące osób są przechowywane w kolekcji "personNotes"
        // i filtrujemy je po polu personId
        val collection = firestore.collection("users")
            .document(userId)
            .collection("personNotes")
            .whereEqualTo("personId", personId)
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val notes = snapshot.toObjects(PersonNote::class.java).mapIndexed { index, note ->
                    note.copy(id = snapshot.documents[index].id)
                }
                trySend(notes).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addPersonNote(note: PersonNote) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        firestore.collection("users")
            .document(userId)
            .collection("personNotes")
            .add(note).await()
    }

    suspend fun updatePersonNote(note: PersonNote) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        if (note.id.isEmpty()) throw Exception("Note id is empty")
        firestore.collection("users")
            .document(userId)
            .collection("personNotes")
            .document(note.id).set(note).await()
    }
}
