

package com.odevs.photodiary.screens

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.odevs.photodiary.R
import com.odevs.photodiary.storage.LanguageManager
import com.odevs.photodiary.ui.screens.LocalLanguage
import com.odevs.photodiary.viewmodel.PhotoViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.media.ThumbnailUtils
import androidx.compose.ui.graphics.graphicsLayer
import coil.request.CachePolicy
import coil.request.ImageRequest
import java.util.Locale

@Composable
fun DiaryScreen(
    viewModel: PhotoViewModel,
    onNavigateHome: () -> Unit,
    onExportPDF: (LocalDate, LocalDate, Context) -> Unit
) {
    val context = LocalContext.current
    val language = LocalLanguage.current

    val texts = mapOf(
        "title" to mapOf("en" to "My Diary", "hu" to "Naplóm"),
        "from" to mapOf("en" to "From", "hu" to "Dátumtól"),
        "to" to mapOf("en" to "To", "hu" to "Dátumig"),
        "home" to mapOf("en" to "🏠 Home", "hu" to "🏠 Kezdőlap"),
        "export" to mapOf("en" to "📄 Export PDF", "hu" to "📄 PDF export"),
        "audio" to mapOf("en" to "🎙️ Audio attached", "hu" to "🎙️ Csatolt hangfelvétel"),
        "ok" to mapOf("en" to "OK", "hu" to "Rendben"),
        "cancel" to mapOf("en" to "Cancel", "hu" to "Mégse")
    )

    var fullScreenImage by remember { mutableStateOf<Uri?>(null) }
    var fullScreenVideo by remember { mutableStateOf<Uri?>(null) }
    var refreshKey by remember { mutableStateOf(System.currentTimeMillis()) }
    val imageMap by viewModel.imageMap.collectAsState()
    LaunchedEffect(imageMap) {
        refreshKey = System.currentTimeMillis()
    }
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }

    val modernFont = FontFamily(Font(R.font.cormorantgaramond_variablefont_wght))

    val allDates = remember(startDate, endDate) {
        generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .background(Color(0xFFF4F4F4))
    ) {
        Text(
            texts["title"]?.get(language) ?: "My Diary",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = modernFont
        )

        Row(
            modifier = Modifier
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DatePickerField(texts["from"]?.get(language) ?: "From", startDate) { startDate = it }
            DatePickerField(texts["to"]?.get(language) ?: "To", endDate) { endDate = it }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(allDates) { date ->
                val noteText = remember(date) { viewModel.loadText(context, date) ?: "" }
                val imageUri = imageMap[date]
                val refreshKey = System.currentTimeMillis()  // MINDIG új érték

                val uriWithFakeParam = imageUri?.buildUpon()
                    ?.encodedQuery("t=" + System.currentTimeMillis())
                    ?.build()

                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(uriWithFakeParam)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .build()
                )
                val audioFile = viewModel.getAudioFile(context, date)
                val videoFile = viewModel.getVideoFile(context, date)
                val videoUri: Uri? = if (videoFile.exists()) Uri.fromFile(videoFile) else null
                val videoThumbnail: Bitmap? = if (videoFile.exists()) {
                    ThumbnailUtils.createVideoThumbnail(videoFile.path, MediaStore.Video.Thumbnails.MINI_KIND)
                } else null

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = date.format(
                                DateTimeFormatter.ofPattern("yyyy. MMMM d.", if (language == "hu") Locale("hu") else Locale.ENGLISH)
                            ),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = modernFont
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        imageUri?.let {
                            Image(
                                painter = painter,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { fullScreenImage = it }
                            )
                        }

                        if (noteText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(noteText, fontSize = 16.sp, lineHeight = 22.sp, fontFamily = modernFont)
                        }

                        videoUri?.let { uri ->
                            videoThumbnail?.let { thumb ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Image(
                                    bitmap = thumb.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { fullScreenVideo = uri }
                                )
                            }
                        }

                        if (audioFile.exists()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Color(0xFF3E2723),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        val player = MediaPlayer()
                                        player.setDataSource(audioFile.path)
                                        player.prepare()
                                        player.start()
                                    }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateHome,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2CFC3))
                    ) {
                        Text(texts["home"]?.get(language) ?: "🏠 Home", color = Color(0xFF3E2723), fontFamily = modernFont)
                    }
                }
            }
        }
    }

    fullScreenImage?.let { uri ->
        Dialog(onDismissRequest = { fullScreenImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .graphicsLayer(scaleX = 1.5f, scaleY = 1.5f)
                        .clickable { fullScreenImage = null }
                )
            }
        }
    }

    fullScreenVideo?.let { uri ->
        Dialog(onDismissRequest = { fullScreenVideo = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = {
                        VideoView(it).apply {
                            setVideoURI(uri)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                mp.start()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .graphicsLayer(scaleX = 1.5f, scaleY = 1.5f)
                        .clickable { fullScreenVideo = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate.toString(),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.width(160.dp),
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null)
            }
        }
    )

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            onDateSelected(localDate)
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4E342E))
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4E342E))
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
