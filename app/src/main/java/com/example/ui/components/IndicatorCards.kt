package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TechnicalIndicators
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardDark
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoPurple
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun IndicatorDashboard(
    indicators: TechnicalIndicators,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("indicator_dashboard_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: RSI Card & MACD Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RsiCard(
                rsi = indicators.rsi,
                modifier = Modifier.weight(1f)
            )
            MacdCard(
                macd = indicators.macd,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: EMA Ribbon Card
        EmaRibbonCard(
            ema9 = indicators.ema9,
            ema21 = indicators.ema21,
            ema50 = indicators.ema50,
            currentPrice = currentPrice
        )

        // Row 3: Bollinger Bands Card
        BollingerBandsCard(
            bb = indicators.bollingerBands,
            currentPrice = currentPrice
        )
    }
}

@Composable
fun RsiCard(
    rsi: Double?,
    modifier: Modifier = Modifier
) {
    val rsiVal = rsi ?: 50.0
    val statusText = when {
        rsiVal >= 70.0 -> "Overbought"
        rsiVal <= 30.0 -> "Oversold"
        rsiVal >= 55.0 -> "Bullish Momentum"
        rsiVal <= 45.0 -> "Bearish Momentum"
        else -> "Neutral Zone"
    }

    val statusColor = when {
        rsiVal >= 70.0 -> CryptoRed
        rsiVal <= 30.0 -> CryptoCyan
        rsiVal >= 55.0 -> CryptoGreen
        rsiVal <= 45.0 -> CryptoYellow
        else -> TextSecondary
    }

    IndicatorCardContainer(
        title = "RSI (14)",
        icon = Icons.Default.Speed,
        modifier = modifier.testTag("rsi_card")
    ) {
        Text(
            text = String.format("%.1f", rsiVal),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // RSI Visual Meter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CryptoCardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((rsiVal / 100.0).toFloat().coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(statusColor)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("30 OS", fontSize = 9.sp, color = TextMuted)
            Text(statusText, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Medium)
            Text("70 OB", fontSize = 9.sp, color = TextMuted)
        }
    }
}

@Composable
fun MacdCard(
    macd: com.example.data.model.MacdResult?,
    modifier: Modifier = Modifier
) {
    val macdVal = macd?.macd ?: 0.0
    val signalVal = macd?.signal ?: 0.0
    val histVal = macd?.histogram ?: 0.0

    val isBullish = macdVal > signalVal
    val histColor = if (histVal >= 0) CryptoGreen else CryptoRed

    IndicatorCardContainer(
        title = "MACD (12, 26, 9)",
        icon = Icons.Default.ShowChart,
        modifier = modifier.testTag("macd_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = String.format("%+.1f", histVal),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = histColor
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isBullish) CryptoGreen.copy(alpha = 0.2f) else CryptoRed.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isBullish) "BULL CROSS" else "BEAR CROSS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBullish) CryptoGreen else CryptoRed
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("MACD", fontSize = 9.sp, color = TextMuted)
                Text(String.format("%.1f", macdVal), fontSize = 11.sp, color = CryptoCyan, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Signal", fontSize = 9.sp, color = TextMuted)
                Text(String.format("%.1f", signalVal), fontSize = 11.sp, color = CryptoYellow, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun EmaRibbonCard(
    ema9: Double?,
    ema21: Double?,
    ema50: Double?,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    val isBullishStack = ema9 != null && ema21 != null && ema50 != null && (ema9 > ema21 && ema21 > ema50)
    val isBearishStack = ema9 != null && ema21 != null && ema50 != null && (ema9 < ema21 && ema21 < ema50)

    IndicatorCardContainer(
        title = "EMA Trend Ribbon",
        icon = Icons.Default.Timeline,
        modifier = modifier.testTag("ema_ribbon_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                EmaItem(label = "EMA 9", value = ema9, color = CryptoCyan)
                EmaItem(label = "EMA 21", value = ema21, color = CryptoYellow)
                EmaItem(label = "EMA 50", value = ema50, color = CryptoPurple)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            isBullishStack -> CryptoGreen.copy(alpha = 0.2f)
                            isBearishStack -> CryptoRed.copy(alpha = 0.2f)
                            else -> CryptoCardBorder
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when {
                        isBullishStack -> "BULL STACK"
                        isBearishStack -> "BEAR STACK"
                        else -> "MIXED ALIGN"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isBullishStack -> CryptoGreen
                        isBearishStack -> CryptoRed
                        else -> TextSecondary
                    }
                )
            }
        }
    }
}

@Composable
private fun EmaItem(label: String, value: Double?, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 9.sp, color = TextMuted)
        }
        Text(
            text = if (value != null) String.format("%,.0f", value) else "--",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
fun BollingerBandsCard(
    bb: com.example.data.model.BollingerBandsResult?,
    currentPrice: Double,
    modifier: Modifier = Modifier
) {
    val upper = bb?.upper ?: 0.0
    val middle = bb?.middle ?: 0.0
    val lower = bb?.lower ?: 0.0
    val bandwidth = bb?.bandwidth ?: 0.0
    val percentB = bb?.percentB ?: 0.5

    IndicatorCardContainer(
        title = "Bollinger Bands (20, 2.0σ)",
        icon = Icons.Default.TrendingUp,
        modifier = modifier.testTag("bollinger_bands_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Upper Band", fontSize = 9.sp, color = TextMuted)
                Text(String.format("%,.1f", upper), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CryptoRed)
            }
            Column {
                Text("Basis (SMA20)", fontSize = 9.sp, color = TextMuted)
                Text(String.format("%,.1f", middle), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CryptoYellow)
            }
            Column {
                Text("Lower Band", fontSize = 9.sp, color = TextMuted)
                Text(String.format("%,.1f", lower), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CryptoGreen)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Bandwidth", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = String.format("%.2f%%", bandwidth),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (bb?.isSqueeze == true) CryptoYellow else TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Position %B Progress
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Channel Position (%B)", fontSize = 10.sp, color = TextSecondary)
                Text(String.format("%.2f", percentB), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CryptoCyan)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CryptoCardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentB.toFloat().coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(CryptoCyan)
                )
            }
        }
    }
}

@Composable
private fun IndicatorCardContainer(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CryptoCyan,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
