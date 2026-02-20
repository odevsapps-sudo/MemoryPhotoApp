package com.example.photodiary.utils

import android.content.Context
import android.net.Uri
import java.io.File

fun getImageUriIfExists(context: Context, fileName: String): Uri? {
    val file = File(context.filesDir, fileName)
    return if (file.exists()) Uri.fromFile(file) else null
}
