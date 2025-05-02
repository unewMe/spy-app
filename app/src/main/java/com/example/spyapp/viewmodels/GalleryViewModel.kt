package com.example.spyapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GalleryViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images: StateFlow<List<String>> = _images

    init {
        refreshGallery()
    }

    private fun refreshGallery() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                // Pobierz wszystkie elementy z folderu "gallery"
                val listResult = storage.reference.child("users/$userId/gallery").listAll().await()
                val downloadUrls = listResult.items.map { ref ->
                    ref.downloadUrl.await().toString()
                }
                _images.value = downloadUrls
            } catch (e: Exception) {
                Log.e("GalleryViewModel", "Error loading gallery images: ${e.message}")
            }
        }
    }

    suspend fun uploadImage(data: ByteArray) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val filename = "${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child("users/$userId/gallery/$filename")
            ref.putBytes(data).await()
            refreshGallery() // Po udanym uploadzie odśwież galerię
        } catch (e: Exception) {
            Log.e("GalleryViewModel", "Error uploading image: ${e.message}")
        }
    }
    
    suspend fun deleteImage(imageUrl: String) {
        try {
            // Extract the file path from the download URL
            // The URL format is typically: https://firebasestorage.googleapis.com/...
            val userId = auth.currentUser?.uid ?: return
            
            // First find the reference by URL
            val storageRefs = storage.reference
                .child("users/$userId/gallery")
                .listAll()
                .await()
                .items
            
            // Find the matching reference with the same download URL
            for (ref in storageRefs) {
                val downloadUrl = ref.downloadUrl.await().toString()
                if (downloadUrl == imageUrl) {
                    // Delete the file when found
                    ref.delete().await()
                    break
                }
            }
            
            refreshGallery() // Refresh gallery after deletion
        } catch (e: Exception) {
            Log.e("GalleryViewModel", "Error deleting image: ${e.message}")
        }
    }
}
