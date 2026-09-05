package com.example.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

object GoogleAuthManager {
    // Panggil dari PengaturanScreen: login Google milik user (Drive App Data Folder scope)
    suspend fun signIn(context: Context, webClientId: String): Result<String> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val cm = CredentialManager.create(context)
            val result = cm.getCredential(context, request)
            val cred = GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCred = GoogleAuthProvider.getCredential(cred.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(firebaseCred).await()
            Result.success(cred.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun currentUserEmail(): String? = FirebaseAuth.getInstance().currentUser?.email
}
