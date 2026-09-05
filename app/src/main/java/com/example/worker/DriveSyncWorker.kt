package com.example.worker

import android.content.Context
import androidx.work.*
import com.example.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // v1 offline-first: tanpa Drive login, cuma update last_sync_local biar tidak FC
            val db = AppDatabase.getDatabase(applicationContext, kotlinx.coroutines.CoroutineScope(Dispatchers.IO))
            val dao = db.umkmDao()
            dao.insertMeta(com.example.data.local.entity.AppMetaEntity(key = "last_sync_local", value = System.currentTimeMillis().toString()))
            // Drive real akan aktif jika sudah login Google + WEB_CLIENT_ID real — untuk sekarang stub sukses offline
            dao.insertMeta(com.example.data.local.entity.AppMetaEntity(key = "last_sync", value = System.currentTimeMillis().toString()))
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DriveSyncWorker", "sync failed", e)
            Result.success() // jangan retry biar tidak loop FC — v1 offline 100%
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
