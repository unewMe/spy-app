package com.example.spyapp.api

import android.util.Log
import com.example.spyapp.models.Person
import com.example.spyapp.utils.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

open class PersonRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val partnersRepository = PartnersRepository()
    private val TAG = "PersonRepository"

    open fun getPersons() = callbackFlow<List<Person>> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not authenticated")
            close(Exception("User not authenticated"))
            return@callbackFlow
        }


        val partnerIds = try {
            partnersRepository.getPartnerIds()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching partner IDs: ${e.message}")
            emptyList<String>()
        }


        val accessibleUserIds = listOf(userId) + partnerIds

        Log.d(TAG, "Fetching persons for users: $accessibleUserIds")


        val personsCollection = if (accessibleUserIds.size <= 10) {
            firestore.collection(FirestoreCollections.PERSONS)
                .whereIn("userId", accessibleUserIds)
        } else {


            firestore.collection(FirestoreCollections.PERSONS)
                .whereEqualTo("userId", userId)
        }

        val subscription = personsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error fetching persons: ${error.message}")
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val persons = snapshot.toObjects(Person::class.java).mapIndexed { index, person ->
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


    open suspend fun addPerson(person: Person) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        val personWithUserId = person.copy(userId = userId)

        try {

            val docRef = firestore.collection(FirestoreCollections.PERSONS)
                .add(personWithUserId).await()
            Log.d(TAG, "Person added successfully with id: ${docRef.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding person: ${e.message}")
            throw e
        }
    }


    open suspend fun updatePerson(person: Person) {
        auth.currentUser?.uid ?: throw Exception("User not authenticated")
        if (person.id.isEmpty()) {
            throw Exception("Person id is empty, cannot update")
        }


        val existingPerson = firestore.collection(FirestoreCollections.PERSONS)
            .document(person.id)
            .get()
            .await()
            .toObject(Person::class.java) ?: throw Exception("Person not found")

        val personWithCorrectUserId = person.copy(userId = existingPerson.userId)

        try {
            firestore.collection(FirestoreCollections.PERSONS)
                .document(person.id)
                .set(personWithCorrectUserId).await()
            Log.d(TAG, "Person updated successfully: ${person.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating person: ${e.message}")
            throw e
        }
    }


    suspend fun uploadPersonAvatar(personId: String, imageData: ByteArray): String {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        try {

            val filename = "avatar_${System.currentTimeMillis()}.jpg"

            val ref = storage.reference.child("users/$userId/persons/$personId/avatars/$filename")

            ref.putBytes(imageData).await()

            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(TAG, "Avatar uploaded successfully: $downloadUrl")


            firestore.collection(FirestoreCollections.PERSONS)
                .document(personId)
                .update("photoUrl", downloadUrl)
                .await()

            return downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading avatar: ${e.message}")
            throw e
        }
    }
}
