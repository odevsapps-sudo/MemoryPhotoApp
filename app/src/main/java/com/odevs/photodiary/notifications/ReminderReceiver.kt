package com.odevs.photodiary.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.odevs.photodiary.R
import com.odevs.photodiary.datastore.NotificationPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.odevs.photodiary.notifications.*

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Értesítés létrehozása
        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("PhotoDiary")
            .setContentText("Don't forget to upload a photo today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(1001, notification)
        }

        // 🔁 Következő nap újraütemezése
        CoroutineScope(Dispatchers.IO).launch {
            val hour = NotificationPreferences.getReminderHour(context).firstOrNull() ?: 8
            ReminderScheduler.scheduleDailyReminder(context, hour)
        }
    }
}
