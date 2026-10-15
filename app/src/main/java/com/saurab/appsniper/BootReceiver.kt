package com.saurab.appsniper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            7, TimeUnit.DAYS, 1, TimeUnit.DAYS
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(true)
                .setRequiresDeviceIdle(true)
                .build()
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "app_sniper_cleanup",
            ExistingPeriodicWorkPolicy.UPDATE,
            cleanupRequest
        )
    }
}
