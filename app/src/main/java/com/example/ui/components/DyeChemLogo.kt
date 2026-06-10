package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryTeal

@Composable
fun DyeChemLogo(
    modifier: Modifier = Modifier,
    color: Color = PrimaryTeal
) {
    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height
        
        // 1. Draw Industrial Gears / Rings in the background
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = w * 0.45f,
            style = Stroke(width = 4f)
        )
        for (i in 0 until 12) {
            val angle = i * 30 * Math.PI / 180.0
            val xStart = (w / 2) + (w * 0.42f * Math.cos(angle)).toFloat()
            val yStart = (h / 2) + (h * 0.42f * Math.sin(angle)).toFloat()
            val xEnd = (w / 2) + (w * 0.48f * Math.cos(angle)).toFloat()
            val yEnd = (h / 2) + (h * 0.48f * Math.sin(angle)).toFloat()
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(xStart, yStart),
                end = Offset(xEnd, yEnd),
                strokeWidth = 6f
            )
        }

        // 2. Draw Chemical Flask Path
        // Coordinate reference:
        // Neck top-left: (w*0.4, h*0.2)
        // Neck top-right: (w*0.6, h*0.2)
        // Neck bottom-left: (w*0.4, h*0.4)
        // Neck bottom-right: (w*0.6, h*0.4)
        // Body bottom-right: (w*0.8, h*0.8)
        // Body bottom-left: (w*0.2, h*0.8)
        val flaskPath = Path().apply {
            moveTo(w * 0.4f, h * 0.2f)
            lineTo(w * 0.6f, h * 0.2f)
            lineTo(w * 0.6f, h * 0.4f)
            lineTo(w * 0.85f, h * 0.78f)
            quadraticTo(w * 0.88f, h * 0.85f, w * 0.8f, h * 0.85f)
            lineTo(w * 0.2f, h * 0.85f)
            quadraticTo(w * 0.12f, h * 0.85f, w * 0.15f, h * 0.78f)
            lineTo(w * 0.4f, h * 0.4f)
            close()
        }

        // Fill Flask lower level with "chemical dye"
        val liquidPath = Path().apply {
            moveTo(w * 0.3f, h * 0.58f)
            // Draw a wave
            quadraticTo(w * 0.45f, h * 0.56f, w * 0.5f, h * 0.58f)
            quadraticTo(w * 0.65f, h * 0.6f, w * 0.7f, h * 0.58f)
            lineTo(w * 0.81f, h * 0.78f)
            quadraticTo(w * 0.83f, h * 0.83f, w * 0.78f, h * 0.83f)
            lineTo(w * 0.22f, h * 0.83f)
            quadraticTo(w * 0.17f, h * 0.83f, w * 0.19f, h * 0.78f)
            close()
        }
        drawPath(liquidPath, color = color.copy(alpha = 0.5f))

        // Draw Flask Outline
        drawPath(flaskPath, color = color, style = Stroke(width = 8f))
        
        // Draw Flask Rim Top
        drawOval(
            color = color,
            topLeft = Offset(w * 0.38f, h * 0.17f),
            size = Size(w * 0.24f, h * 0.05f),
            style = Stroke(width = 8f)
        )

        // Draw boiling liquid bubbles inside the flask
        drawCircle(color = color, radius = 6f, center = Offset(w * 0.42f, h * 0.72f))
        drawCircle(color = color, radius = 4f, center = Offset(w * 0.46f, h * 0.66f))
        drawCircle(color = color, radius = 8f, center = Offset(w * 0.55f, h * 0.76f))
        drawCircle(color = color, radius = 5f, center = Offset(w * 0.64f, h * 0.70f))

        // 3. Draw DC Monogram in the core
        // Drawing custom text manually using paint or drawing beautiful paths representing 'D' and 'C'
        // 'D' path: left line and round curve
        // 'C' path: round curve opening to the right
        val dPath = Path().apply {
            moveTo(w * 0.43f, h * 0.47f)
            lineTo(w * 0.43f, h * 0.57f)
            moveTo(w * 0.43f, h * 0.47f)
            quadraticTo(w * 0.52f, h * 0.52f, w * 0.43f, h * 0.57f)
        }
        val cPath = Path().apply {
            moveTo(w * 0.57f, h * 0.48f)
            quadraticTo(w * 0.47f, h * 0.52f, w * 0.57f, h * 0.56f)
        }
        
        drawPath(dPath, color = Color.White, style = Stroke(width = 6f))
        drawPath(cPath, color = Color.White, style = Stroke(width = 6f))
    }
}
