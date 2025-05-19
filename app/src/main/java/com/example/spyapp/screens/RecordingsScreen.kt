package com.example.spyapp.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spyapp.viewmodels.RecordingsViewModel
import kotlinx.coroutines.launch
import java.io.InputStream
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsScreen(
    onBack: () -> Unit
) {
    val recordingsViewModel: RecordingsViewModel = viewModel()
    val recordings by recordingsViewModel.recordings.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val audioUri: Uri? = result.data?.data
            if (audioUri != null) {
                try {
                    val inputStream: InputStream? =
                        context.contentResolver.openInputStream(audioUri)
                    val data = inputStream?.readBytes()
                    if (data != null) {
                        coroutineScope.launch {
                            recordingsViewModel.uploadRecording(data)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RecordingsScreen", "Error reading audio file: ${e.message}")
                }
            } else {
                Log.e("RecordingsScreen", "No audio URI received")
            }
        }
    }

    val recordIntent = Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (recordIntent.resolveActivity(context.packageManager) != null) {
                    audioLauncher.launch(recordIntent)
                } else {
                    Toast.makeText(context, "No audio recording app available", Toast.LENGTH_SHORT)
                        .show()
                }
            }) {
                Icon(Icons.Filled.Mic, contentDescription = "Record Audio")
            }
        }
    ) { padding ->
        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No recordings found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(recordings) { recordingUrl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {

                                val playIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(recordingUrl.toUri(), "audio/*")
                                }
                                context.startActivity(playIntent)
                            },
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play Recording")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recording - ${recordings.indexOf(recordingUrl) + 1}",
                        )
                    }
                }
            }
        }
    }
}
