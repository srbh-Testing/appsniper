package com.saurab.appsniper

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.lang.reflect.Proxy

class CleanupWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        clearOwnCacheAndTemp()
        clearAllAppsCache()
        clearThumbnailCache()
        clearStaleApkInstallers()
        return Result.success()
    }

    // 1. Apna khud ka cache/temp
    private fun clearOwnCacheAndTemp() {
        applicationContext.cacheDir.deleteRecursively()
        applicationContext.externalCacheDir?.deleteRecursively()
    }

    // 2. Har installed app ka cache -- hidden API, reflection se call karte hain
    //    taaki compile-time error na aaye (iPackageDataObserver bhi hidden hai,
    //    isliye uska bhi dynamic proxy banate hain, direct reference nahi)
    private fun clearAllAppsCache() {
        try {
            val pm = applicationContext.packageManager
            val observerClass = Class.forName("android.content.pm.IPackageDataObserver")
            val proxyObserver = Proxy.newProxyInstance(
                observerClass.classLoader,
                arrayOf(observerClass)
            ) { _, _, _ -> null }

            val method = pm.javaClass.getMethod(
                "deleteApplicationCacheFiles",
                String::class.java,
                observerClass
            )

            pm.getInstalledApplications(PackageManager.GET_META_DATA).forEach { appInfo ->
                try {
                    method.invoke(pm, appInfo.packageName, proxyObserver)
                } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }

    // 3. Thumbnail cache (photos/videos ke generated thumbnails)
    private fun clearThumbnailCache() {
        try {
            val base = Environment.getExternalStorageDirectory()
            File(base, "Android/data/.thumbnails").let { if (it.exists()) it.deleteRecursively() }
            File(base, "DCIM/.thumbnails").let { if (it.exists()) it.deleteRecursively() }
        } catch (_: Exception) { }
    }

    // 4. Downloads folder mein pade stale .apk installer files
    private fun clearStaleApkInstallers() {
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            downloadDir?.listFiles { f -> f.extension.equals("apk", ignoreCase = true) }
                ?.forEach { it.delete() }
        } catch (_: Exception) { }
    }
}
