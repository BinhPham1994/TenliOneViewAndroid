package com.tenli.oneview.ui.features.home

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun HomeLineChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF1890FF),
    axisColor: Color = Color(0xFF434343)
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Không có dữ liệu", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.second }?.coerceAtLeast(10f) ?: 10f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        
        val chartWidth = width - padding * 1.5f
        val chartHeight = height - padding * 1.5f
        
        // Vẽ trục X, Y
        drawLine(
            color = axisColor,
            start = Offset(padding, height - padding),
            end = Offset(width - padding / 2, height - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = axisColor,
            start = Offset(padding, height - padding),
            end = Offset(padding, padding / 2),
            strokeWidth = 2f
        )

        val textPaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 28f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.RIGHT
        }

        // Vẽ các mức giá trị trục Y (0, max/2, max)
        val steps = 4
        for (i in 0..steps) {
            val yValue = maxValue * i / steps
            val yPos = height - padding - (i.toFloat() / steps) * chartHeight
            drawContext.canvas.nativeCanvas.drawText(
                yValue.toInt().toString(),
                padding - 10f,
                yPos + 10f,
                textPaint
            )
        }

        // Vẽ đường line
        if (data.size > 1) {
            val path = Path()
            val stepX = chartWidth / (data.size - 1)
            
            data.forEachIndexed { index, pair ->
                val x = padding + index * stepX
                val y = height - padding - (pair.second / maxValue) * chartHeight
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // Vẽ điểm
                drawCircle(color = lineColor, radius = 6f, center = Offset(x, y))
                
                // Vẽ nhãn trục X (chỉ vẽ một vài nhãn để tránh đè chữ)
                if (data.size < 10 || index % (data.size / 5) == 0 || index == data.size - 1) {
                    textPaint.textAlign = Paint.Align.CENTER
                    drawContext.canvas.nativeCanvas.drawText(
                        pair.first,
                        x,
                        height - padding + 35f,
                        textPaint
                    )
                }
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 4f)
            )
        }
    }
}

@Composable
fun HomeHorizontalBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF1890FF),
    barHeight: Float = 40f
) {
    if (data.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Không có dữ liệu", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.second }?.coerceAtLeast(10f) ?: 10f

    Canvas(modifier = modifier) {
        val width = size.width
        val paddingLeft = 120.dp.toPx() // Chỗ cho Label
        val paddingRight = 40.dp.toPx()
        val chartWidth = width - paddingLeft - paddingRight
        
        val itemHeight = size.height / data.size
        
        val textPaintLabel = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 28f
            textAlign = Paint.Align.RIGHT
        }
        
        val textPaintValue = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 28f
            textAlign = Paint.Align.LEFT
        }

        data.forEachIndexed { index, pair ->
            val y = index * itemHeight + itemHeight / 2
            
            // Tên nhãn (Label)
            val labelStr = if (pair.first.length > 15) pair.first.take(12) + "..." else pair.first
            drawContext.canvas.nativeCanvas.drawText(
                labelStr,
                paddingLeft - 20f,
                y + 10f,
                textPaintLabel
            )
            
            // Vẽ Bar
            val barW = (pair.second / maxValue) * chartWidth
            drawRoundRect(
                color = barColor,
                topLeft = Offset(paddingLeft, y - barHeight / 2),
                size = Size(barW, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
            )
            
            // Vẽ giá trị
            drawContext.canvas.nativeCanvas.drawText(
                pair.second.toInt().toString(),
                paddingLeft + barW + 10f,
                y + 10f,
                textPaintValue
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = title.uppercase(), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = iconColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}
