package com.odevs.photodiary.screens


import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.odevs.photodiary.ui.LanguageProvider
import com.odevs.photodiary.ui.components.BubbleButton
import com.odevs.photodiary.ui.components.SmallBubbleButton
import com.odevs.photodiary.viewmodel.PhotoViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

val SoftYellow = Color(0xFFFEC868)
val pastelColors = listOf(
    Color(0xFFFFD3B6), Color(0xFFDCEDC8), Color(0xFFFFF9C4), Color(0xFFB2EBF2), Color(0xFFF8BBD0), Color(0xFFD1C4E9)
)

@Composable
fun WeeklyScreen(
    viewModel: PhotoViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val language by remember { derivedStateOf { LanguageProvider.language } }
    val title = if (language == "hu") "Heti napló" else "Weekly Diary"
    val previousWeek = if (language == "hu") "Előző hét" else "Previous Week"
    val nextWeek = if (language == "hu") "Következő hét" else "Next Week"
    val noImage = if (language == "hu") "Nincs kép" else "No image"
    val homepage = if (language == "hu") "Főoldal" else "Homepage"
    val monthlyView = if (language == "hu") "Havi nézet" else "Monthly View"
    var currentMonday by remember { mutableStateOf(getMonday(LocalDate.now())) }
    val weekDates by remember { derivedStateOf { (0..6).map { currentMonday.plusDays(it.toLong()) } } }
    val imageMap by viewModel.imageMap.collectAsState(initial = emptyMap())
    var showDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()
    val images by remember(imageMap, weekDates, refreshTrigger) {
        derivedStateOf { weekDates.map { imageMap[it] ?: Uri.EMPTY } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftYellow)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (language == "hu") "Heti napló" else "Weekly Diary", fontSize = 26.sp)
        val dateRangeText = "${currentMonday.toString()} – ${currentMonday.plusDays(6).toString()}"
        Text(dateRangeText, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SmallBubbleButton(
                text = if (language == "hu") "Előző hét" else "Previous week",
                color = Color(0xFFFDA769),
                onClick = { currentMonday = currentMonday.minusWeeks(1) }
            )
            SmallBubbleButton(
                text = if (language == "hu") "Következő hét" else "Next week",
                color = Color(0xFFABC270),
                onClick = { currentMonday = currentMonday.plusWeeks(1) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            for (i in 0 until 7 step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DayCard(
                        date = weekDates[i],
                        imageUri = images.getOrNull(i) ?: Uri.EMPTY,
                        onClick = {
                            selectedIndex = i
                            showDialog = true
                        },
                        hasText = viewModel.hasText(LocalContext.current, weekDates[i]),
                        hasAudio = viewModel.hasAudio(LocalContext.current, weekDates[i]),
                        hasVideo = viewModel.hasVideo(LocalContext.current, weekDates[i]),
                        language = language
                    )

                    if (i + 1 < 7) {
                        DayCard(
                            date = weekDates[i + 1],
                            imageUri = images.getOrNull(i + 1) ?: Uri.EMPTY,
                            onClick = {
                                selectedIndex = i + 1
                                showDialog = true
                            },
                            hasText = viewModel.hasText(LocalContext.current, weekDates[i + 1]),
                            hasAudio = viewModel.hasAudio(LocalContext.current, weekDates[i + 1]),
                            hasVideo = viewModel.hasVideo(LocalContext.current, weekDates[i + 1]),
                            language = language
                        )
                    } else {
                        Spacer(modifier = Modifier.size(180.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BubbleButton(
                    text = if (language == "hu") "Kezdőlap" else "Homepage",
                    icon = Icons.Default.Home,
                    color = Color(0xFFFDA769),
                    onClick = { navController.navigate("home") }
                )
                BubbleButton(
                    text = if (language == "hu") "Havi nézet" else "Monthly View",
                    icon = Icons.Default.CalendarToday,
                    color = Color(0xFFABC270),
                    onClick = { navController.navigate("monthly") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDialog) {
        WeeklyImageViewerDialog(
            imageMap = imageMap,
            selectedDate = weekDates[selectedIndex],
            onClose = { showDialog = false },
            onChangePicture = { date, uri ->
                viewModel.replacePhotoForDate(date, uri)
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun DayCard(
    date: LocalDate,
    imageUri: Uri,
    onClick: () -> Unit,
    hasText: Boolean = false,
    hasAudio: Boolean = false,
    hasVideo: Boolean = false,
    language: String
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, if (language == "hu") Locale("hu") else Locale.ENGLISH)
    val borderColor = pastelColors.random()

    val imageUriWithTimestamp = if (imageUri != Uri.EMPTY) {
        imageUri.buildUpon()
            .appendQueryParameter("ts", System.currentTimeMillis().toString())
            .build()
    } else {
        Uri.EMPTY
    }

    Column(
        modifier = Modifier
            .size(180.dp)
            .border(3.dp, borderColor, RoundedCornerShape(16.dp))
            .background(SoftYellow, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (imageUriWithTimestamp != Uri.EMPTY) {
                AsyncImage(
                    model = imageUriWithTimestamp,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dayName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(date.toString(), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (language == "hu") "Nincs kép" else "No image", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (hasText) Text("📝", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 4.dp))
            if (hasAudio) Text("🎧", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 4.dp))
            if (hasVideo) Text("🎥", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}

fun getMonday(date: LocalDate): LocalDate {
    return date.minusDays(((date.dayOfWeek.value + 6) % 7).toLong())
}
