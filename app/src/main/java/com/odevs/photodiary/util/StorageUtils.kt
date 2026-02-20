package com.example.photodiary.utils

import android.content.Context
import android.net.Uri

fun loadAllPhotoUris(context: Context): List<Uri> {
    val files = context.filesDir.listFiles() ?: return emptyList()
    return files.filter { it.name.startsWith("photo_") && it.name.endsWith(".jpg") }
        .map { Uri.fromFile(it) }
}
