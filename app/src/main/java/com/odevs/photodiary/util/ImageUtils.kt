package com.example.photodiary.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.io.FileOutputStream

suspend fun generateCollageBitmap(context: Context, uris: List<Uri>): Bitmap {
    require(uris.isNotEmpty()) { "No images provided for collage" }

    val columns = 3  // 3 oszlop
    val rows = 4     // 4 sor
    val width = 1200
    val height = 1600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    val cellWidth = width / columns
    val cellHeight = height / rows

    // Pasztellszínek listája
    val pastelColors = listOf(
        Color.rgb(255, 179, 186), // rózsaszín
        Color.rgb(255, 223, 186), // barack
        Color.rgb(255, 255, 186), // sárga
        Color.rgb(186, 255, 201), // menta
        Color.rgb(186, 225, 255), // világoskék
        Color.rgb(255, 204, 229), // halványlila
        Color.rgb(204, 255, 229), // türkiz
        Color.rgb(240, 200, 255), // lila
        Color.rgb(200, 240, 255), // égkék
        Color.rgb(255, 240, 200)  // vaníliasárga
    )

    // Random sorrendben képek és cellapozíciók
    val shuffledUris = uris.shuffled()
    val positions = mutableListOf<Pair<Int, Int>>()
    for (row in 0 until rows) {
        for (col in 0 until columns) {
            positions.add(Pair(col, row))
        }
    }
    positions.shuffle()

    println("📸 Generating random collage with ${shuffledUris.size} images...")

    for ((index, pair) in positions.withIndex()) {
        val (col, row) = pair
        val x = col * cellWidth
        val y = row * cellHeight

        // Háttér szín
        val color = pastelColors.random()
        paint.color = color
        canvas.drawRect(x.toFloat(), y.toFloat(), (x + cellWidth).toFloat(), (y + cellHeight).toFloat(), paint)

        // Kép ráhelyezése (ha van elég kép)
        if (index < shuffledUris.size) {
            val loadedBitmap = loadBitmapFromUri(context, shuffledUris[index])

            // CENTER CROP logika
            val aspectBitmap = loadedBitmap.width.toFloat() / loadedBitmap.height
            val aspectCell = cellWidth.toFloat() / cellHeight

            var cropWidth = loadedBitmap.width
            var cropHeight = loadedBitmap.height

            if (aspectBitmap > aspectCell) {
                cropWidth = (loadedBitmap.height * aspectCell).toInt()
            } else {
                cropHeight = (loadedBitmap.width / aspectCell).toInt()
            }

            val cropX = (loadedBitmap.width - cropWidth) / 2
            val cropY = (loadedBitmap.height - cropHeight) / 2

            val srcRect = Rect(cropX, cropY, cropX + cropWidth, cropY + cropHeight)
            val destRect = Rect(x, y, x + cellWidth, y + cellHeight)

            canvas.drawBitmap(loadedBitmap, srcRect, destRect, null)
        }
    }
    println("✅ Random collage generated: width=${bitmap.width}, height=${bitmap.height}")
    return bitmap
}

    private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val loader = context.imageLoader
    val request = ImageRequest.Builder(context)
        .data(uri)
        .allowHardware(false)
        .build()
    val result = loader.execute(request)
    return (result as SuccessResult).drawable.toBitmap()
}

fun saveCollageBitmapToFile(context: Context, bitmap: Bitmap): File {
    val collageDir = File(context.filesDir, "collages")
    if (!collageDir.exists()) {
        collageDir.mkdirs()
    }

    val file = File(collageDir, "collage_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    }

    println("💾 Collage saved to: ${file.absolutePath}, size: ${file.length()} bytes")
    return file
}