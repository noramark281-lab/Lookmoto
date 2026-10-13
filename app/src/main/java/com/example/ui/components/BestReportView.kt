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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Hub
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
import com.example.ui.theme.CryptoOrange
import com.example.ui.theme.CryptoRed
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Consolidated Matrix for all 5 timeframes: 5m, 10m, 30m, 1h, 1d
        AllTimeframesInstantMatrix(
            multiSignals = multiSignals,
            selectedTimeframe = selectedTimeframe,
            onSelectTimeframe = onSelectTimeframe
        )

        // Compact details strip for the selected timeframe
        if (selectedReport != null) {
            SelectedTimeframeCompactStrip(
                report = selectedReport,
                currentPrice = currentPrice
            )
        }

        // Institutional Consensus Attribution Card
        if (selectedReport != null) {
            InstitutionalConsensusCard(report = selectedReport)
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
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = CryptoCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "تقرير الاتجاه اللحظي لكل إطار زمني",
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
private fun SelectedTimeframeCompactStrip(
    report: InstantFlashReport,
    currentPrice: Double
) {
    val isBullish = report.isBullish
    val primaryColor = if (isBullish) CryptoGreen else CryptoRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .testTag("selected_timeframe_strip"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("الإطار المختار", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = report.timeframe.labelArabic,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyan
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("السعر المستهدف", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = "$${formatPrice(report.targetPrice)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("وقف الخسارة", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = "$${formatPrice(report.stopLoss)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoOrange
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("دقة التوافق", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = "${report.probabilityAccuracy}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryColor
                )
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
        Column(modifier = Modifier.padding(12.dp)) {
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
                    text = "توافق المنصات العالمية ومصادر التقرير",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = report.primarySourceConsensusArabic,
                fontSize = 11.sp,
                color = CryptoCyan,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = report.institutionalInsightArabic,
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    return String.format("%,.1f", price)
}
