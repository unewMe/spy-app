package com.example.spyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spyapp.api.InvitationRepository
import com.example.spyapp.models.Invitation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvitationViewModel : ViewModel() {
    private val repository = InvitationRepository()
    private val _invitations = MutableStateFlow<List<Invitation>>(emptyList())
    val invitations: StateFlow<List<Invitation>> = _invitations

    init {
        viewModelScope.launch {
            repository.getInvitations().collect { invitations ->
                _invitations.value = invitations
            }
        }
    }

    fun sendInvitation(toEmail: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.sendInvitation(toEmail)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun acceptInvitation(invitation: Invitation, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.acceptInvitation(invitation)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun rejectInvitation(invitation: Invitation, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.rejectInvitation(invitation)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
