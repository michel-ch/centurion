package com.century.app.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.century.app.CenturyApp
import com.century.app.MainActivity
import com.century.app.R
import com.century.app.data.repository.CenturyRepository
import com.century.app.domain.model.TrainingProgramData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CenturyRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val profile = repository.getProfileOnce() ?: return Result.success()
        val currentDay = profile.currentDay
        val dayData = TrainingProgramData.getDayForProgram(currentDay)

        val (title, message) = if (dayData != null) {
            val (week, day) = dayData
            if (day.isRestDay) {
                "Recovery Day" to "Recovery day — stretch, hydrate, and sleep well tonight."
            } else {
                "Day $currentDay: ${day.label}" to "Day $currentDay: ${day.label} is waiting. Let's go."
            }
        } else {
            "CENTURY" to "Time to train!"
        }

        showNotification(title, message)
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CenturyApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 100
        const val WORK_NAME = "century_daily_reminder"

        fun schedule(context: Context, reminderTime: String, enabled: Boolean) {
            val workManager = WorkManager.getInstance(context)

            if (!enabled) {
                workManager.cancelUniqueWork(WORK_NAME)
                return
            }

            val parts = reminderTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
            }

            val delay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
