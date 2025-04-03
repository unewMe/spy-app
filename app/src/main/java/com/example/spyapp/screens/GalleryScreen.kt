package com.example.spyapp.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.spyapp.viewmodels.GalleryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

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

    // Launcher do aparatu – kontrakt zwraca Bitmapę zdjęcia
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            // Konwersja Bitmapy do ByteArray
            val stream = ByteArrayOutputStream()
            it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, stream)
            val data = stream.toByteArray()
            // Uruchom coroutine, aby wywołać upload zdjęcia
            coroutineScope.launch {
                galleryViewModel.uploadImage(data)
            }
        } ?: run {
            // Opcjonalnie obsłuż sytuację, gdy zdjęcie nie zostało wykonane
            Log.e("GalleryScreen", "No image captured")
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
            FloatingActionButton(onClick = { cameraLauncher.launch() }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
            }
        }
    ) { padding ->
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
                items(images) { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(128.dp)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
