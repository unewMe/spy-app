package com.example.spyapp.models

data class Partner(
    val id: String = "",
    val userId: String = "",       // ID of the user who owns this partner entry
    val partnerId: String = "",    // ID of the partner user
    val partnerEmail: String = ""  // Email of the partner for display purposes
)