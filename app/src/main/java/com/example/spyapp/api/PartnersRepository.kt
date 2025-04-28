package com.example.spyapp.api

import com.example.spyapp.models.Partner
import com.example.spyapp.utils.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class PartnersRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getPartners() = callbackFlow<List<Partner>> {
        val userId = auth.currentUser?.uid ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        // Pobieramy partnerów z kolekcji głównej filtrując po polu userId
        val partnersRef = firestore.collection(FirestoreCollections.PARTNERS)
            .whereEqualTo("userId", userId)
            
        val subscription = partnersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val partners = snapshot.toObjects(Partner::class.java).mapIndexed { index, partner ->
                    partner.copy(id = snapshot.documents[index].id)
                }
                trySend(partners).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }
    
    // New function to get a list of partner IDs
    suspend fun getPartnerIds(): List<String> {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        
        val snapshot = firestore.collection(FirestoreCollections.PARTNERS)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            
        return snapshot.documents.mapNotNull { it.getString("partnerId") }
    }
    
    // New helper function to get partner IDs as a Flow
    fun getPartnerIdsFlow(): Flow<List<String>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        val partnersRef = firestore.collection(FirestoreCollections.PARTNERS)
            .whereEqualTo("userId", userId)
            
        val subscription = partnersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val partnerIds = snapshot.documents.mapNotNull { it.getString("partnerId") }
                trySend(partnerIds).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }
    
    // Helper function to get all accessible user IDs (current user and all partners)
    suspend fun getAccessibleUserIds(): List<String> {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val partnerIds = getPartnerIds()
        return listOf(userId) + partnerIds
    }
}
