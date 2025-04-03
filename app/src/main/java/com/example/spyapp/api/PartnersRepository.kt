package com.example.spyapp.api

import com.example.spyapp.models.Person
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PartnersRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getPartners() = callbackFlow<List<Person>> {
        val userId = auth.currentUser?.uid ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        val partnersRef = firestore.collection("users")
            .document(userId)
            .collection("partners")
        val subscription = partnersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val partners = snapshot.toObjects(Person::class.java).mapIndexed { index, partner ->
                    partner.copy(id = snapshot.documents[index].id)
                }
                trySend(partners).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }
}
