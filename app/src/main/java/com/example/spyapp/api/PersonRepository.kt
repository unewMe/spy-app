package com.example.spyapp.api

import android.util.Log
import com.example.spyapp.models.Person
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PersonRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "PersonRepository"

    // Pobieranie listy osób z subkolekcji /users/{userId}/persons
    fun getPersons() = callbackFlow<List<Person>> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not authenticated")
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        val personsCollection = firestore.collection("users")
            .document(userId)
            .collection("persons")
        val subscription = personsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error fetching persons: ${error.message}")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Ważne: upewnij się, że dokumenty zawierają pole id – możesz to zrobić ręcznie lub ustawić automatycznie przy pobieraniu
                val persons = snapshot.toObjects(Person::class.java).mapIndexed { index, person ->
                    // Jeśli używasz automatycznego generowania ID, możesz nadpisać pole id
                    person.copy(id = snapshot.documents[index].id)
                }
                Log.d(TAG, "Fetched ${persons.size} persons")
                trySend(persons).isSuccess
            }
        }
        awaitClose {
            Log.d(TAG, "Removing snapshot listener")
            subscription.remove()
        }
    }

    // Funkcja dodająca osobę do subkolekcji użytkownika
    suspend fun addPerson(person: Person) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val personsCollection = firestore.collection("users")
            .document(userId)
            .collection("persons")
        try {
            // Dodajemy dokument – Firestore sam nada ID
            val docRef = personsCollection.add(person).await()
            Log.d(TAG, "Person added successfully with id: ${docRef.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding person: ${e.message}")
            throw e
        }
    }

    // Funkcja aktualizująca osobę – wymaga, by person.id było ustawione
    suspend fun updatePerson(person: Person) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        if (person.id.isEmpty()) {
            throw Exception("Person id is empty, cannot update")
        }
        val docRef = firestore.collection("users")
            .document(userId)
            .collection("persons")
            .document(person.id)
        try {
            docRef.set(person).await()
            Log.d(TAG, "Person updated successfully: ${person.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating person: ${e.message}")
            throw e
        }
    }
}
