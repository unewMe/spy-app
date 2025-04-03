package com.example.spyapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spyapp.Person

@Composable
fun PartnersSection(
    partners: List<Person>,
    onAddPartner: () -> Unit,
    onSelectPartner: (Person) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = "Partners",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onAddPartner) {
            Text("+")
        }
    }
    LazyColumn {
        items(partners) { partner ->
            TextButton(
                onClick = { onSelectPartner(partner) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(partner.name, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
