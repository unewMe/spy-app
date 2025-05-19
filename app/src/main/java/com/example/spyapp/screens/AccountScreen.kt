package com.example.spyapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spyapp.R
import com.example.spyapp.models.Invitation
import com.example.spyapp.models.Partner
import com.example.spyapp.viewmodels.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    accountViewModel: AccountViewModel = viewModel(),
    onLogout: () -> Unit = {},
) {
    
    val currentUser by accountViewModel.currentUser.collectAsState()
    val partners by accountViewModel.partners.collectAsState()
    val invitations by accountViewModel.invitations.collectAsState()
    
    
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
                        
                    }
                },
                onReject = { invitation ->
                    accountViewModel.rejectInvitation(invitation) { success, _ ->
                        
                    }
                }
            )
        }
    }
    
    
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
    partners: List<Partner>,
    onAddPartner: () -> Unit,
    onSelectPartner: (Partner) -> Unit
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
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerItem(
    partner: Partner,
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
                text = partner.partnerEmail,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
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
                        HorizontalDivider(
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

