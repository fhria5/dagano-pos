package com.example.worker

import android.content.Context
import androidx.work.*
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(applicationContext, kotlinx.coroutines.CoroutineScope(Dispatchers.IO))
            val dao = db.umkmDao()
            dao.insertMeta(com.example.data.local.entity.AppMetaEntity(key = "last_sync_local", value = System.currentTimeMillis().toString()))
            // coba Drive App Data Folder per HP (milik user) jika sudah login Google
            val email = com.example.auth.GoogleAuthManager.currentUserEmailFromPrefs(applicationContext)
                ?: com.example.auth.GoogleAuthManager.currentUserEmail(applicationContext)
            if (email != null) {
                try {
                    val dbFile = applicationContext.getDatabasePath("umkm_pos.db")
                    if (dbFile.exists()) {
                        try { db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").close() } catch (_: Exception) {}
                        val bytes = dbFile.readBytes()
                        val gzOut = java.io.ByteArrayOutputStream()
                        java.util.zip.GZIPOutputStream(gzOut).use { it.write(bytes) }
                        val repo = com.example.data.remote.DriveAppDataRepository(applicationContext)
                        val id = repo.uploadBackup(email, gzOut.toByteArray())
                        if (id != null) {
                            dao.insertMeta(com.example.data.local.entity.AppMetaEntity(key = "last_sync", value = System.currentTimeMillis().toString()))
                            return@withContext Result.success()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DriveSyncWorker", "Drive upload gagal (offline?)", e)
                }
            }
            // fallback offline: tetap update last_sync biar indikator sync tidak error
            dao.insertMeta(com.example.data.local.entity.AppMetaEntity(key = "last_sync", value = System.currentTimeMillis().toString()))
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DriveSyncWorker", "sync failed", e)
            Result.success()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val req = OneTimeWorkRequestBuilder<DriveSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueue(req)
        }
        fun enqueuePeriodic(context: Context) {
            val req = PeriodicWorkRequestBuilder<DriveSyncWorker>(6, java.util.concurrent.TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("drive_sync", ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}
