package com.example.spyapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spyapp.api.InvitationRepository
import com.example.spyapp.api.PartnersRepository
import com.example.spyapp.models.Invitation
import com.example.spyapp.models.Partner
import com.example.spyapp.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountViewModel(
    private val partnersRepository: PartnersRepository = PartnersRepository(),
    private val invitationRepository: InvitationRepository = InvitationRepository(),
    initialUser: User? = null
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _partners = MutableStateFlow<List<Partner>>(emptyList())
    val partners: StateFlow<List<Partner>> = _partners.asStateFlow()

    private val _invitations = MutableStateFlow<List<Invitation>>(emptyList())
    val invitations: StateFlow<List<Invitation>> = _invitations.asStateFlow()

    init {
        if (initialUser != null) {
            _currentUser.value = initialUser
        } else {
            loadCurrentUser()
        }
        loadPartners()
        loadInvitations()
    }

    private fun loadCurrentUser() {

        viewModelScope.launch {
            UserSession.currentUser.collectLatest { user ->
                _currentUser.value = user
            }
        }
    }

    private fun loadPartners() {
        viewModelScope.launch {
            partnersRepository.getPartners()
                .catch { e ->
                    Log.e("AccountViewModel", "Error loading partners", e)
                }
                .collect { partnersList ->
                    _partners.value = partnersList
                }
        }
    }

    private fun loadInvitations() {
        viewModelScope.launch {
            invitationRepository.getInvitations()
                .catch { e ->
                    Log.e("AccountViewModel", "Error loading invitations", e)
                }
                .collect { invitationsList ->
                    _invitations.value = invitationsList
                }
        }
    }

    fun addPartner(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                invitationRepository.sendInvitation(email)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun acceptInvitation(invitation: Invitation, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                invitationRepository.acceptInvitation(invitation)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun rejectInvitation(invitation: Invitation, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                invitationRepository.rejectInvitation(invitation)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}