package com.odevs.photodiary.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BubbleButton(
    text: String,
    color: Color,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    fontSize: Int = 16,
    height: Dp = 80.dp,
    width: Dp = 170.dp,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .width(width)
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = fontSize.sp
        )
    }
}

@Composable
fun SmallBubbleButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    fontSize: Int = 14,
    height: Dp = 48.dp,
    width: Dp = 140.dp
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(height)
            .width(width)
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp
        )
    }
}
