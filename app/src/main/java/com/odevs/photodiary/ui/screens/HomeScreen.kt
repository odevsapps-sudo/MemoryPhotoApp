package com.odevs.photodiary.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.odevs.photodiary.R
import com.odevs.photodiary.graphics.DarkBrown
import com.odevs.photodiary.graphics.LimeGreen
import com.odevs.photodiary.graphics.SoftYellow
import com.odevs.photodiary.storage.LanguageManager
import com.odevs.photodiary.ui.LanguageProvider
import com.odevs.photodiary.ui.components.BubbleButton
import kotlin.random.Random
import java.time.LocalDate

val LocalLanguage = compositionLocalOf { "en" }

@Composable
fun HomeScreen(navController: NavController, onLanguageChange: (String) -> Unit) {
    val context = LocalContext.current
    val language = LocalLanguage.current
    LaunchedEffect(language) {
        LanguageProvider.language = language
    }
    CompositionLocalProvider(LocalLanguage provides language) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBrown)
        ) {
            Canvas(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopStart)
            ) {
                val diameter = size.width * 2f
                drawArc(
                    color = Color(0xFFB3E5FC),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = true,
                    topLeft = Offset(-size.width, -size.height),
                    size = androidx.compose.ui.geometry.Size(diameter, diameter)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Odevs",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 32.sp,
                    color = Color(0xFFFFF9C4),
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        onLanguageChange("hu")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.flag_hu),
                            contentDescription = "Magyar",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        onLanguageChange("en")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.flag_en),
                            contentDescription = "English",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                    }
                }

                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
            }

            BackgroundBubbles()

            val textMap = mapOf(
                "weekly" to mapOf("en" to "Weekly diary", "hu" to "Heti napló"),
                "monthly" to mapOf("en" to "Monthly diary", "hu" to "Havi napló"),
                "create" to mapOf("en" to "Creating collage", "hu" to "Kollázs készítés"),
                "collages" to mapOf("en" to "Collages", "hu" to "Kollázsok"),
                "diary" to mapOf("en" to "My Diary", "hu" to "Naplóm"),
                "hint" to mapOf("en" to "Tap to add content", "hu" to "Érintsd meg a tartalom hozzáadásához")
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(45.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    BubbleButton(
                        text = textMap["weekly"]?.get(language) ?: "Weekly diary",
                        color = LimeGreen,
                        icon = Icons.Filled.CalendarToday,
                        onClick = { navController.navigate("weekly") },
                        fontSize = 21,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 8.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .offset(x = (-10).dp, y = 10.dp)
                    )
                }
                Text(
                    text = textMap["hint"]?.get(language) ?: "Tap to add content",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Box(contentAlignment = Alignment.TopEnd) {
                    BubbleButton(
                        text = textMap["monthly"]?.get(language) ?: "Monthly diary",
                        color = SoftYellow,
                        icon = Icons.Filled.CalendarToday,
                        onClick = { navController.navigate("monthly") },
                        fontSize = 21,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(horizontal = 8.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .offset(x = (-10).dp, y = 10.dp)
                    )
                }
                Text(
                    text = textMap["hint"]?.get(language) ?: "Tap to add content",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                BubbleButton(
                    text = "🎨 " + (textMap["create"]?.get(language) ?: "Creating collage"),
                    color = LimeGreen,
                    onClick = { navController.navigate("collageSelection") },
                    fontSize = 21,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                BubbleButton(
                    text = "🎨 " + (textMap["collages"]?.get(language) ?: "Collages"),
                    color = Color(0xFFD1C4E9),
                    onClick = { navController.navigate("savedCollages") },
                    fontSize = 21,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                BubbleButton(
                    text = if (language == "hu") "📓 Naplóm" else "📓 My Diary",
                    color = Color(0xFFFFE0B2),
                    onClick = { navController.navigate("diary") },
                    fontSize =21,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                BubbleButton(
                    text = if (language == "hu") "📝 Heti teendők" else "📝 Weekly Todo",
                    color = Color(0xFFB2DFDB),
                    onClick = { navController.navigate("todo_screen/${LocalDate.now()}") },
                    fontSize = 21,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(horizontal = 8.dp)
                )

            }
        }
    }
}

@Composable
fun BackgroundBubbles() {
    val pastelColors = listOf(
        Color(0xFFFFC1CC), Color(0xFFB3E5FC), Color(0xFFC8E6C9),
        Color(0xFFFFF9C4), Color(0xFFD1C4E9), Color(0xFFFFE0B2)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "bubbleTransition")

    val bubbles = remember {
        List(20) {
            Bubble(
                color = pastelColors.random(),
                offsetX = Random.nextFloat(),
                offsetY = Random.nextFloat(),
                baseSize = (30..120).random().dp
            )
        }
    }

    val animatedSizes = bubbles.mapIndexed { index, bubble ->
        infiniteTransition.animateValue(
            initialValue = bubble.baseSize,
            targetValue = bubble.baseSize + 10.dp,
            typeConverter = Dp.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing, delayMillis = index * 100),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bubbleAnim$index"
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        bubbles.forEachIndexed { index, bubble ->
            drawBubble(
                color = bubble.color,
                radius = animatedSizes[index].value.toPx() / 2,
                offsetX = size.width * bubble.offsetX,
                offsetY = size.height * bubble.offsetY
            )
        }
    }
}

fun DrawScope.drawBubble(color: Color, radius: Float, offsetX: Float, offsetY: Float) {
    drawCircle(
        color = color.copy(alpha = 0.25f),
        radius = radius,
        center = Offset(offsetX, offsetY)
    )
}

data class Bubble(
    val color: Color,
    val offsetX: Float,
    val offsetY: Float,
    val baseSize: Dp
)
