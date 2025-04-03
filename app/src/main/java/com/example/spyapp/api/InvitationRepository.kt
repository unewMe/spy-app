package com.example.spyapp.api

import com.example.spyapp.models.Invitation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InvitationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Wysyłanie zaproszenia – dodaje dokument w kolekcji "invitations" odbiorcy.
    // W tym rozwiązaniu zaproszenie jest zapisywane w dokumentach nadawcy, a odbiorca pobiera je z kolekcji "invitations" w swoim dokumencie.
    suspend fun sendInvitation(toEmail: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
        // Dla uproszczenia zapisujemy zaproszenie w kolekcji "invitations" bieżącego użytkownika,
        // ale w praktyce możesz zapisywać zaproszenie u nadawcy lub w centralnej kolekcji.
        val invitation = Invitation(
            fromUserId = currentUser.uid,
            fromEmail = currentUser.email ?: "",
            toEmail = toEmail,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection("users")
            .document(currentUser.uid)
            .collection("sentInvitations")
            .add(invitation)
            .await()
    }

    // Pobieranie zaproszeń dla zalogowanego użytkownika (odbiorcy)
    fun getInvitations() = callbackFlow<List<Invitation>> {
        val currentUser = auth.currentUser ?: run {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        // Zakładamy, że odbiorca ma zaproszenia zapisane w kolekcji "invitations" w swoim dokumencie
        val invitationsRef = firestore.collection("users")
            .document(currentUser.uid)
            .collection("invitations")
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

    // Akceptacja zaproszenia – dodajemy partnera do obu kolekcji
    suspend fun acceptInvitation(invitation: Invitation) {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
        // Dodajemy nadawcę jako partnera u odbiorcy
        firestore.collection("users")
            .document(currentUser.uid)
            .collection("partners")
            .document(invitation.fromUserId)
            .set(mapOf("email" to invitation.fromEmail))
            .await()
        // Dodajemy odbiorcę jako partnera u nadawcy
        val currentEmail = currentUser.email ?: ""
        firestore.collection("users")
            .document(invitation.fromUserId)
            .collection("partners")
            .document(currentUser.uid)
            .set(mapOf("email" to currentEmail))
            .await()
        // Usuwamy zaproszenie – z odbiorcy
        firestore.collection("users")
            .document(currentUser.uid)
            .collection("invitations")
            .document(invitation.id)
            .delete()
            .await()
    }

    // Odrzucenie zaproszenia
    suspend fun rejectInvitation(invitation: Invitation) {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
        firestore.collection("users")
            .document(currentUser.uid)
            .collection("invitations")
            .document(invitation.id)
            .delete()
            .await()
    }
}
