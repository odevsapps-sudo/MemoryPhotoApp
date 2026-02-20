package com.odevs.photodiary

import android.app.AlarmManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.odevs.photodiary.screens.*
import com.odevs.photodiary.ui.theme.MemoryPhotoAppTheme
import com.odevs.photodiary.viewmodel.PhotoViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.odevs.photodiary.ui.screens.HomeScreen
import com.odevs.photodiary.screens.WeeklyScreen
import java.time.LocalDate
import com.odevs.photodiary.screens.CollagePreviewScreen
import com.odevs.photodiary.ui.screens.SettingsScreen
import java.io.File
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import com.google.android.gms.common.api.Scope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.odevs.photodiary.notifications.ReminderScheduler
import com.odevs.photodiary.storage.LanguageManager
import com.odevs.photodiary.ui.LanguageProvider.language
import com.odevs.photodiary.ui.screens.LocalLanguage
import com.odevs.photodiary.util.exportDiaryToPdf
import com.odevs.photodiary.util.savePdfToDownloads
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.services.drive.DriveScopes
import com.odevs.photodiary.notifications.ReminderReceiver
import java.time.LocalDateTime
import java.time.ZoneId

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ esetén: értesítési engedély kérése
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }


        ReminderScheduler.createNotificationChannel(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
            .requestIdToken("354602803455-pf8maic2vsl4uacjrt2cdh0nkcmakkg1.apps.googleusercontent.com")
            .build()


        val googleSignInClient = GoogleSignIn.getClient(this@MainActivity, gso)


        setContent {
            MemoryPhotoAppTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                val viewModel: PhotoViewModel = hiltViewModel()
                val context = LocalContext.current


                var language: String by remember { mutableStateOf(LanguageManager.loadLanguage(context)) }
                CompositionLocalProvider(LocalLanguage provides language) {
                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel,
                        onLanguageChange = { newLang ->
                            language = newLang
                            LanguageManager.saveLanguage(context, newLang)
                        }
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Daily Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows daily reminder to upload a photo"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: PhotoViewModel,
    onLanguageChange: (String) -> Unit
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, onLanguageChange = onLanguageChange)
         }
        composable("upload/{selectedDate}") { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: LocalDate.now().toString()
            UploadPhotoScreen(navController, selectedDate)
        }
        composable("weekly") {
            WeeklyScreen(
                viewModel = hiltViewModel(),
                navController = navController
            )
        }
        composable("collagePreview/{startIndex}",
            arguments = listOf(navArgument("startIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val startIndex = backStackEntry.arguments?.getInt("startIndex") ?: 0
            CollagePreviewScreen(navController, startIndex)
        }

        composable("monthly") { MonthlyScreen(navController) }
        composable("collageSelection") { CollageSelectionScreen(navController) }
        composable("collageResult") { CollageResultScreen(navController = navController) }
        //composable("collagePreview") { CollagePreviewScreen(navController) }
        composable("savedCollages") { SavedCollagesScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("weeklyPreview") { WeeklyPreviewScreen(navController, viewModel) }
        composable("diary") {
            DiaryScreen(
                viewModel = viewModel,
                onNavigateHome = { navController.navigate("home") },
                onExportPDF = { start, end, context ->
                    val tempFile = File(context.cacheDir, "naplo_${start}_to_${end}.pdf")

                    val textMap = generateSequence(start) { it.plusDays(1) }
                        .takeWhile { it <= end }
                        .associateWith { viewModel.loadText(context, it) ?: "" }

                    val imageMap = viewModel.imageMap.value

                    val success = exportDiaryToPdf(context, imageMap, textMap, start, end, tempFile)

                    if (success) {
                        val publicSuccess = savePdfToDownloads(context, tempFile, "naplom_${start}_to_${end}.pdf")
                        if (publicSuccess) {
                            Toast.makeText(context, "PDF elmentve a Letöltések közé!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Mentés sikerült, de nem került nyilvános mappába.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Hiba a PDF mentésekor", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
        composable("todo_screen/{date}") { backStackEntry ->
            val dateArg = backStackEntry.arguments?.getString("date")
            val parsedDate = dateArg?.let { LocalDate.parse(it) } ?: LocalDate.now()
            WeeklyTodoScreen()
        }
    }
}
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Task Notifications"
        val descriptionText = "Notifications for upcoming tasks"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("TASK_REMINDER_CHANNEL", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}




