package com.example.spyapp.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers

class GoogleSignOutUtils {

    companion object {
        fun doGoogleSignOut(context: Context, logout: () -> Unit) {


            val credentialManager = CredentialManager.create(context)

            try {
                val clearRequest = ClearCredentialStateRequest()
                GlobalScope.launch (Dispatchers.Main){
                    credentialManager.clearCredentialState(clearRequest)
                }
              } catch (e: ClearCredentialException) {
                Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
              }

            Firebase.auth.signOut()
            logout.invoke()
        }
    }
}