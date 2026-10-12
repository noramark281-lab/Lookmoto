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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradingViewGauge
import com.example.ui.theme.CryptoBgDark
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardDark
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TradingViewGaugeCard(
    gauge: TradingViewGauge,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
            .testTag("trading_view_gauge_card"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
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
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = CryptoCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "مؤشرات تريدنج فيو المجمعة (TradingView Gauges)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CryptoCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "BTC/USDT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Overall Summary Meter Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CryptoBgDark)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الملخص الفني المجمع (Technical Summary)",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = gauge.summaryRatingArabic,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = CryptoGreen
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VotePill(count = gauge.buyCount, label = "شراء", color = CryptoGreen)
                    VotePill(count = gauge.neutralCount, label = "حياد", color = CryptoYellow)
                    VotePill(count = gauge.sellCount, label = "بيع", color = CryptoRed)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Oscillators vs Moving Averages row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubGaugeBox(
                    title = "المتوسطات المتحركة (MAs)",
                    rating = gauge.movingAveragesRatingArabic,
                    color = CryptoGreen,
                    modifier = Modifier.weight(1f)
                )

                SubGaugeBox(
                    title = "مؤشرات التذبذب (Oscillators)",
                    rating = gauge.oscillatorsRatingArabic,
                    color = CryptoCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VotePill(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 9.sp, color = TextMuted)
    }
}

@Composable
private fun SubGaugeBox(title: String, rating: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CryptoBgDark)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 10.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = rating,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
