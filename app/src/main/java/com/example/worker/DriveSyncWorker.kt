package com.example.worker

import android.content.Context
import androidx.work.*
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Stub: upload DB ke Drive App Data Folder milik user
            // TODO: real Drive API: files.create parents=["appDataFolder"] + gzip DB
            // Untuk v1, cuma update last_sync timestamp (offline-first indikator)
            val db = AppDatabase.getDatabase(applicationContext, kotlinx.coroutines.CoroutineScope(Dispatchers.IO))
            val dao = db.umkmDao()
            dao.insertMeta(com.example.data.local.entity.AppMetaEntity(key = "last_sync", value = System.currentTimeMillis().toString()))
            Result.success()
        } catch (e: Exception) {
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
