package com.example.spyapp.api

import android.util.Log
import com.example.spyapp.models.JournalNote
import com.example.spyapp.utils.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JournalRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val partnersRepository = PartnersRepository()
    private val TAG = "JournalRepository"
    
    fun getJournalNotes() = callbackFlow<List<JournalNote>> {
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
        
        Log.d(TAG, "Fetching journal notes from users: $accessibleUserIds")
        
        // Korzystamy z kolekcji głównej "journal" i filtrujemy po dostępnych userIds
        val journalCollection = if (accessibleUserIds.size <= 10) {
            firestore.collection(FirestoreCollections.JOURNAL)
                .whereIn("userId", accessibleUserIds)
        } else {
            // Firestore ma ograniczenie do 10 wartości w whereIn
            // To uproszczone podejście, w rzeczywistej aplikacji można by użyć
            // kilku zapytań lub innej strategii
            firestore.collection(FirestoreCollections.JOURNAL)
                .whereEqualTo("userId", userId)
        }
            
        val subscription = journalCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { 
                Log.e(TAG, "Error fetching journal notes: ${error.message}")
                close(error); 
                return@addSnapshotListener 
            }
            if (snapshot != null) {
                val notes = snapshot.toObjects(JournalNote::class.java).mapIndexed { index, note ->
                    note.copy(id = snapshot.documents[index].id)
                }
                Log.d(TAG, "Fetched ${notes.size} journal notes")
                trySend(notes).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }
    
    // New method for getting notes for a specific person
    fun getJournalNotesForPerson(personId: String) = callbackFlow<List<JournalNote>> {
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
        
        Log.d(TAG, "Fetching journal notes for person $personId from users: $accessibleUserIds")
        
        // Korzystamy z kolekcji głównej "journal" i filtrujemy po dostępnych userIds
        val journalCollection = if (accessibleUserIds.size <= 10) {
            firestore.collection(FirestoreCollections.JOURNAL)
                .whereIn("userId", accessibleUserIds)
                .whereArrayContains("personIds", personId)
        } else {
            // Firestore ma ograniczenie do 10 wartości w whereIn
            // To uproszczone podejście, w rzeczywistej aplikacji można by użyć
            // kilku zapytań lub innej strategii
            firestore.collection(FirestoreCollections.JOURNAL)
                .whereEqualTo("userId", userId)
                .whereArrayContains("personIds", personId)
        }
            
        val subscription = journalCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { 
                Log.e(TAG, "Error fetching journal notes for person: ${error.message}")
                close(error)
                return@addSnapshotListener 
            }
            if (snapshot != null) {
                val notes = snapshot.toObjects(JournalNote::class.java).mapIndexed { index, note ->
                    note.copy(id = snapshot.documents[index].id)
                }
                Log.d(TAG, "Fetched ${notes.size} journal notes for person $personId")
                trySend(notes).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }
    
    suspend fun addJournalNote(note: JournalNote) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        
        // Dodajemy pole userId do notatki przed zapisaniem
        val noteWithUserId = note.copy(userId = userId)
        
        try {
            firestore.collection(FirestoreCollections.JOURNAL)
                .add(noteWithUserId)
                .await()
            Log.d(TAG, "Journal note added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding journal note: ${e.message}")
            throw e
        }
    }
    
    suspend fun updateJournalNote(note: JournalNote) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        if (note.id.isEmpty()) throw Exception("Note id is empty")
        
        // Pobieramy aktualną notatkę, aby nie zmienić właściciela
        val existingNote = firestore.collection(FirestoreCollections.JOURNAL)
            .document(note.id)
            .get()
            .await()
            .toObject(JournalNote::class.java) ?: throw Exception("Journal note not found")
            
        // Upewniamy się, że pole userId pozostaje niezmienione
        val noteWithCorrectUserId = note.copy(userId = existingNote.userId)
        
        try {
            firestore.collection(FirestoreCollections.JOURNAL)
                .document(note.id)
                .set(noteWithCorrectUserId)
                .await()
            Log.d(TAG, "Journal note updated successfully: ${note.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating journal note: ${e.message}")
            throw e
        }
    }
}
