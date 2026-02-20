package com.odevs.photodiary.graphics

import androidx.compose.ui.graphics.Color

val LimeGreen = Color(0xFF8BC34A)
val SoftYellow = Color(0xFFFFF59D)
val SoftOrange = Color(0xFFFFA726)
val DarkBrown = Color(0xFF473C33)
val SoftGreen = Color(0xFFB4D8A8)

fun randomPastelColor(): Color {
    val pastelColors = listOf(
        Color(0xFFF9F1DA),
        Color(0xFFE5EBCE),
        Color(0xFFFFE4FA),
        Color(0xFFCCF4CB),
        Color(0xFFF7DDC8),
        Color(0xFFE0CCCC),
        Color(0xFFDAD5EE)
    )
    return pastelColors.random()
}
