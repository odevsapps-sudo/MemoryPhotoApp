package com.odevs.photodiary.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.odevs.photodiary.datastore.NotificationPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = NotificationPreferences.isReminderEnabled(context).firstOrNull() ?: true
                if (enabled) {
                    val hour = NotificationPreferences.getReminderHour(context).firstOrNull() ?: 8
                    ReminderScheduler.scheduleDailyReminder(context, hour)
                }
            }
        }
    }
}
