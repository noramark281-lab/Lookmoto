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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoNewsItem
import com.example.data.model.MarketSentiment
import com.example.data.model.NewsSentiment
import com.example.ui.theme.CryptoBgDark
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardDark
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenGlow
import com.example.ui.theme.CryptoOrange
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NewsSentimentView(
    marketSentiment: MarketSentiment,
    newsFeed: List<CryptoNewsItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("news_sentiment_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Market Sentiment & Fear & Greed Card
        SentimentMetricsCard(sentiment = marketSentiment)

        // News Feed Card
        NewsFeedListCard(newsFeed = newsFeed)
    }
}

@Composable
private fun SentimentMetricsCard(sentiment: MarketSentiment) {
    val fearGreedColor = when {
        sentiment.fearAndGreedIndex >= 60 -> CryptoGreen
        sentiment.fearAndGreedIndex in 45..59 -> CryptoYellow
        else -> CryptoRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp)),
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
                        contentDescription = "مؤشرات المشاعر",
                        tint = CryptoYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "نبض المشاعر العامة لأسواق البيتكوين",
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
                        text = "رصد لحظي مباشر",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fear and Greed Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CryptoBgDark)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مؤشر الخوف والطمع العالمي",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = sentiment.fearAndGreedLabelArabic,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = fearGreedColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(fearGreedColor.copy(alpha = 0.15f))
                        .border(1.5.dp, fearGreedColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${sentiment.fearAndGreedIndex}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = fearGreedColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // News Bullish / Bearish Ratio Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "أخبار إيجابية صاعدة: ${sentiment.newsBullishPercentage}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoGreen
                    )
                    Text(
                        text = "أخبار سلبية هابطة: ${sentiment.newsBearishPercentage}%",
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
                            .weight(sentiment.newsBullishPercentage.toFloat())
                            .fillMaxWidth()
                            .background(CryptoGreen)
                    )
                    Box(
                        modifier = Modifier
                            .weight(sentiment.newsBearishPercentage.toFloat())
                            .fillMaxWidth()
                            .background(CryptoRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = sentiment.overallSentimentArabic,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun NewsFeedListCard(newsFeed: List<CryptoNewsItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp)),
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
                        imageVector = Icons.Default.Newspaper,
                        contentDescription = "آخر الأخبار",
                        tint = CryptoCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "أحدث أخبار وتقارير الويب عن البيتكوين",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "${newsFeed.size} تقارير",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                newsFeed.forEach { item ->
                    NewsItemRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun NewsItemRow(item: CryptoNewsItem) {
    val badgeColor = Color(item.sentiment.badgeColorHex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CryptoBgDark)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Source and Sentiment Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = CryptoCyan,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = item.sourceName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyan
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = item.sentiment.labelArabic,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
        }

        // Title Arabic
        Text(
            text = item.titleArabic,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 18.sp
        )

        // Summary
        Text(
            text = item.summaryArabic,
            fontSize = 10.sp,
            color = TextMuted,
            lineHeight = 14.sp
        )
    }
}
