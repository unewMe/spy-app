package com.example.spyapp.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.spyapp.models.Person
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

@Composable
fun PersonCard(person: Person, onClick: (Person) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(person) },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Photo container that fills the whole width
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                if (person.photoUrl.isNotEmpty()) {
                    // Display actual image when available
                    Image(
                        painter = rememberAsyncImagePainter(person.photoUrl),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Display a square placeholder with gray color
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                }
            }
            
            // Text content below the image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 8.dp)
            ) {
                Text(
                    person.name, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis, 
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.titleMedium
                )
                
                val updatedText = formatUpdatedTime(person.updatedAt)
                Text(
                    text = updatedText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = Color.DarkGray)
                )
            }
        }
    }
}

@Composable
private fun formatUpdatedTime(timestamp: Long): String {
    if (timestamp <= 0) return "Updated: Unknown"
    
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val days = diff / (24 * 60 * 60 * 1000)
    
    return when {
        days == 0L -> "Updated today"
        days == 1L -> "Updated yesterday"
        days < 7 -> "Updated $days days ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            "Updated ${sdf.format(Date(timestamp))}"
        }
    }
}
