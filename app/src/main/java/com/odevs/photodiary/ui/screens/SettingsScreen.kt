package com.odevs.photodiary.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.odevs.photodiary.datastore.NotificationPreferences
import com.odevs.photodiary.notifications.ReminderScheduler
import com.odevs.photodiary.screens.SoftYellow
import kotlinx.coroutines.launch
import com.odevs.photodiary.ui.LanguageProvider

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val language = LanguageProvider.language
    val reminderHour by NotificationPreferences.getReminderHour(context).collectAsStateWithLifecycle(initialValue = 8)
    val isReminderEnabledState by NotificationPreferences.isReminderEnabled(context).collectAsStateWithLifecycle(initialValue = true)
    var isReminderEnabled by remember { mutableStateOf(isReminderEnabledState) }

    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (language == "hu") "Értesítési engedély szükséges." else "Notification permission is needed."
                    )
                }
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftYellow)
                .padding(24.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Column {
                Text(
                    text = if (language == "hu") "Beállítások" else "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (language == "hu") "Napi emlékeztető engedélyezése" else "Enable daily reminder",
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = isReminderEnabled,
                        onCheckedChange = { enabled ->
                            isReminderEnabled = enabled
                            coroutineScope.launch {
                                NotificationPreferences.setReminderEnabled(context, enabled)
                                if (enabled) {
                                    ReminderScheduler.scheduleDailyReminder(context, reminderHour)
                                } else {
                                    ReminderScheduler.cancelReminder(context)
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (language == "hu") "Emlékeztető időpontja:" else "Daily reminder time:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                val hours = (6..22).toList().chunked(5)
                hours.forEach { rowHours ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowHours.forEach { hour ->
                            val hourLabel = hour.toString() + ":00"
                            if (hour == reminderHour) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            NotificationPreferences.saveReminderHour(context, hour)

                                            if (isReminderEnabled) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    val permissionGranted = ContextCompat.checkSelfPermission(
                                                        context, Manifest.permission.POST_NOTIFICATIONS
                                                    ) == PackageManager.PERMISSION_GRANTED

                                                    if (!permissionGranted) {
                                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    }
                                                }
                                                ReminderScheduler.scheduleDailyReminder(context, hour)
                                            }

                                            snackbarHostState.showSnackbar(
                                                (if (language == "hu") "Emlékeztető beállítva: " else "Reminder set for: ") + hourLabel
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.DarkGray,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(text = hourLabel)
                                }
                            } else {
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            NotificationPreferences.saveReminderHour(context, hour)
                                            if (isReminderEnabled) {
                                                ReminderScheduler.scheduleDailyReminder(context, hour)
                                            }

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                val permissionGranted = ContextCompat.checkSelfPermission(
                                                    context, Manifest.permission.POST_NOTIFICATIONS
                                                ) == PackageManager.PERMISSION_GRANTED

                                                if (!permissionGranted) {
                                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                            }

                                            snackbarHostState.showSnackbar(
                                                (if (language == "hu") "Emlékeztető beállítva: " else "Reminder set for: ") + hourLabel
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color.Gray
                                    )
                                ) {
                                    Text(text = hourLabel)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isTaskNotificationEnabled by NotificationPreferences.isTaskNotificationEnabled(context)
                .collectAsStateWithLifecycle(initialValue = true)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (language == "hu") "Feladat értesítések engedélyezése" else "Enable task notifications",
                    fontSize = 18.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isTaskNotificationEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            NotificationPreferences.setTaskNotificationEnabled(context, enabled)
                            snackbarHostState.showSnackbar(
                                if (enabled)
                                    if (language == "hu") "Feladat értesítések bekapcsolva" else "Task notifications enabled"
                                else
                                    if (language == "hu") "Feladat értesítések kikapcsolva" else "Task notifications disabled"
                            )
                        }
                    }
                )
            }


            Spacer(modifier = Modifier.height(16.dp))


            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("odevs.apps@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "PhotoDiary Contact")
                }
                navController.context.startActivity(Intent.createChooser(intent, "Send email"))
            }) {
                Text(
                    text = if (language == "hu") "Kapcsolat: odevs.apps@gmail.com" else "Contact us: odevs.apps@gmail.com",
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }
    }
}
