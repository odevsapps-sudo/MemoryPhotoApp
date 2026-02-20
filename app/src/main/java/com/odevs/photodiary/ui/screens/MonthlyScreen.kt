package com.odevs.photodiary.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.odevs.photodiary.ui.LanguageProvider
import com.odevs.photodiary.ui.components.BubbleButton
import com.odevs.photodiary.ui.components.SmallBubbleButton
import com.odevs.photodiary.viewmodel.PhotoViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun MonthlyScreen(
    navController: NavHostController
) {
    val parentEntry = remember(navController) {
        navController.getBackStackEntry("home")
    }
    val viewModel: PhotoViewModel = hiltViewModel(parentEntry)

    val language = LanguageProvider.language
    val locale = if (language == "hu") Locale("hu") else Locale.ENGLISH

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val dates = (1..currentMonth.lengthOfMonth()).map { currentMonth.atDay(it) }
    val imageMap by viewModel.imageMap.collectAsState(initial = emptyMap())
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }

    val images by remember(imageMap, dates, refreshTrigger) {
        derivedStateOf {
            val now = System.currentTimeMillis()
            dates.map {
                val uri = imageMap[it] ?: Uri.EMPTY
                if (uri != Uri.EMPTY) {
                    uri.buildUpon().appendQueryParameter("ts", now.toString()).build()
                } else Uri.EMPTY
            }
        }
    }

    val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase(locale) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8DC))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "$monthName ${currentMonth.year}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SmallBubbleButton(
                text = if (language == "hu") "Előző hónap" else "Previous Month",
                color = Color(0xFFFDA769),
                onClick = { currentMonth = currentMonth.minusMonths(1) }
            )
            SmallBubbleButton(
                text = if (language == "hu") "Következő hónap" else "Next Month",
                color = Color(0xFFABC270),
                onClick = { currentMonth = currentMonth.plusMonths(1) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f)) {
            itemsIndexed(dates) { index, date ->
                val uri = images[index]
                val hasAudio = viewModel.hasAudio(LocalContext.current, date)
                val hasVideo = viewModel.hasVideo(LocalContext.current, date)
                val hasText = viewModel.loadText(LocalContext.current, date)?.isNotBlank() == true
                MonthDayCard(date, uri, hasAudio, hasVideo, hasText, language) {
                    selectedIndex = index
                    showDialog = true
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BubbleButton(
                text = if (language == "hu") "Kezdőlap" else "Homepage",
                color = Color(0xFFFDA769),
                icon = Icons.Default.Home,
                onClick = { navController.navigate("home") }
            )
            BubbleButton(
                text = if (language == "hu") "Heti nézet" else "Weekly View",
                color = Color(0xFFABC270),
                icon = Icons.Default.CalendarToday,
                onClick = { navController.navigate("weekly") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDialog) {
        WeeklyImageViewerDialog(
            imageMap = imageMap,
            selectedDate = dates[selectedIndex],
            onClose = { showDialog = false },
            onChangePicture = { date, uri -> viewModel.replacePhotoForDate(date, uri) },
            viewModel = viewModel
        )
    }
}

@Composable
fun MonthDayCard(
    date: LocalDate,
    imageUri: Uri?,
    hasAudio: Boolean,
    hasVideo: Boolean,
    hasText: Boolean,
    language: String,
    onClick: () -> Unit
) {
    val borderColor = Color(0xFFE0C097)
    val locale = if (language == "hu") Locale("hu") else Locale.ENGLISH
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
    val dayText = "${date.dayOfMonth} – $dayOfWeek"

    Box(
        modifier = Modifier
            .size(100.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF8DC), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null && imageUri != Uri.EMPTY) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                dayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(2.dp)
                .zIndex(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (hasAudio) Text("🎤", fontSize = 12.sp)
            if (hasVideo) Text("🎥", fontSize = 12.sp)
            if (hasText) Text("📓", fontSize = 12.sp)
        }
    }
}
