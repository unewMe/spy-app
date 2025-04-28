package com.example.spyapp.api

import android.util.Log
import com.example.spyapp.models.PersonNote
import com.example.spyapp.utils.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PersonNoteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val partnersRepository = PartnersRepository()
    private val TAG = "PersonNoteRepository"

    fun getPersonNotes(personId: String) = callbackFlow<List<PersonNote>> {
        val userId = auth.currentUser?.uid ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        // Pobierz listę ID wszystkich partnerów
        val partnerIds = try {
            partnersRepository.getPartnerIds()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching partner IDs: ${e.message}")
            emptyList<String>()
        }
        
        // Utwórz listę wszystkich ID do których użytkownik ma dostęp (własne + partnerów)
        val accessibleUserIds = listOf(userId) + partnerIds
        
        Log.d(TAG, "Fetching notes for person $personId from users: $accessibleUserIds")
        
        // Korzystamy z kolekcji głównej "personNotes" i filtrujemy po personId oraz dostępnych userIds
        val collection = if (accessibleUserIds.size <= 10) {
            firestore.collection(FirestoreCollections.PERSON_NOTES)
                .whereEqualTo("personId", personId)
                .whereIn("userId", accessibleUserIds)
        } else {
            // Firestore ma ograniczenie do 10 wartości w whereIn
            // To uproszczone podejście, w rzeczywistej aplikacji można by użyć
            // kilku zapytań lub innej strategii
            firestore.collection(FirestoreCollections.PERSON_NOTES)
                .whereEqualTo("personId", personId)
                .whereEqualTo("userId", userId)
        }
            
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error fetching notes: ${error.message}")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val notes = snapshot.toObjects(PersonNote::class.java).mapIndexed { index, note ->
                    note.copy(id = snapshot.documents[index].id)
                }
                Log.d(TAG, "Fetched ${notes.size} notes for person $personId")
                trySend(notes).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun addPersonNote(note: PersonNote) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        // Dodajemy pole userId do notatki
        val noteWithUserId = note.copy(userId = userId)
        
        try {
            firestore.collection(FirestoreCollections.PERSON_NOTES)
                .add(noteWithUserId).await()
            Log.d(TAG, "Note added successfully for person ${note.personId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding note: ${e.message}")
            throw e
        }
    }

    suspend fun updatePersonNote(note: PersonNote) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        if (note.id.isEmpty()) throw Exception("Note id is empty")
        
        // Pobieramy aktualną notatkę, aby nie zmienić właściciela
        val existingNote = firestore.collection(FirestoreCollections.PERSON_NOTES)
            .document(note.id)
            .get()
            .await()
            .toObject(PersonNote::class.java) ?: throw Exception("Note not found")
            
        // Upewniamy się, że pole userId pozostaje niezmienione
        val noteWithCorrectUserId = note.copy(userId = existingNote.userId)
        
        try {
            firestore.collection(FirestoreCollections.PERSON_NOTES)
                .document(note.id)
                .set(noteWithCorrectUserId).await()
            Log.d(TAG, "Note updated successfully: ${note.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating note: ${e.message}")
            throw e
        }
    }
}
