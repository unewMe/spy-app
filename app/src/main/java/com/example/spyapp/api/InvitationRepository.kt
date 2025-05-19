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


    suspend fun sendInvitation(toEmail: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
        val invitation = Invitation(
            fromUserId = currentUser.uid,
            fromEmail = currentUser.email ?: "",
            toEmail = toEmail,
            timestamp = System.currentTimeMillis()
        )


        firestore.collection(FirestoreCollections.INVITATIONS)
            .add(invitation)
            .await()
    }

    fun getInvitations() = callbackFlow<List<Invitation>> {
        val currentUser = auth.currentUser ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }


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


    suspend fun acceptInvitation(invitation: Invitation) {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")


        firestore.collection(FirestoreCollections.PARTNERS)
            .add(
                mapOf(
                    "userId" to currentUser.uid,
                    "partnerId" to invitation.fromUserId,
                    "partnerEmail" to invitation.fromEmail
                )
            )
            .await()


        val currentEmail = currentUser.email ?: ""
        firestore.collection(FirestoreCollections.PARTNERS)
            .add(
                mapOf(
                    "userId" to invitation.fromUserId,
                    "partnerId" to currentUser.uid,
                    "partnerEmail" to currentEmail
                )
            )
            .await()


        firestore.collection(FirestoreCollections.INVITATIONS)
            .document(invitation.id)
            .delete()
            .await()
    }


    suspend fun rejectInvitation(invitation: Invitation) {
        auth.currentUser ?: throw Exception("User not authenticated")
        firestore.collection(FirestoreCollections.INVITATIONS)
            .document(invitation.id)
            .delete()
            .await()
    }
}
