package com.example.spyapp.screens

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.spyapp.viewmodels.GalleryViewModel
import kotlinx.coroutines.launch
import java.io.InputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onBack: () -> Unit
) {
    val galleryViewModel: GalleryViewModel = viewModel()
    val images by galleryViewModel.images.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    
    var showFullScreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    
    
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    
    var isUploading by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    
    
    
    
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
    
    
    val cameraFullResLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && photoUri != null) {
            try {
                
                val inputStream: InputStream? = context.contentResolver.openInputStream(photoUri!!)
                inputStream?.use { stream ->
                    
                    val imageBytes = stream.readBytes()
                    
                    
                    isUploading = true
                    
                    
                    coroutineScope.launch {
                        try {
                            galleryViewModel.uploadImage(imageBytes)
                        } finally {
                            
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

    
    if (showFullScreen && images.isNotEmpty()) {
        FullScreenPhotoViewer(
            images = images,
            initialPage = selectedImageIndex,
            onDismiss = { showFullScreen = false },
            onDelete = { imageUrl ->
                
                isDeleting = true
                coroutineScope.launch {
                    try {
                        galleryViewModel.deleteImage(imageUrl)
                    } finally {
                        isDeleting = false
                        
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
            
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                
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
                
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
            
            
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
    
    
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
                            onDismiss() 
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
