package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstantFlashReport
import com.example.data.model.MultiTimeframeSignals
import com.example.data.model.Timeframe
import com.example.ui.theme.CryptoBgDark
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardDark
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenGlow
import com.example.ui.theme.CryptoOrange
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoRedGlow
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BestReportView(
    multiSignals: MultiTimeframeSignals,
    selectedTimeframe: Timeframe,
    currentPrice: Double,
    onSelectTimeframe: (Timeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    val report = multiSignals.flashReportsByTimeframe[selectedTimeframe] ?: multiSignals.bestOverallInstantReport

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("best_report_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Multi-Timeframe Selector Chips (1m, 5m, 10m, 15m, 30m, 1h, 1d)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Timeframe.values().forEach { tf ->
                val isSelected = tf == selectedTimeframe
                val chipBg = if (isSelected) CryptoCyan.copy(alpha = 0.22f) else CryptoCardDark
                val borderCol = if (isSelected) CryptoCyan else CryptoCardBorder
                val textCol = if (isSelected) CryptoCyan else TextMuted

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBg)
                        .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                        .clickable { onSelectTimeframe(tf) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tf.shortLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textCol,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Primary Hero Best Flash Report Card
        if (report != null) {
            SelectedFlashReportCard(report = report, currentPrice = currentPrice)
        }

        // All Timeframes Consolidated Instant Comparison
        AllTimeframesInstantMatrix(
            multiSignals = multiSignals,
            selectedTimeframe = selectedTimeframe,
            onSelectTimeframe = onSelectTimeframe
        )

        // Institutional Consensus Attribution Card
        if (report != null) {
            InstitutionalConsensusCard(report = report)
        }
    }
}

@Composable
private fun SelectedFlashReportCard(
    report: InstantFlashReport,
    currentPrice: Double
) {
    val isBullish = report.isBullish
    val primaryColor = if (isBullish) CryptoGreen else CryptoRed
    val glowColor = if (isBullish) CryptoGreenGlow else CryptoRedGlow

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .testTag("selected_flash_report_card"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            glowColor.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Top Badge: Verified Instant Report
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = CryptoYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "التقرير اللحظي المختار لإطار ${report.timeframe.labelArabic}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(primaryColor.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = report.reliabilityRatingArabic,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Direction & Target Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(primaryColor.copy(alpha = 0.12f))
                    .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBullish) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = report.expectedDirectionArabic,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor
                        )
                        Text(
                            text = "السعر المستهدف: $${formatPrice(report.targetPrice)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${report.probabilityAccuracy}%",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryColor
                    )
                    Text(
                        text = "دقة التوافق",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Price Levels Matrix
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CryptoBgDark)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("السعر الحالي", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "$${formatPrice(currentPrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(1.dp)
                        .background(CryptoCardBorder)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("وقف الخسارة", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "$${formatPrice(report.stopLoss)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoOrange
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(1.dp)
                        .background(CryptoCardBorder)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("النطاق المتوقع", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "$${formatPrice(report.expectedPriceRangeLow)} - $${formatPrice(report.expectedPriceRangeHigh)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CryptoCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun AllTimeframesInstantMatrix(
    multiSignals: MultiTimeframeSignals,
    selectedTimeframe: Timeframe,
    onSelectTimeframe: (Timeframe) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "جدول التوقعات اللحظية المباشرة (1m إلى 1d)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "اختر الإطار للمعاينة",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Timeframe.values().forEach { tf ->
                    val item = multiSignals.flashReportsByTimeframe[tf] ?: return@forEach
                    val isSelected = tf == selectedTimeframe
                    val isBullish = item.isBullish
                    val color = if (isBullish) CryptoGreen else CryptoRed

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) color.copy(alpha = 0.14f) else CryptoBgDark)
                            .border(1.dp, if (isSelected) color else CryptoCardBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectTimeframe(tf) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(
                                text = tf.labelArabic,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isBullish) "صعود البيتكوين 📈" else "هبوط البيتكوين 📉",
                                fontSize = 10.sp,
                                color = color
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "الهدف: $${formatPrice(item.targetPrice)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Text(
                                text = "${item.probabilityAccuracy}% دقة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstitutionalConsensusCard(report: InstantFlashReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = null,
                    tint = CryptoCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "توافق المنصات العالمية ومصادر التقرير",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.primarySourceConsensusArabic,
                fontSize = 11.sp,
                color = CryptoCyan,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.institutionalInsightArabic,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    return String.format("%,.1f", price)
}
