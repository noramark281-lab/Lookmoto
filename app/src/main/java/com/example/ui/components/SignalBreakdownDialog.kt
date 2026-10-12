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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MultiTimeframeSignals
import com.example.data.model.SignalScore
import com.example.data.model.Timeframe
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardDark
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoSurfaceDark
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SignalBreakdownDialog(
    currentSignal: SignalScore?,
    multiTimeframe: MultiTimeframeSignals,
    onDismiss: () -> Unit
) {
    if (currentSignal == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CryptoCardBorder),
                modifier = Modifier.testTag("dismiss_signal_breakdown_button")
            ) {
                Text("إغلاق التفاصيل", color = TextPrimary, fontSize = 12.sp)
            }
        },
        containerColor = CryptoSurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = CryptoCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "تفاصيل المنطق الخوارزمي للإشارة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signal_breakdown_content"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Multi-Timeframe Matrix
                Text(
                    text = "توافق احتمالية الاتجاه عبر الأطر الزمنية",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeframePill(
                        label = "1 دقيقة",
                        score = multiTimeframe.signalsByTimeframe[Timeframe.ONE_MIN]?.probabilityBullish ?: 50,
                        modifier = Modifier.weight(1f)
                    )
                    TimeframePill(
                        label = "5 دقائق",
                        score = multiTimeframe.signalsByTimeframe[Timeframe.FIVE_MIN]?.probabilityBullish ?: 50,
                        modifier = Modifier.weight(1f)
                    )
                    TimeframePill(
                        label = "15 دقيقة",
                        score = multiTimeframe.signalsByTimeframe[Timeframe.FIFTEEN_MIN]?.probabilityBullish ?: 50,
                        modifier = Modifier.weight(1f)
                    )
                    TimeframePill(
                        label = "30 دقيقة",
                        score = multiTimeframe.signalsByTimeframe[Timeframe.THIRTY_MIN]?.probabilityBullish ?: 50,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Confidence Rating
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoCardDark)
                        .border(1.dp, CryptoCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("درجة الثقة الخوارزمية", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = "${currentSignal.confluenceScore} / 10.0",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoYellow
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("مسار الاتجاه", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = currentSignal.trendDirection.labelArabic,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (currentSignal.probabilityBullish >= 50) CryptoGreen else CryptoRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Algorithmic Factor Checklist
                Text(
                    text = "العوامل والمؤشرات الفاعلة (${currentSignal.timeframe.labelArabic})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    currentSignal.keyConfluences.forEach { confluence ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CryptoGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = confluence,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun TimeframePill(
    label: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    val isBullish = score >= 50
    val color = if (isBullish) CryptoGreen else CryptoRed

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CryptoCardDark)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(8.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 8.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$score%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = if (isBullish) "صعود" else "هبوط",
            fontSize = 8.sp,
            color = color.copy(alpha = 0.8f)
        )
    }
}
