package com.example.spyapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spyapp.models.Invitation
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InvitationsSection(
    invitations: List<Invitation>,
    onAccept: (Invitation) -> Unit,
    onReject: (Invitation) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Invitations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (invitations.isNotEmpty()) {
                Badge { Text(text = "${invitations.size}") }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(invitations) { invitation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("From: ${invitation.fromEmail}", fontWeight = FontWeight.SemiBold)
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            Text(
                                text = dateFormat.format(Date(invitation.timestamp)),
                                fontSize = 12.sp,
                            )
                        }
                        Row {
                            TextButton(onClick = { onAccept(invitation) }) { Text("Accept") }
                            TextButton(onClick = { onReject(invitation) }) { Text("Reject") }
                        }
                    }
                }
            }
        }
    }
}
