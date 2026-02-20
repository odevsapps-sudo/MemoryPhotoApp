package com.odevs.photodiary.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun exportDiaryToPdf(
    context: Context,
    imageMap: Map<LocalDate, Uri>,
    textMap: Map<LocalDate, String>,
    startDate: LocalDate,
    endDate: LocalDate,
    outputFile: File
): Boolean {
    val document = PdfDocument()
    val formatter = DateTimeFormatter.ofPattern("yyyy. MMMM dd.")

    var pageNumber = 1

    val allDates = generateSequence(startDate) { it.plusDays(1) }
        .takeWhile { it <= endDate }
        .toList()

    for (date in allDates) {
        val text = textMap[date] ?: ""
        val imageUri = imageMap[date]

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Fejléc: dátum
        canvas.drawText("📅 ${formatter.format(date)}", 40f, 60f, android.graphics.Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        })

        // Kép, ha van
        imageUri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.let { bmp ->
                    val scaled = Bitmap.createScaledBitmap(bmp, 300, 200, true)
                    canvas.drawBitmap(scaled, 40f, 80f, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Napló szöveg
        val wrappedText = text.chunked(90).joinToString("\n") // sortörés hosszabb szöveghez
        canvas.drawText("📝", 40f, 300f, android.graphics.Paint().apply { textSize = 16f })
        canvas.drawText(wrappedText, 60f, 320f, android.graphics.Paint().apply {
            textSize = 14f
        })

        document.finishPage(page)
        pageNumber++
    }

    return try {
        FileOutputStream(outputFile).use { output ->
            document.writeTo(output)
        }
        document.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
