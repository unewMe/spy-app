package com.example.spyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spyapp.api.PartnersRepository
import com.example.spyapp.models.Partner
import com.example.spyapp.models.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PartnersViewModel : ViewModel() {
    private val repository = PartnersRepository()
    private val _partners = MutableStateFlow<List<Partner>>(emptyList())
    val partners: StateFlow<List<Partner>> = _partners

    init {
        viewModelScope.launch {
            repository.getPartners().collect { partnersList ->
                _partners.value = partnersList
            }
        }
    }
}
