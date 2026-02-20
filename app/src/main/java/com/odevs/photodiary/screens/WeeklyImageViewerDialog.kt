package com.odevs.photodiary.screens

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.*
import com.odevs.photodiary.R
import com.odevs.photodiary.ui.LanguageProvider
import com.odevs.photodiary.viewmodel.PhotoViewModel
import java.time.LocalDate

val backgroundColor = Color(0xFFFDF6EC)
val headerTextColor = Color(0xFF4B3832)
val textFieldBackground = Color(0xFFFFFBF0)
val placeholderColor = Color(0xFFB8A598)
val buttonColor = Color(0xFFE2CFC3)
val darkButtonText = Color(0xFF3E2723)
val borderRadius = RoundedCornerShape(16.dp)


fun getVideoThumbnail(path: String): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        retriever.release()
        bitmap
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WeeklyImageViewerDialog(
    imageMap: Map<LocalDate, Uri>,
    selectedDate: LocalDate,
    onClose: () -> Unit,
    onChangePicture: (LocalDate, Uri) -> Unit,
    viewModel: PhotoViewModel
) {
    val context = LocalContext.current
    val language = LanguageProvider.language
    val cormorantFont = FontFamily(Font(R.font.cormorantgaramond_variablefont_wght))
    val permissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    val allDates = remember(imageMap, selectedDate) {
        (imageMap.keys + selectedDate).toSortedSet().toList()
    }

    val pagerState = rememberPagerState { allDates.size }
    val textStates = remember { mutableStateMapOf<LocalDate, String>() }
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()

    fun handleCapturedImage(uri: Uri?) {
        uri?.let {
            val date = allDates.getOrNull(pagerState.currentPage) ?: return
            viewModel.replacePhotoForDate(date, it)
            onChangePicture(date, it)
        }
    }

    fun handleCapturedVideo(uri: Uri?) {
        uri?.let {
            val date = allDates.getOrNull(pagerState.currentPage) ?: return
            viewModel.saveMediaFile(context, date, it, "video")
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = ::handleCapturedImage
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = ::handleCapturedVideo
    )

    LaunchedEffect(selectedDate) {
        val initialIndex = allDates.indexOf(selectedDate).coerceAtLeast(0)
        pagerState.scrollToPage(initialIndex)
    }

    val strings = mapOf(
        "title" to if (language == "hu") "Napi fájlok" else "Daily files",
        "noImage" to if (language == "hu") "Nincs kép" else "No image",
        "diary" to if (language == "hu") "Napló bejegyzés:" else "Diary blog:",
        "record" to if (language == "hu") "🎙️ Hangfelvétel" else "🎙️ Voice Record",
        "stop" to if (language == "hu") "⏹️ Hangfelv.leállítás" else "⏹️ Stop voice recording",
        "play" to if (language == "hu") "▶️ Hang lejátszás" else "▶️ Play voice",
        "pause" to if (language == "hu") "⏸️ Hang lejátszás szünet" else "⏸️ Pause voice",
        "playVideo" to if (language == "hu") "▶️ Videó lejátszás" else "▶️ Play Video",
        "pauseVideo" to if (language == "hu") "⏸️ Videó szünet" else "⏸️ Pause Video",
        "attachVideo" to if (language == "hu") "🎥 Videó hozzáadása" else "🎥 Attach Video",
        "saveClose" to if (language == "hu") "Mentés és bezárás" else "Save and Close",
        "uploadPic" to if (language == "hu") "📷 Kép feltöltése" else "📷 Upload Picture"
    )

    BasicAlertDialog(
        onDismissRequest = {
            val date = allDates.getOrNull(pagerState.currentPage)
            date?.let {
                textStates[it]?.let { text -> viewModel.saveText(context, it, text) }
            }
            onClose()
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val page = pagerState.currentPage.coerceIn(0, allDates.lastIndex)
                    val currentDate = allDates[page]
                    val hasAudio = viewModel.hasAudio(context, currentDate)
                    val hasVideo = viewModel.hasVideo(context, currentDate)

                    if (!textStates.containsKey(currentDate)) {
                        val loaded = viewModel.loadText(context, currentDate) ?: ""
                        textStates[currentDate] = loaded
                    }

                    Text(
                        text = "${strings["title"]}: $currentDate",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = cormorantFont,
                        color = headerTextColor
                    )

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .padding(vertical = 12.dp)) {

                        HorizontalPager(
                            state = pagerState,
                            pageSize = PageSize.Fill,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp) // Hagyj helyet a nyilaknak
                        ) { pageIndex ->
                            val date = allDates[pageIndex]
                            val baseUri = imageMap[date] ?: Uri.EMPTY
                            val imageUriWithTimestamp = if (baseUri != Uri.EMPTY) {
                                baseUri.buildUpon().appendQueryParameter("ts", System.currentTimeMillis().toString()).build()
                            } else Uri.EMPTY

                            if (imageUriWithTimestamp != Uri.EMPTY) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUriWithTimestamp),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(borderRadius)
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(strings["noImage"]!!, color = Color.DarkGray)
                                }
                            }
                        }

                        ScrollHintArrow(isLeft = true)
                        ScrollHintArrow(isLeft = false)
                    }

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        shape = borderRadius
                          ) {
                        Text(strings["uploadPic"]!!, color = darkButtonText)
                    }
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(strings["diary"]!!, fontSize = 20.sp, fontWeight = FontWeight.Medium, fontFamily = cormorantFont, color = headerTextColor)

                    TextField(
                        value = textStates[currentDate] ?: "",
                        onValueChange = { textStates[currentDate] = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 8.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 17.sp, fontFamily = cormorantFont, color = Color.DarkGray
                        ),
                        placeholder = {
                            Text(
                                if (language == "hu") "Ide írhatod a napi naplóbejegyzést..." else "Write your daily journal entry here...",
                                fontSize = 18.sp,
                                fontFamily = cormorantFont,
                                color = placeholderColor
                            )
                        },
                        shape = borderRadius,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = textFieldBackground,
                            focusedIndicatorColor = Color(0xFFFFF8DC),
                            unfocusedIndicatorColor = Color(0xFFFFF8DC),
                            cursorColor = Color(0xFFFFF8DC)
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val audioFile = viewModel.getAudioFile(context, currentDate)
                    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
                    var isRecording by remember { mutableStateOf(false) }
                    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
                    var isPlaying by remember { mutableStateOf(false) }

                    LaunchedEffect(hasAudio, audioFile.path) {
                        if (hasAudio && audioFile.exists()) {
                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {
                                setDataSource(audioFile.absolutePath)
                                prepare()
                            }
                        }
                    }

// 🎙️ FELVÉTEL + ▶️ LEJÁTSZÁS EGY SORBAN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isRecording) {
                                    try {
                                        audioFile.parentFile?.mkdirs()
                                        mediaRecorder = MediaRecorder().apply {
                                            setAudioSource(MediaRecorder.AudioSource.MIC)
                                            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                            setOutputFile(audioFile.absolutePath)
                                            prepare()
                                            start()
                                        }
                                        isRecording = true
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Voice recording failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    try {
                                        mediaRecorder?.apply {
                                            stop()
                                            release()
                                            Toast.makeText(context, "Saved to: ${audioFile.absolutePath}", Toast.LENGTH_SHORT).show()
                                        }
                                    } finally {
                                        mediaRecorder = null
                                        isRecording = false
                                    }
                                    mediaPlayer = MediaPlayer().apply {
                                        setDataSource(audioFile.absolutePath)
                                        prepare()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text(if (isRecording) strings["stop"]!! else strings["record"]!!)
                        }

                        Button(
                            onClick = {
                                mediaPlayer?.let {
                                    if (it.isPlaying) {
                                        it.pause()
                                        isPlaying = false
                                    } else {
                                        it.start()
                                        isPlaying = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = hasAudio && audioFile.exists()
                        ) {
                            Text(if (isPlaying) strings["pause"]!! else strings["play"]!!)
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            mediaPlayer?.release()
                            mediaPlayer = null
                        }
                    }

                    val videoFile = viewModel.getVideoFile(context, currentDate)
                    if (hasVideo && videoFile.exists() && videoFile.length() > 0) {
                        var videoView: VideoView? by remember { mutableStateOf(null) }
                        var isVideoPlaying by remember { mutableStateOf(false) }
                        var showVideo by remember { mutableStateOf(false) }

                        if (!showVideo) {
                            val thumbnail = remember(videoFile.path) { getVideoThumbnail(videoFile.path) }
                            if (thumbnail != null) {
                                Image(
                                    bitmap = thumbnail.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .padding(top = 16.dp)
                                        .clickable { showVideo = true }
                                )
                            }
                        } else {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoPath(videoFile.path)
                                        setOnPreparedListener { mediaPlayer ->
                                            mediaPlayer.isLooping = false
                                            start() // Azonnal elindítjuk
                                            isVideoPlaying = true
                                        }
                                        setOnCompletionListener {
                                            isVideoPlaying = false
                                        }
                                        videoView = this
                                    }
                                },
                                update = {
                                    if (isVideoPlaying) it.start() else it.pause()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(360.dp)
                                    .padding(top = 16.dp)
                                    .clickable {
                                        if (isVideoPlaying) videoView?.pause()
                                        else videoView?.start()
                                        isVideoPlaying = !isVideoPlaying
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))


                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(strings["attachVideo"]!!)
                        }

                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(strings["attachVideo"]!!)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                textStates[currentDate]?.let { viewModel.saveText(context, currentDate, it) }
                                onClose()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings["saveClose"]!!)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ScrollHintArrow(
    isLeft: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(
        contentAlignment = if (isLeft) Alignment.CenterStart else Alignment.CenterEnd,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = if (isLeft) "◀" else "▶",
            fontSize = 32.sp,
            color = Color.Gray.copy(alpha = alpha),
            modifier = Modifier
                .padding(horizontal = 12.dp)
        )
    }
}
