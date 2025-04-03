package com.example.spyapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spyapp.models.Person

@Composable
fun PersonCard(person: Person, onClick: (Person) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick(person) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7E9EB))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Icon(Icons.Default.ChangeHistory, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Gray)
            Row(horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.CheckBox, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Circle, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(person.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
            Text(person.updated, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
        }
    }
}
