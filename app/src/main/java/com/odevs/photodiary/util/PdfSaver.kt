package com.odevs.photodiary.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.time.LocalDate

fun savePdfToDownloads(context: Context, pdfFile: File, filename: String): Boolean {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, filename)
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        try {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            val inputStream = FileInputStream(pdfFile)
            inputStream.copyTo(outputStream!!)
            inputStream.close()
            outputStream.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return false
}