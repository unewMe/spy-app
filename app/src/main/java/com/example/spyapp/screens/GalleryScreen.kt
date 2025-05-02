package com.example.spyapp.screens

import android.content.ContentValues
import android.graphics.Bitmap // Keep Bitmap for potential future use or other parts of the file
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.spyapp.viewmodels.GalleryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
// Removed ByteArrayOutputStream as we are not compressing
import java.io.InputStream
// Removed File and FileOutputStream as we are not using temp files

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    val galleryViewModel: GalleryViewModel = viewModel()
    val images by galleryViewModel.images.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for full-screen photo viewer
    var showFullScreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    
    // Create a URI for saving the full-resolution photo
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Separate states for uploads and deletions
    var isUploading by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    
    // Removed the fixImageRotation function as it's no longer needed
    
    // Create URI for the full-resolution photo
    fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        
        return context.contentResolver.insert(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            },
            contentValues
        )
    }
    
    // Full-resolution camera launcher (captures to URI)
    val cameraFullResLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && photoUri != null) {
            try {
                // Get the InputStream directly from the URI
                val inputStream: InputStream? = context.contentResolver.openInputStream(photoUri!!)
                inputStream?.use { stream ->
                    // Read the raw bytes directly from the stream
                    val imageBytes = stream.readBytes()
                    
                    // Set uploading state to true before starting the upload
                    isUploading = true
                    
                    // Upload the raw image bytes without compression or rotation fix
                    coroutineScope.launch {
                        try {
                            galleryViewModel.uploadImage(imageBytes)
                        } finally {
                            // Set uploading state to false when upload completes or fails
                            isUploading = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GalleryScreen", "Error reading photo from URI: ${e.message}")
                isUploading = false
            }
        } else {
            Log.e("GalleryScreen", "Failed to take photo or URI is null")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    photoUri = createImageUri()
                    photoUri?.let { cameraFullResLauncher.launch(it) }
                }
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (images.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No images found")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 128.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(images.size) { index ->
                        val imageUrl = images[index]
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(128.dp)
                                .padding(4.dp)
                                .clickable {
                                    selectedImageIndex = index
                                    showFullScreen = true
                                }
                        )
                    }
                }
            }
            
            // Show loading overlay when uploading
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .clickable(enabled = false) { /* Prevent clicks during upload */ },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(200.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Uploading image...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
            
            // Show loading overlay when deleting
            if (isDeleting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .clickable(enabled = false) { /* Prevent clicks during deletion */ },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(200.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Deleting image...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Screen Photo Viewer
    if (showFullScreen && images.isNotEmpty()) {
        FullScreenPhotoViewer(
            images = images,
            initialPage = selectedImageIndex,
            onDismiss = { showFullScreen = false },
            onDelete = { imageUrl ->
                // Set loading state to show feedback during deletion
                isDeleting = true
                coroutineScope.launch {
                    try {
                        galleryViewModel.deleteImage(imageUrl)
                    } finally {
                        isDeleting = false
                        // After deletion, close the full-screen viewer
                        showFullScreen = false
                    }
                }
            }
        )
    }
}

@Composable
fun FullScreenPhotoViewer(
    images: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { images.size }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Pager for swiping between images
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(images[page]),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            // Top action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Delete button
                IconButton(
                    onClick = { showDeleteConfirmDialog = true }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Close button
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
            
            // Image counter
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmDialog && images.isNotEmpty() && pagerState.currentPage < images.size) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(images[pagerState.currentPage])
                        showDeleteConfirmDialog = false
                        if (images.size <= 1) {
                            onDismiss() // Close the viewer if this was the last image
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
