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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    val selectedReport = multiSignals.flashReportsByTimeframe[selectedTimeframe]
        ?: multiSignals.bestOverallInstantReport

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("best_report_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Featured Selected Instant Flash Report Card
        if (selectedReport != null) {
            SelectedFlashReportCard(
                report = selectedReport,
                currentPrice = currentPrice
            )
        }

        // 2. Comprehensive Instant Trend Matrix for all timeframes
        AllTimeframesInstantMatrix(
            multiSignals = multiSignals,
            selectedTimeframe = selectedTimeframe,
            onSelectTimeframe = onSelectTimeframe
        )

        // 3. Global Platforms & Institutional Consensus Card
        if (selectedReport != null) {
            InstitutionalConsensusCard(report = selectedReport)
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
    val priceDiff = kotlin.math.abs(report.targetPrice - currentPrice)
    val priceDiffPercent = if (currentPrice > 0) (priceDiff / currentPrice) * 100 else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(primaryColor, glowColor.copy(alpha = 0.4f))),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("selected_flash_report_card"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Timeframe title and consensus accuracy
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "التقرير اللحظي المختار (${report.timeframe.labelArabic})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(primaryColor.copy(alpha = 0.16f))
                        .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "دقة ${report.probabilityAccuracy}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor
                        )
                    }
                }
            }

            // Direction Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(primaryColor.copy(alpha = 0.12f))
                    .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBullish) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isBullish) "ارتفاع البيتكوين (Bullish) 📈" else "هبوط البيتكوين (Bearish) 📉",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = primaryColor
                            )
                            Text(
                                text = if (isBullish) "انخفاض قوة USDT لصالح صعود البيتكوين" else "ارتفاع قوة USDT وتراجع سعر البيتكوين",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isBullish) "+${String.format("%,.1f", priceDiff)} $" else "-${String.format("%,.1f", priceDiff)} $",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = String.format("%.2f%% المتوقع", priceDiffPercent),
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Key Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Target Price Card
                MetricBox(
                    title = "السعر المستهدف",
                    value = "$${formatPrice(report.targetPrice)}",
                    accentColor = primaryColor,
                    modifier = Modifier.weight(1f)
                )

                // Stop Loss Card
                MetricBox(
                    title = "وقف الخسارة",
                    value = "$${formatPrice(report.stopLoss)}",
                    accentColor = CryptoOrange,
                    modifier = Modifier.weight(1f)
                )

                // Risk:Reward Card
                MetricBox(
                    title = "العائد للمخاطرة",
                    value = "1 : 2.5",
                    accentColor = CryptoCyan,
                    modifier = Modifier.weight(1f)
                )

                // Time Horizon Card
                MetricBox(
                    title = "المدى الزمني",
                    value = report.timeframe.shortLabel,
                    accentColor = CryptoYellow,
                    modifier = Modifier.weight(1f)
                )
            }

            // Consensus and Action Advice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CryptoBgDark)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = CryptoCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "توصية التحرك اللحظي:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoCyan
                        )
                    }
                    Text(
                        text = if (isBullish) {
                            "شراء مع تأكيد الزخم الصاعد وتصفية عقود البيع، وجني أرباح عند مستويات $${formatPrice(report.targetPrice)}"
                        } else {
                            "بيع وتأمين الأرباح تحسباً لضغط السيولة وكسر مستويات الدعم حتى $${formatPrice(report.targetPrice)}"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CryptoBgDark)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 9.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
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
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp))
            .testTag("all_timeframes_instant_matrix"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = CryptoCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "مصفوفة الاتجاه اللحظي لجميع الأطر الزمنية",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "اضغط للمعاينة",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Timeframe.values().forEach { tf ->
                    val item = multiSignals.flashReportsByTimeframe[tf]
                    val isSelected = tf == selectedTimeframe
                    val isBullish = item?.isBullish ?: true
                    val color = if (isBullish) CryptoGreen else CryptoRed

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) color.copy(alpha = 0.14f) else CryptoBgDark)
                            .border(1.dp, if (isSelected) color else CryptoCardBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectTimeframe(tf) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = if (isBullish) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isBullish) "صعود البيتكوين 📈" else "هبوط البيتكوين 📉",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = color
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (item != null) {
                                Text(
                                    text = "$${formatPrice(item.targetPrice)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color.copy(alpha = 0.18f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${item.probabilityAccuracy}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = color
                                    )
                                }
                            }
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
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = null,
                    tint = CryptoCyan,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "توافق المنصات العالمية ومصادر التقرير اللحظي",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Institutional Sources Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SourceBadge(title = "Tree News", subtitle = "عواجل لحظية", color = CryptoCyan, modifier = Modifier.weight(1f))
                SourceBadge(title = "TradingView", subtitle = "مؤشرات فنية", color = CryptoGreen, modifier = Modifier.weight(1f))
                SourceBadge(title = "CoinGlass", subtitle = "مشتقات وسيولة", color = CryptoOrange, modifier = Modifier.weight(1f))
                SourceBadge(title = "Binance/Bybit", subtitle = "عمق الأوامر", color = CryptoYellow, modifier = Modifier.weight(1f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CryptoBgDark)
                    .padding(8.dp)
            ) {
                Text(
                    text = report.primarySourceConsensusArabic,
                    fontSize = 11.sp,
                    color = CryptoCyan,
                    lineHeight = 15.sp
                )
            }

            Text(
                text = report.institutionalInsightArabic,
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SourceBadge(
    title: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CryptoBgDark)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 8.sp,
                color = TextMuted
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    return String.format("%,.1f", price)
}
