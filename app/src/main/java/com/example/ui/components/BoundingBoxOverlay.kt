package com.example.ui.components

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MinimalistPurpleLight
import com.example.ui.theme.MinimalistPurplePrimary
import com.example.ui.theme.MinimalistStatusSuccess
import com.example.ui.theme.MinimalistStatusWarning

@Composable
fun BoundingBoxOverlay(
    modifier: Modifier = Modifier,
    boundingBox: Rect?,
    isMatched: Boolean,
    isCooldown: Boolean
) {
    val boxColor = when {
        isCooldown -> MinimalistStatusWarning
        isMatched -> MinimalistStatusSuccess
        boundingBox != null -> MinimalistPurplePrimary
        else -> MinimalistPurpleLight.copy(alpha = 0.6f)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Central Viewfinder Guide Box
        val guideWidth = width.coerceAtMost(height) * 0.65f
        val guideHeight = guideWidth * 1.25f
        val left = (width - guideWidth) / 2
        val top = (height - guideHeight) / 2.3f

        // Draw Rounded Dashed Guide Box (matching clean minimalism frame)
        drawRoundRect(
            color = boxColor,
            topLeft = Offset(left, top),
            size = Size(guideWidth, guideHeight),
            cornerRadius = CornerRadius(48.dp.toPx(), 48.dp.toPx()),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            )
        )

        // Draw Scanning Laser Beam Effect Line
        val laserY = top + guideHeight * 0.5f
        drawLine(
            color = Color(0xFF60A5FA).copy(alpha = 0.5f),
            start = Offset(left - 10.dp.toPx(), laserY),
            end = Offset(left + guideWidth + 10.dp.toPx(), laserY),
            strokeWidth = 2.dp.toPx()
        )
    }
}
