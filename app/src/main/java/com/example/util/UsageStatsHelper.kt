package com.example.util

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar

object UsageStatsHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Throwable) {
            false
        }
    }

    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Reads foreground usage time for the past 7 days directly from Android's UsageStatsManager,
     * and calculates the daily average in hours.
     */
    fun getWeeklyDailyAverageScreenHours(context: Context): Float? {
        return try {
            if (!hasUsageStatsPermission(context)) return null

            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return null

            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val startTime = calendar.timeInMillis

            val stats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            if (stats.isNullOrEmpty()) return null

            var totalTimeMillis = 0L
            for ((_, usage) in stats) {
                totalTimeMillis += usage.totalTimeInForeground
            }

            if (totalTimeMillis <= 0L) return null

            val totalHours = totalTimeMillis / (1000f * 60f * 60f)
            val dailyAvg = totalHours / 7f
            // Return rounded to 1 decimal place
            ((dailyAvg * 10).toInt() / 10f).coerceIn(0.1f, 18.0f)
        } catch (e: Throwable) {
            null
        }
    }
}
