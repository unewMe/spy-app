package com.example.spyapp.api

import com.example.spyapp.models.Invitation
import com.example.spyapp.utils.FirestoreCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InvitationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Wysyłanie zaproszenia – dodaje dokument w kolekcji "invitations"
    // W tym rozwiązaniu zaproszenie jest zapisywane bezpośrednio w kolekcji "invitations"
    suspend fun sendInvitation(toEmail: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
        val invitation = Invitation(
            fromUserId = currentUser.uid,
            fromEmail = currentUser.email ?: "",
            toEmail = toEmail,
            timestamp = System.currentTimeMillis()
        )
        
        // Zapisujemy zaproszenie w kolekcji głównej
        firestore.collection(FirestoreCollections.INVITATIONS)
            .add(invitation)
            .await()
    }

    fun getInvitations() = callbackFlow<List<Invitation>> {
        val currentUser = auth.currentUser ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        // Pobieramy zaproszenia z kolekcji głównej filtrując po emailu odbiorcy
        val invitationsRef = firestore.collection(FirestoreCollections.INVITATIONS)
            .whereEqualTo("toEmail", currentUser.email)
            
        val subscription = invitationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val invitations = snapshot.toObjects(Invitation::class.java)
                    .mapIndexed { index, invitation ->
                        invitation.copy(id = snapshot.documents[index].id)
                    }
                trySend(invitations).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    // Akceptacja zaproszenia – dodajemy partnera do kolekcji partners
    suspend fun acceptInvitation(invitation: Invitation) {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
        
        // Dodajemy nadawcę jako partnera u odbiorcy
        firestore.collection(FirestoreCollections.PARTNERS)
            .add(mapOf(
                "userId" to currentUser.uid,
                "partnerId" to invitation.fromUserId,
                "partnerEmail" to invitation.fromEmail
            ))
            .await()
            
        // Dodajemy odbiorcę jako partnera u nadawcy
        val currentEmail = currentUser.email ?: ""
        firestore.collection(FirestoreCollections.PARTNERS)
            .add(mapOf(
                "userId" to invitation.fromUserId,
                "partnerId" to currentUser.uid,
                "partnerEmail" to currentEmail
            ))
            .await()
            
        // Usuwamy zaproszenie
        firestore.collection(FirestoreCollections.INVITATIONS)
            .document(invitation.id)
            .delete()
            .await()
    }

    // Odrzucenie zaproszenia
    suspend fun rejectInvitation(invitation: Invitation) {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
        firestore.collection(FirestoreCollections.INVITATIONS)
            .document(invitation.id)
            .delete()
            .await()
    }
}
