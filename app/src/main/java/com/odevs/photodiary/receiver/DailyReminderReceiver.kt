package com.memoryphotoapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.odevs.photodiary.R
import com.odevs.photodiary.datastore.NotificationPreferences
import com.odevs.photodiary.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val type = intent?.getStringExtra("type")
        val message = when (type) {
            "task" -> intent.getStringExtra("task_title") ?: "You have a task!"
            "daily" -> "Don't forget to upload a photo today!"
            else -> "Reminder from PhotoDiary"
        }

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("PhotoDiary")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(1001, notification)
        }

        if (type == "daily") {
            CoroutineScope(Dispatchers.IO).launch {
                val hour = NotificationPreferences.getReminderHour(context).firstOrNull() ?: 8
                ReminderScheduler.scheduleDailyReminder(context, hour)
            }
        }
    }
}
