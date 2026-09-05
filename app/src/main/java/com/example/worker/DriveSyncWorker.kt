package com.example.worker

import android.content.Context
import androidx.work.*
import com.example.auth.GoogleAuthManager
import com.example.data.local.AppDatabase
import com.example.data.remote.DriveAppDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class DriveSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val email = GoogleAuthManager.currentUserEmail()
            val db = AppDatabase.getDatabase(applicationContext, kotlinx.coroutines.CoroutineScope(Dispatchers.IO))
            val dao = db.umkmDao()
            // selalu update lokal dulu
            dao.insertMeta(com.example.data.local.entity.AppMetaEntity(key = "last_sync_local", value = System.currentTimeMillis().toString()))
            if (email == null) {
                // belum login Google — tetap sukses offline
                return@withContext Result.success()
            }
            // baca DB file dan gzip
            val dbFile = applicationContext.getDatabasePath("umkm_pos.db")
            if (!dbFile.exists()) {
                dao.insertMeta(com.example.data.local.entity.AppMetaEntity(key = "last_sync", value = System.currentTimeMillis().toString()))
                return@withContext Result.success()
            }
            // checkpoint WAL agar DB konsisten
            try {
                androidx.sqlite.db.SupportSQLiteDatabase::class.java
                // Room checkpoint via query
                db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").close()
            } catch (_: Exception) {}
            val bytes = dbFile.readBytes()
            val gzOut = ByteArrayOutputStream()
            GZIPOutputStream(gzOut).use { it.write(bytes) }
            val repo = DriveAppDataRepository(applicationContext)
            val id = repo.uploadBackup(email, gzOut.toByteArray())
            if (id != null) {
                dao.insertMeta(com.example.data.local.entity.AppMetaEntity(key = "last_sync", value = System.currentTimeMillis().toString()))
            }
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DriveSyncWorker", "sync failed", e)
            Result.retry()
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
