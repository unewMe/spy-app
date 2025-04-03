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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spyapp.Person
import com.example.spyapp.R
import com.example.spyapp.models.Invitation
import com.example.spyapp.viewmodels.UserSession

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    currentUser: Person = Person("Dawid Chudzicki", "dawid.chudz@gmail.com"),
    partners: List<Person> = listOf(
        Person("Bartosz Gotowski", "rei.okei@gmail.com")
    ),
    invitations: List<Invitation> = listOf(
        Invitation(id = "1", fromEmail = "jan.kowalski@gmail.com", fromUserId = "user123", timestamp = System.currentTimeMillis()),
        Invitation(id = "2", fromEmail = "anna.nowak@gmail.com", fromUserId = "user456", timestamp = System.currentTimeMillis())
    ),
    onLogout: () -> Unit = {},
    onAddPartner: () -> Unit = {},
    onSelectPartner: (Person) -> Unit = {},
    onAcceptInvitation: (Invitation) -> Unit = {},
    onRejectInvitation: (Invitation) -> Unit = {},
    onNavigatePeople: () -> Unit = {},
    onNavigateJournal: () -> Unit = {},
    onNavigateAccount: () -> Unit = {}
) {
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

            Text(
                text = currentUser.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            UserSession.email.value?.let {
                Text(
                    text = it,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PartnersSection(
                partners = partners,
                onAddPartner = onAddPartner,
                onSelectPartner = onSelectPartner
            )

            Spacer(modifier = Modifier.height(24.dp))

            InvitationsSection(
                invitations = invitations,
                onAccept = onAcceptInvitation,
                onReject = onRejectInvitation
            )
        }
    }
}
