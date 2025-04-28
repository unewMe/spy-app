package com.example.spyapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spyapp.R
import com.example.spyapp.models.Invitation
import com.example.spyapp.models.Person
import com.example.spyapp.models.User
import com.example.spyapp.viewmodels.AccountViewModel
import com.example.spyapp.viewmodels.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    accountViewModel: AccountViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onNavigatePeople: () -> Unit = {},
    onNavigateJournal: () -> Unit = {},
    onNavigateAccount: () -> Unit = {}
) {
    // Collect state from ViewModel
    val currentUser by accountViewModel.currentUser.collectAsState()
    val partners by accountViewModel.partners.collectAsState()
    val invitations by accountViewModel.invitations.collectAsState()
    
    // Dialog state for adding partners
    var showAddPartnerDialog by remember { mutableStateOf(false) }
    var partnerEmail by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.logo2),
                contentDescription = "Spy Icon",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display current user info
            currentUser?.let { user ->
                Text(
                    text = user.displayName.ifEmpty { "User" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user.email,
                    fontSize = 16.sp
                )
            } ?: run {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            PartnersSection(
                partners = partners,
                onAddPartner = { showAddPartnerDialog = true },
                onSelectPartner = { /* Handle partner selection if needed */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            InvitationsSection(
                invitations = invitations,
                onAccept = { invitation ->
                    accountViewModel.acceptInvitation(invitation) { success, _ ->
                        // Optional: Show feedback to user
                    }
                },
                onReject = { invitation ->
                    accountViewModel.rejectInvitation(invitation) { success, _ ->
                        // Optional: Show feedback to user
                    }
                }
            )
        }
    }
    
    // Dialog for adding a partner
    if (showAddPartnerDialog) {
        AlertDialog(
            onDismissRequest = { showAddPartnerDialog = false },
            title = { Text("Add Partner") },
            text = {
                TextField(
                    value = partnerEmail,
                    onValueChange = { partnerEmail = it },
                    label = { Text("Email") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        accountViewModel.addPartner(partnerEmail) { success, _ ->
                            if (success) {
                                partnerEmail = ""
                                showAddPartnerDialog = false
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPartnerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PartnersSection(
    partners: List<Person>,
    onAddPartner: () -> Unit,
    onSelectPartner: (Person) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Partners",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(onClick = onAddPartner) {
                Text("Add Partner")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (partners.isEmpty()) {
            Text(
                text = "No partners yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 150.dp)
            ) {
                items(partners.size) { index ->
                    val partner = partners[index]
                    PartnerItem(
                        partner = partner,
                        onClick = { onSelectPartner(partner) }
                    )
                    
                    if (index < partners.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerItem(
    partner: Person,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = partner.name,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = partner.email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun InvitationsSection(
    invitations: List<Invitation>,
    onAccept: (Invitation) -> Unit,
    onReject: (Invitation) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Invitations",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (invitations.isEmpty()) {
            Text(
                text = "No pending invitations",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(invitations.size) { index ->
                    val invitation = invitations[index]
                    InvitationItem(
                        invitation = invitation,
                        onAccept = { onAccept(invitation) },
                        onReject = { onReject(invitation) }
                    )
                    
                    if (index < invitations.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitationItem(
    invitation: Invitation,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = invitation.fromEmail,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        
        Row {
            TextButton(onClick = onAccept) {
                Text("Accept")
            }
            
            TextButton(onClick = onReject) {
                Text("Reject")
            }
        }
    }
}

