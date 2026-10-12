package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.MultiTimeframeSignals
import com.example.data.model.Timeframe
import com.example.data.model.TimeframePrediction
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
fun PredictionsView(
    multiSignals: MultiTimeframeSignals,
    selectedTimeframe: Timeframe,
    currentPrice: Double,
    onSelectTimeframe: (Timeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    val prediction = multiSignals.predictionsByTimeframe[selectedTimeframe]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("predictions_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Timeframe Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Timeframe.values().forEach { tf ->
                val isSelected = tf == selectedTimeframe
                val chipBg = if (isSelected) CryptoCyan.copy(alpha = 0.2f) else CryptoCardDark
                val borderColor = if (isSelected) CryptoCyan else CryptoCardBorder
                val textColor = if (isSelected) CryptoCyan else TextMuted

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBg)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .clickable { onSelectTimeframe(tf) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tf.labelArabic,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Hero Prediction Card
        if (prediction != null) {
            HeroPredictionCard(
                prediction = prediction,
                currentPrice = currentPrice
            )
        }

        // Multi-Timeframe Summary Grid
        AllTimeframesGrid(
            multiSignals = multiSignals,
            selectedTimeframe = selectedTimeframe,
            onSelectTimeframe = onSelectTimeframe
        )

        // Confluences and Justification Breakdown
        if (prediction != null) {
            PredictionConfluencesCard(prediction = prediction)
        }
    }
}

@Composable
private fun HeroPredictionCard(
    prediction: TimeframePrediction,
    currentPrice: Double
) {
    val isBullish = prediction.isBullish
    val primaryColor = if (isBullish) CryptoGreen else CryptoRed
    val glowColor = if (isBullish) CryptoGreenGlow else CryptoRedGlow

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag("hero_prediction_card"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header: Timeframe + Confidence Badge
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
                        contentDescription = "تنبؤ ذكي",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "التوقع اللحظي لإطار ${prediction.timeframe.labelArabic}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(primaryColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = prediction.confidenceArabic,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direction Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.1f))
                    .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBullish) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = prediction.expectedDirectionArabic,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = prediction.expectedDirectionArabic,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor
                        )
                        Text(
                            text = if (isBullish) "زخم تصاعدي وإشارات شراء قوية" else "ضغط بيعي وتصحيح سعري مرجح",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isBullish) prediction.riseProbability else prediction.fallProbability}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryColor
                    )
                    Text(
                        text = "نسبة الاحتمال",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Probability Distribution Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "احتمال الصعود: ${prediction.riseProbability}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoGreen
                    )
                    Text(
                        text = "احتمال الهبوط: ${prediction.fallProbability}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoRed
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(prediction.riseProbability.toFloat())
                            .fillMaxWidth()
                            .background(CryptoGreen)
                    )
                    Box(
                        modifier = Modifier
                            .weight(prediction.fallProbability.toFloat())
                            .fillMaxWidth()
                            .background(CryptoRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Targets & Price Levels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CryptoBgDark)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("السعر المستهدف", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "$${formatPrice(prediction.targetPrice)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
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
                        text = "$${formatPrice(prediction.stopLoss)}",
                        fontSize = 13.sp,
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
                    Text("نطاق التذبذب المتوقع", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "$${formatPrice(prediction.expectedPriceRangeLow)} - $${formatPrice(prediction.expectedPriceRangeHigh)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun AllTimeframesGrid(
    multiSignals: MultiTimeframeSignals,
    selectedTimeframe: Timeframe,
    onSelectTimeframe: (Timeframe) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نظرة مقارنة شاملة لجميع الأطر الزمنية",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "اضغط للمعاينة",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Timeframe.values().forEach { tf ->
                    val pred = multiSignals.predictionsByTimeframe[tf] ?: return@forEach
                    val isSelected = tf == selectedTimeframe
                    val isBullish = pred.isBullish
                    val color = if (isBullish) CryptoGreen else CryptoRed

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) color.copy(alpha = 0.12f) else CryptoBgDark)
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
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isBullish) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = tf.labelArabic,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = pred.expectedDirectionArabic,
                                    fontSize = 10.sp,
                                    color = color
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isBullish) pred.riseProbability else pred.fallProbability}% احتمال",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                                Text(
                                    text = "الهدف: $${formatPrice(pred.targetPrice)}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionConfluencesCard(prediction: TimeframePrediction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = CryptoCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "أسباب ومبررات التنبؤ اللحظي (${prediction.timeframe.labelArabic})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prediction.keyIndicatorsArabic.forEach { factor ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CryptoCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = factor,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prediction.technicalSummaryArabic,
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    return String.format("%,.1f", price)
}
