package com.odevs.photodiary.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.odevs.photodiary.data.PhotoDatabase
import com.odevs.photodiary.data.PhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import android.graphics.BitmapFactory
import android.util.Log
import com.example.photodiary.util.saveCollageBitmapToFile
import com.example.photodiary.util.generateCollageBitmap
import com.odevs.photodiary.data.CollageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter


class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        PhotoDatabase::class.java,
        "photo_db"
    )

       .build()

    private val dao = db.photoDao()
    private val collageDao = db.collageDao() // <- ÚJ
    private val appContext = application.applicationContext

   private val _imageMap = MutableStateFlow<Map<LocalDate, Uri>>(emptyMap())
    val imageMap: StateFlow<Map<LocalDate, Uri>> = _imageMap

    private val _selectedCollageUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedCollageUris: StateFlow<List<Uri>> = _selectedCollageUris

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _currentPagerIndex = MutableStateFlow(0)
    val currentPagerIndex: StateFlow<Int> = _currentPagerIndex

    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen

    private val _weeklyImages = MutableStateFlow<List<Uri>>(emptyList())
    val weeklyImages: StateFlow<List<Uri>> = _weeklyImages

    private val _generatedCollages = MutableStateFlow<List<File>>(emptyList())
    val generatedCollages: StateFlow<List<File>> = _generatedCollages

    init {
        viewModelScope.launch {
            dao.getAllPhotos().collect { photoEntities ->
                val newMap = photoEntities.associate {
                    LocalDate.parse(it.date) to Uri.parse(it.filename)
                }
                _imageMap.value = newMap // mindig új Map példányt adunk → Compose frissül

                // 🔍 DEBUG LOG
                android.util.Log.d("PhotoViewModel", "Collected new imageMap: $newMap")
            }
        }

        viewModelScope.launch {
            collageDao.getAllCollages().collect { collageEntities ->
                val files = collageEntities.map { File(it.filename) }
                _generatedCollages.value = files
            }
        }
    }

    fun replacePhotoForDate(date: LocalDate, uri: Uri) {
        viewModelScope.launch {
            dao.deletePhotoByDate(date.toString())
            val copiedUri = saveImageToInternalStorage(appContext, uri, "$date.jpg")
            dao.insertPhoto(PhotoEntity(date = date.toString(), filename = copiedUri.toString()))

            // Frissítjük az imageMap-et új példányra
            val updatedMap = _imageMap.value.toMutableMap()
            updatedMap[date] = copiedUri
            _imageMap.value = updatedMap.toMap()

            // 🔍 DEBUG LOG
            android.util.Log.d("PhotoViewModel", "replacePhotoForDate called with date=$date, uri=$uri")
            android.util.Log.d("PhotoViewModel", "Updated imageMap: ${_imageMap.value}")

            _refreshTrigger.value += 1
        }
    }

    fun updateSelectedCollageUris(uris: List<Uri>) {
        _selectedCollageUris.value = uris
    }

    fun onDayClicked(date: LocalDate) {
        _selectedDate.value = date
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
    }

    fun setPagerIndex(index: Int) {
        _currentPagerIndex.value = index
    }

    fun openDialog() {
        _isDialogOpen.value = true
    }

    fun closeDialog() {
        _isDialogOpen.value = false
    }

    fun getVideoForDate(context: Context, date: LocalDate): Uri? {
        val filename = "${date.format(DateTimeFormatter.ISO_DATE)}.mp4"
        val videoFile = File(context.filesDir, "videos/$filename")
        return if (videoFile.exists()) Uri.fromFile(videoFile) else null
    }

    private fun saveImageToInternalStorage(context: Context, uri: Uri, filename: String): Uri {
        val file = File(context.filesDir, filename)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(file)
    }

    fun isLandscape(context: Context, uri: Uri): Boolean {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            return options.outWidth >= options.outHeight
        }
        return false
    }

    fun setSelectedCollageUris(uris: List<Uri>) {
        _selectedCollageUris.value = uris
    }

    fun toggleCollageSelection(uri: Uri) {
        val current = _selectedCollageUris.value.toMutableList()
        if (current.contains(uri)) {
            current.remove(uri)
        } else if (current.size < 12) {
            current.add(uri)
        }
        _selectedCollageUris.value = current
    }

    fun clearNewlyGeneratedFlag() {
        _isNewlyGenerated.value = false
    }

    private val _isNewlyGenerated = MutableStateFlow(false)
    val isNewlyGenerated: StateFlow<Boolean> = _isNewlyGenerated

    fun generateCollage(context: Context, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val selected = _selectedCollageUris.value
            if (selected.size < 2) {
                onComplete(false)
                return@launch
            }
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    generateCollageBitmap(context, selected)
                }
                val file = withContext(Dispatchers.IO) {
                    saveCollageBitmapToFile(context, bitmap)
                }
                if (file.exists() && file.length() > 0) {
                    val today = LocalDate.now().toString()
                    collageDao.insertCollage(
                        CollageEntity(
                            filename = file.absolutePath,
                            date = today
                        )
                    )
                    _generatedCollages.value = listOf(file) + _generatedCollages.value
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }

    }

    fun clearAllCollages() {
        viewModelScope.launch {
            collageDao.clearCollages()
            _generatedCollages.value = emptyList()
        }
    }
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    fun triggerRefresh() {
        _refreshTrigger.value += 1
    }

    private fun getNoteFile(context: Context, date: LocalDate): File {
        val dir = File(context.filesDir, "notes")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${date}_note.txt")
    }

    fun getAudioFile(context: Context, date: LocalDate): File {
        val dir = File(context.filesDir, "audio")
        return File(dir, "${date}_audio.m4a")
    }

    fun getVideoFile(context: Context, date: LocalDate): File {
        val dir = File(context.filesDir, "video")
        return File(dir, "${date}_video.mp4")
    }

    fun hasText(context: Context, date: LocalDate): Boolean {
        val file = getNoteFile(context, date)
        return file.exists() && file.readText().isNotBlank()
    }

    fun hasAudio(context: Context, date: LocalDate): Boolean {
        return getAudioFile(context, date).exists()
    }

    fun hasVideo(context: Context, date: LocalDate): Boolean {
        return getVideoFile(context, date).exists()
    }

    fun saveText(context: Context, date: LocalDate, text: String) {
        getNoteFile(context, date).writeText(text)
    }

    fun loadText(context: Context, date: LocalDate): String? {
        val file = getNoteFile(context, date)
        return if (file.exists()) file.readText() else null
    }

    fun saveMediaFile(context: Context, date: LocalDate, uri: Uri, type: String) {
        try {
            val dir = File(context.filesDir, type)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val extension = if (type == "video") "mp4" else "unknown"
            val file = File(dir, "${date}_$type.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("PhotoViewModel", "Saved $type to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("PhotoViewModel", "Failed to save $type: ${e.message}", e)
        }
    }
}
