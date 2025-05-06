package com.example.spyapp.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.spyapp.viewmodels.RecordingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    val recordingsViewModel: RecordingsViewModel = viewModel()
    val recordings by recordingsViewModel.recordings.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val audioUri: Uri? = result.data?.data
            if(audioUri != null) {
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(audioUri)
                    val data = inputStream?.readBytes()
                    if(data != null) {
                        coroutineScope.launch {
                            recordingsViewModel.uploadRecording(data)
                        }
                    }
                } catch(e: Exception) {
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
                    Toast.makeText(context, "No audio recording app available", Toast.LENGTH_SHORT).show()
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
                                    setDataAndType(Uri.parse(recordingUrl), "audio/*")
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
