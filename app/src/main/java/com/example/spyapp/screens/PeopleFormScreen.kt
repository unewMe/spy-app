package com.example.spyapp.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.spyapp.models.Person
import com.example.spyapp.viewmodels.PersonViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonFormScreen(
    initialPerson: Person?,
    isEditMode: Boolean,
    onSave: (Person) -> Unit, // callback wywoływany po zatwierdzeniu formularza
    onClose: () -> Unit,
    viewModel: PersonViewModel = viewModel() // Add optional viewModel parameter with default
) {
    // Now use the provided viewModel instead of creating a new one
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Jeśli edytujemy, prefillujemy pola; przy dodawaniu pozostają puste
    var firstName by remember { mutableStateOf(initialPerson?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initialPerson?.lastName ?: "") }
    var email by remember { mutableStateOf(initialPerson?.email ?: "") }
    var photoUrl by remember { mutableStateOf(initialPerson?.photoUrl ?: "") }
    var isAvatarUploading by remember { mutableStateOf(false) }

    // Format daty: "yyyy-MM-dd"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var birthday by remember {
        mutableStateOf(
            if (initialPerson != null && initialPerson.birthdate != 0L) dateFormat.format(Date(initialPerson.birthdate))
            else ""
        )
    }
    
    // Camera launcher to take a photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            isAvatarUploading = true
            // Convert bitmap to ByteArray
            val stream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val imageData = stream.toByteArray()
            
            // Upload the image if we're in edit mode and have a person ID
            if (isEditMode && initialPerson != null && initialPerson.id.isNotEmpty()) {
                coroutineScope.launch {
                    viewModel.uploadAvatar(initialPerson.id, imageData) { success, url ->
                        isAvatarUploading = false
                        if (success && url != null) {
                            photoUrl = url
                        }
                    }
                }
            } else {
                // For new person, we'll store the image data and upload after person creation
                // This is a simplified version - in a real app, you might want to store the bitmap
                // and handle this case more elegantly
                isAvatarUploading = false
            }
        }
    }
    
    // Gallery launcher to pick an image
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isAvatarUploading = true
            try {
                // Convert URI to bitmap
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                
                // Convert bitmap to ByteArray
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val imageData = stream.toByteArray()
                
                // Upload the image if we're in edit mode and have a person ID
                if (isEditMode && initialPerson != null && initialPerson.id.isNotEmpty()) {
                    coroutineScope.launch {
                        viewModel.uploadAvatar(initialPerson.id, imageData) { success, url ->
                            isAvatarUploading = false
                            if (success && url != null) {
                                photoUrl = url
                            }
                        }
                    }
                } else {
                    // For new person, we'll store the image data and upload after person creation
                    // This is a simplified version - in a real app, you might want to store the bitmap
                    // and handle this case more elegantly
                    isAvatarUploading = false
                }
            } catch (e: Exception) {
                isAvatarUploading = false
            }
        }
    }
    
    // Image selection dialog state
    var showAvatarSourceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                title = { Text(if (isEditMode) "Edit person" else "Add person") },
                actions = {
                    // Po kliknięciu Save budujemy obiekt Person i wywołujemy onSave
                    TextButton(onClick = {
                        val parsedBirthday = try {
                            dateFormat.parse(birthday)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                        val person = Person(
                            id = initialPerson?.id ?: "", // przy edycji zachowujemy ID, przy dodawaniu puste
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            birthdate = parsedBirthday,
                            updatedAt = System.currentTimeMillis(),
                            photoUrl = photoUrl
                        )
                        onSave(person)
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar section
            if (photoUrl.isNotEmpty()) {
                // Display the current avatar
                Image(
                    painter = rememberAsyncImagePainter(photoUrl),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { showAvatarSourceDialog = true },
                    contentScale = ContentScale.Crop
                )
            } else {
                // Display a placeholder
                Icon(
                    Icons.Default.AccountBox,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { showAvatarSourceDialog = true },
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
            
            // Loading indicator for avatar upload
            if (isAvatarUploading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 8.dp)
                )
            }
            
            TextButton(onClick = { showAvatarSourceDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Change Photo")
            }
            
            // Image source selection dialog
            if (showAvatarSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showAvatarSourceDialog = false },
                    title = { Text("Choose image source") },
                    text = {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showAvatarSourceDialog = false
                                        cameraLauncher.launch()
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera"
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Take photo with camera")
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showAvatarSourceDialog = false
                                        galleryLauncher.launch("image/*")
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = "Gallery"
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Choose from gallery")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAvatarSourceDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            if (isEditMode) {
                Text("${firstName} ${lastName}", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = birthday,
                onValueChange = { birthday = it },
                label = { Text("Birthday (yyyy-MM-dd)") },
                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
