package com.odevs.photodiary.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LimeGreen = Color(0xFFABC270)
val SoftYellow = Color(0xFFFEC868)
val SoftOrange = Color(0xFFFDA769)
val DarkBrown = Color(0xFF473C33)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = LimeGreen,
    secondary = SoftOrange,
    tertiary = SoftYellow,
    background = Color.White,
    surface = SoftYellow,
    onPrimary = DarkBrown,
    onSecondary = DarkBrown,
    onTertiary = DarkBrown,
    onBackground = DarkBrown,
    onSurface = DarkBrown
)

@Composable
fun MemoryPhotoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}