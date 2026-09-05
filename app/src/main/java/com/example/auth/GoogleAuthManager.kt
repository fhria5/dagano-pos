package com.example.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
object GoogleAuthManager {
    // Offline-first: tanpa Firebase (dummy google-services.json) — login Google hanya untuk Drive App Data Folder
    // Untuk v1, stub saja biar tidak FC — nanti ganti ke Firebase jika sudah punya google-services.json real
    suspend fun signIn(context: Context, webClientId: String): Result<String> {
        return try {
            if (webClientId.startsWith("xxx")) {
                return Result.failure(IllegalStateException("Isi WEB_CLIENT_ID dulu di .env"))
            }
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
            // Simpan email ke prefs (tanpa Firebase) — offline 100%
            context.getSharedPreferences("dagano_auth", Context.MODE_PRIVATE)
                .edit().putString("google_email", cred.id).apply()
            Result.success(cred.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        // stub
    }

    fun currentUserEmail(): String? = try {
        // coba baca dari prefs dulu (offline), baru Firebase jika ada
        null // stub offline — tidak pakai Firebase biar tidak FC dengan dummy google-services.json
    } catch (_: Throwable) { null }
}
