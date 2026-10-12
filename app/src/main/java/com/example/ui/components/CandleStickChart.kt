package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.IndicatorEngine
import com.example.data.model.Candle
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoPurple
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandleStickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    visibleCandleCount: Int = 38,
    onCandleSelected: (Candle?) -> Unit = {}
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Connecting to market feed...",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val displayCandles = remember(candles, visibleCandleCount) {
        candles.takeLast(visibleCandleCount)
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Precalculate EMAs for the full candle list, then take display window
    val closes = remember(candles) { candles.map { it.close } }
    val ema9All = remember(closes) { IndicatorEngine.calculateEMASeries(closes, 9) }
    val ema21All = remember(closes) { IndicatorEngine.calculateEMASeries(closes, 21) }
    val ema50All = remember(closes) { IndicatorEngine.calculateEMASeries(closes, 50) }

    val displayEma9 = remember(ema9All, visibleCandleCount) { ema9All.takeLast(visibleCandleCount) }
    val displayEma21 = remember(ema21All, visibleCandleCount) { ema21All.takeLast(visibleCandleCount) }
    val displayEma50 = remember(ema50All, visibleCandleCount) { ema50All.takeLast(visibleCandleCount) }

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .testTag("candlestick_chart_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(displayCandles) {
                    detectTapGestures(
                        onTap = { offset ->
                            val width = size.width - 55.dp.toPx()
                            val candleWidth = width / displayCandles.size
                            val idx = (offset.x / candleWidth).toInt().coerceIn(0, displayCandles.size - 1)
                            selectedIndex = idx
                            onCandleSelected(displayCandles.getOrNull(idx))
                        }
                    )
                }
                .pointerInput(displayCandles) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width - 55.dp.toPx()
                            val candleWidth = width / displayCandles.size
                            val idx = (offset.x / candleWidth).toInt().coerceIn(0, displayCandles.size - 1)
                            selectedIndex = idx
                            onCandleSelected(displayCandles.getOrNull(idx))
                        },
                        onDragEnd = {
                            selectedIndex = null
                            onCandleSelected(null)
                        },
                        onDragCancel = {
                            selectedIndex = null
                            onCandleSelected(null)
                        },
                        onDrag = { change, _ ->
                            val width = size.width - 55.dp.toPx()
                            val candleWidth = width / displayCandles.size
                            val idx = (change.position.x / candleWidth).toInt().coerceIn(0, displayCandles.size - 1)
                            selectedIndex = idx
                            onCandleSelected(displayCandles.getOrNull(idx))
                        }
                    )
                }
        ) {
            val chartRightMargin = 55.dp.toPx()
            val chartBottomMargin = 22.dp.toPx()
            val chartWidth = size.width - chartRightMargin
            val chartHeight = size.height - chartBottomMargin

            val count = displayCandles.size
            if (count == 0) return@Canvas

            var minPrice = displayCandles.minOf { it.low }
            var maxPrice = displayCandles.maxOf { it.high }

            // Include EMAs in scale boundaries
            displayEma9.forEach { if (it > 0) { minPrice = min(minPrice, it); maxPrice = max(maxPrice, it) } }
            displayEma21.forEach { if (it > 0) { minPrice = min(minPrice, it); maxPrice = max(maxPrice, it) } }
            displayEma50.forEach { if (it > 0) { minPrice = min(minPrice, it); maxPrice = max(maxPrice, it) } }

            val pricePadding = (maxPrice - minPrice) * 0.08
            val priceMin = minPrice - pricePadding
            val priceMax = maxPrice + pricePadding
            val priceRange = max(priceMax - priceMin, 1.0)

            fun getY(price: Double): Float {
                return (chartHeight - ((price - priceMin) / priceRange * chartHeight)).toFloat()
            }

            // Draw horizontal subtle grid lines & price labels
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val gridY = (chartHeight / gridSteps) * i
                drawLine(
                    color = Color(0x18FFFFFF),
                    start = Offset(0f, gridY),
                    end = Offset(chartWidth, gridY),
                    strokeWidth = 1f
                )
            }

            val stepX = chartWidth / count
            val candleBarWidth = stepX * 0.70f

            // Draw Candlesticks
            displayCandles.forEachIndexed { i, candle ->
                val centerX = (i * stepX) + (stepX / 2f)
                val openY = getY(candle.open)
                val closeY = getY(candle.close)
                val highY = getY(candle.high)
                val lowY = getY(candle.low)

                val isGreen = candle.close >= candle.open
                val candleColor = if (isGreen) CryptoGreen else CryptoRed

                // Wick line
                drawLine(
                    color = candleColor,
                    start = Offset(centerX, highY),
                    end = Offset(centerX, lowY),
                    strokeWidth = 1.6f,
                    cap = StrokeCap.Round
                )

                // Body rectangle
                val bodyTop = min(openY, closeY)
                val bodyBottom = max(openY, closeY)
                val bodyHeight = max(abs(bodyBottom - bodyTop), 2.5f)

                drawRect(
                    color = candleColor,
                    topLeft = Offset(centerX - (candleBarWidth / 2f), bodyTop),
                    size = Size(candleBarWidth, bodyHeight)
                )
            }

            // Draw EMA curves
            fun drawEmaLine(emaList: List<Double>, color: Color, strokeWidth: Float = 1.8f) {
                if (emaList.size < 2) return
                val path = Path()
                var started = false

                emaList.forEachIndexed { i, valEma ->
                    if (valEma > 0 && i < count) {
                        val x = (i * stepX) + (stepX / 2f)
                        val y = getY(valEma)
                        if (!started) {
                            path.moveTo(x, y)
                            started = true
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                }
                if (started) {
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            drawEmaLine(displayEma50, CryptoPurple, 1.6f)
            drawEmaLine(displayEma21, CryptoYellow, 1.8f)
            drawEmaLine(displayEma9, CryptoCyan, 2.0f)

            // Draw current price dashed line across chart
            val currentPrice = displayCandles.last().close
            val currentY = getY(currentPrice)
            drawLine(
                color = CryptoGreen.copy(alpha = 0.7f),
                start = Offset(0f, currentY),
                end = Offset(chartWidth, currentY),
                strokeWidth = 1.2f
            )

            // Scrubber guideline if selected
            selectedIndex?.let { idx ->
                if (idx in 0 until count) {
                    val scrubberX = (idx * stepX) + (stepX / 2f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(scrubberX, 0f),
                        end = Offset(scrubberX, chartHeight),
                        strokeWidth = 1.2f
                    )
                }
            }
        }

        // Y-axis price labels
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        ) {
            val high = displayCandles.maxOfOrNull { it.high } ?: 0.0
            val low = displayCandles.minOfOrNull { it.low } ?: 0.0
            val mid = (high + low) / 2.0

            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%,.0f", high),
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = String.format("%,.0f", mid),
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = String.format("%,.0f", low),
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                // Spacer for bottom time axis
                Text(
                    text = "",
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun abs(value: Float): Float = kotlin.math.abs(value)
