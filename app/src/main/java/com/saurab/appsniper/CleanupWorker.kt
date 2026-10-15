package com.saurab.appsniper

import android.content.Context
import android.content.pm.PackageManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class CleanupWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        applicationContext.cacheDir.deleteRecursively()
        applicationContext.externalCacheDir?.deleteRecursively()

        val pm = applicationContext.packageManager
        pm.getInstalledApplications(PackageManager.GET_META_DATA).forEach { appInfo ->
            try {
                pm.deleteApplicationCacheFiles(appInfo.packageName, null)
            } catch (_: Exception) { }
        }
        return Result.success()
    }
}
