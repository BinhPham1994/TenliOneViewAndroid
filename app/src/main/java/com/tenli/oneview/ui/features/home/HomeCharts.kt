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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawWithContent


@Composable
fun HomeLineChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFF97316),
    axisColor: Color = Color(0xFF434343)
) {
    if (data.isEmpty() || data.all { it.second == 0f }) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Không có dữ liệu",
                tint = Color.LightGray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Không có dữ liệu", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.second }?.coerceAtLeast(10f) ?: 10f
    
    val textColorArgb = MaterialTheme.colorScheme.onSurface.run { 
        android.graphics.Color.argb((alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
    }
    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 32.dp.toPx()
        
        val chartWidth = width - padding * 1.2f
        val chartHeight = height - padding * 1.2f
        
        // Vẽ trục X, Y
        drawLine(
            color = axisColor.copy(alpha = 0.5f),
            start = Offset(padding, height - padding),
            end = Offset(width - padding / 2, height - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = axisColor.copy(alpha = 0.5f),
            start = Offset(padding, height - padding),
            end = Offset(padding, padding / 2),
            strokeWidth = 2f
        )

        val textPaint = Paint().apply {
            color = textColorArgb
            textSize = 28f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.RIGHT
        }

        // Vẽ các mức giá trị trục Y (0, max/2, max)
        val steps = 4
        for (i in 0..steps) {
            val yValue = maxValue * i / steps
            val yPos = height - padding - (i.toFloat() / steps) * chartHeight
            val labelValue = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(yValue.toLong())
            drawContext.canvas.nativeCanvas.drawText(
                labelValue,
                padding - 12f,
                yPos + 10f,
                textPaint
            )
        }

        // Vẽ đường line
        if (data.isNotEmpty()) {
            val path = Path()
            val fillPath = Path()
            val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth / 2f
            
            // Tính toán các index của nhãn dán sẽ được hiển thị (tối đa 5 nhãn)
            val maxLabels = 5
            val labelIndices = mutableListOf<Int>()
            if (data.size <= maxLabels) {
                labelIndices.addAll(data.indices)
            } else {
                val step = (data.size - 1).toFloat() / (maxLabels - 1)
                for (i in 0 until maxLabels) {
                    labelIndices.add(Math.round(i * step).coerceIn(0, data.size - 1))
                }
            }
            
            data.forEachIndexed { index, pair ->
                val x = padding + if (data.size > 1) index * stepX else stepX
                val y = height - padding - (pair.second / maxValue) * chartHeight
                
                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height - padding)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = padding + (index - 1) * stepX
                    val prevPair = data[index - 1]
                    val prevY = height - padding - (prevPair.second / maxValue) * chartHeight
                    
                    val controlX1 = prevX + stepX / 2f
                    val controlY1 = prevY
                    val controlX2 = x - stepX / 2f
                    val controlY2 = y
                    
                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                }
                
                if (index == data.size - 1) {
                    fillPath.lineTo(x, height - padding)
                    fillPath.close()
                }
                
                // Vẽ nhãn trục X (chỉ vẽ các nhãn đã được chọn để tránh đè chữ)
                if (labelIndices.contains(index)) {
                    textPaint.textAlign = Paint.Align.CENTER
                    drawContext.canvas.nativeCanvas.drawText(
                        pair.first,
                        x,
                        height - padding + 35f,
                        textPaint
                    )
                }
            }
            
            if (data.size > 1) {
                drawPath(
                    path = fillPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                        startY = height - padding - chartHeight,
                        endY = height - padding
                    )
                )
                
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 4f)
                )
            }
            
            // Vẽ các điểm đè lên trên cùng
            data.forEachIndexed { index, pair ->
                val x = padding + if (data.size > 1) index * stepX else stepX
                val y = height - padding - (pair.second / maxValue) * chartHeight
                drawCircle(color = surfaceColor, radius = 8f, center = Offset(x, y))
                drawCircle(color = lineColor, radius = 5f, center = Offset(x, y))
            }
        }
    }
}

@Composable
fun HomeHorizontalBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFF97316),
    barHeight: Float = 40f
) {
    if (data.isEmpty() || data.all { it.second == 0f }) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Không có dữ liệu",
                tint = Color.LightGray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Không có dữ liệu", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.second }?.coerceAtLeast(10f) ?: 10f

    val textColorArgb = MaterialTheme.colorScheme.onSurface.run { 
        android.graphics.Color.argb((alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val paddingLeft = 90.dp.toPx() // Chỗ cho Label
        val paddingRight = 40.dp.toPx()
        val chartWidth = width - paddingLeft - paddingRight
        
        val maxItemHeight = 60.dp.toPx()
        val itemHeight = minOf(size.height / data.size, maxItemHeight)
        val totalHeight = itemHeight * data.size
        val startY = (size.height - totalHeight) / 2f
        
        val textPaintLabel = Paint().apply {
            color = textColorArgb
            textSize = 28f
            textAlign = Paint.Align.RIGHT
        }
        
        val textPaintValue = Paint().apply {
            color = textColorArgb
            textSize = 28f
            textAlign = Paint.Align.LEFT
        }

        data.forEachIndexed { index, pair ->
            val y = startY + index * itemHeight + itemHeight / 2f
            
            // Tên nhãn (Label)
            val labelStr = if (pair.first.length > 15) pair.first.take(12) + "..." else pair.first
            drawContext.canvas.nativeCanvas.drawText(
                labelStr,
                paddingLeft - 20f,
                y + 10f,
                textPaintLabel
            )
            
            val corner = 6.dp.toPx()
            val actualBarHeight = itemHeight * 0.8f // Tăng độ dày cột lên 80% không gian
            
            // Vẽ Bar (bo cong toàn bộ)
            val barW = (pair.second / maxValue) * chartWidth
            drawRoundRect(
                color = barColor,
                topLeft = Offset(paddingLeft, y - actualBarHeight / 2),
                size = Size(barW, actualBarHeight),
                cornerRadius = CornerRadius(corner, corner)
            )
            
            // Vẽ đè một hình chữ nhật nhỏ lên phần bên trái để làm cho nó vuông góc (không bo cong bên phía nhãn)
            if (barW > corner) {
                drawRect(
                    color = barColor,
                    topLeft = Offset(paddingLeft, y - actualBarHeight / 2),
                    size = Size(corner, actualBarHeight)
                )
            }
            
            // Vẽ giá trị
            val valueStr = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(pair.second.toLong())
            drawContext.canvas.nativeCanvas.drawText(
                valueStr,
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
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                
                var textSize by remember(value) { mutableStateOf(24.sp) }
                var readyToDraw by remember(value) { mutableStateOf(false) }
                
                Text(
                    text = value, 
                    color = iconColor, 
                    fontSize = textSize, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.drawWithContent {
                        if (readyToDraw) drawContent()
                    },
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.didOverflowWidth) {
                            textSize = textSize * 0.9f
                        } else {
                            readyToDraw = true
                        }
                    }
                )
            }
        }
    }
}
