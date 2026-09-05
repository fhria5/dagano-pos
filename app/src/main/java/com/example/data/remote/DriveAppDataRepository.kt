package com.example.data.remote

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Collections

class DriveAppDataRepository(private val context: Context) {

    private fun getDriveService(accountEmail: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccountName = accountEmail
        return Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Dagano POS")
            .build()
    }

    suspend fun uploadBackup(accountEmail: String, dbBytes: ByteArray, fileName: String = "dagano_pos.db.gz"): String? = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(accountEmail)
            // cari file lama di appDataFolder
            val list = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$fileName' and trashed=false")
                .setFields("files(id, name)").execute()
            val content = ByteArrayContent("application/gzip", dbBytes)
            val metadata = File().apply {
                name = fileName
                parents = listOf("appDataFolder")
                appProperties = mapOf("device" to android.os.Build.MODEL, "ts" to System.currentTimeMillis().toString())
            }
            val existing = list.files.firstOrNull()
            val result = if (existing != null) {
                drive.files().update(existing.id, metadata, content).setFields("id").execute()
            } else {
                drive.files().create(metadata, content).setFields("id").execute()
            }
            result.id
        } catch (e: Exception) {
            android.util.Log.e("DriveAppData", "upload failed", e)
            null
        }
    }

    suspend fun downloadBackup(accountEmail: String, fileName: String = "dagano_pos.db.gz"): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(accountEmail)
            val list = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$fileName' and trashed=false")
                .setFields("files(id, modifiedTime)").execute()
            val file = list.files.firstOrNull() ?: return@withContext null
            val out = ByteArrayOutputStream()
            drive.files().get(file.id).executeMediaAndDownloadTo(out)
            out.toByteArray()
        } catch (e: Exception) {
            android.util.Log.e("DriveAppData", "download failed", e)
            null
        }
    }

    suspend fun getLastModified(accountEmail: String, fileName: String = "dagano_pos.db.gz"): Long? = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(accountEmail)
            val list = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$fileName' and trashed=false")
                .setFields("files(modifiedTime)").execute()
            list.files.firstOrNull()?.modifiedTime?.value
        } catch (_: Exception) { null }
    }
}
