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

class RecordingsViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _recordings = MutableStateFlow<List<String>>(emptyList())
    val recordings: StateFlow<List<String>> = _recordings

    init {
        refreshRecordings()
    }

    fun refreshRecordings() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                val listResult =
                    storage.reference.child("users/$userId/recordings").listAll().await()
                val downloadUrls = listResult.items.map { ref ->
                    ref.downloadUrl.await().toString()
                }
                _recordings.value = downloadUrls
            } catch (e: Exception) {
                Log.e("RecordingsViewModel", "Error loading recordings: ${e.message}")
            }
        }
    }

    suspend fun uploadRecording(data: ByteArray) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val filename = "${System.currentTimeMillis()}.3gp"
            val ref = storage.reference.child("users/$userId/recordings/$filename")
            ref.putBytes(data).await()
            refreshRecordings()
        } catch (e: Exception) {
            Log.e("RecordingsViewModel", "Error uploading recording: ${e.message}")
        }
    }
}
