package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AggTrade
import com.example.data.model.OrderBookData
import com.example.data.model.OrderBookEntry
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardDark
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun OrderBookAndTradesView(
    orderBook: OrderBookData,
    recentTrades: List<AggTrade>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_book_and_trades_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Order Book Depth Card
        OrderBookDepthCard(orderBook = orderBook)

        // Live Trades Tape Card
        RecentTradesCard(trades = recentTrades)
    }
}

@Composable
fun OrderBookDepthCard(
    orderBook: OrderBookData,
    modifier: Modifier = Modifier
) {
    val topBids = remember(orderBook.bids) { orderBook.bids.take(8) }
    val topAsks = remember(orderBook.asks) { orderBook.asks.take(8) }

    val maxBidQty = remember(topBids) { topBids.maxOfOrNull { it.quantity } ?: 1.0 }
    val maxAskQty = remember(topAsks) { topAsks.maxOfOrNull { it.quantity } ?: 1.0 }
    val maxQty = max(maxBidQty, maxAskQty)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp))
            .testTag("order_book_depth_card"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with Spread & Bid/Ask Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Order Book Depth",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Spread: $${String.format("%.2f", orderBook.spread)}",
                        fontSize = 11.sp,
                        color = CryptoYellow,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Ratio: ${String.format("%.2f", orderBook.bidAskRatio)}x",
                        fontSize = 11.sp,
                        color = if (orderBook.bidAskRatio >= 1.0) CryptoGreen else CryptoRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table Columns Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Bid Price", fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1f))
                Text("Bid Size", fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Ask Price", fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1f))
                Text("Ask Size", fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Depth ladder rows
            val rowCount = max(topBids.size, topAsks.size).coerceAtMost(8)
            for (i in 0 until rowCount) {
                val bid = topBids.getOrNull(i)
                val ask = topAsks.getOrNull(i)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bid Side (Left)
                    Box(modifier = Modifier.weight(2f)) {
                        if (bid != null) {
                            val ratio = (bid.quantity / maxQty).toFloat().coerceIn(0.05f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio)
                                    .align(Alignment.CenterEnd)
                                    .background(CryptoGreen.copy(alpha = 0.15f))
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("%,.1f", bid.price),
                                    fontSize = 11.sp,
                                    color = CryptoGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = String.format("%.4f", bid.quantity),
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Ask Side (Right)
                    Box(modifier = Modifier.weight(2f)) {
                        if (ask != null) {
                            val ratio = (ask.quantity / maxQty).toFloat().coerceIn(0.05f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(ratio)
                                    .align(Alignment.CenterStart)
                                    .background(CryptoRed.copy(alpha = 0.15f))
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("%,.1f", ask.price),
                                    fontSize = 11.sp,
                                    color = CryptoRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = String.format("%.4f", ask.quantity),
                                    fontSize = 10.sp,
                                    color = TextSecondary
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
fun RecentTradesCard(
    trades: List<AggTrade>,
    modifier: Modifier = Modifier
) {
    val displayTrades = remember(trades) { trades.take(12) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp))
            .testTag("recent_trades_card"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "High-Frequency Aggregate Trades",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Live Tape",
                    fontSize = 10.sp,
                    color = CryptoCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Time", fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1f))
                Text("Price (USDT)", fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                Text("Size (BTC)", fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                Text("Side", fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (displayTrades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Streaming trade executions...", fontSize = 11.sp, color = TextMuted)
                }
            } else {
                displayTrades.forEach { trade ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeFormatter.format(Date(trade.time)),
                            fontSize = 10.sp,
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = String.format("%,.1f", trade.price),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (trade.isBuyTaker) CryptoGreen else CryptoRed,
                            modifier = Modifier.weight(1.4f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = String.format("%.4f", trade.quantity),
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.8f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (trade.isBuyTaker) CryptoGreen.copy(alpha = 0.15f) else CryptoRed.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (trade.isBuyTaker) "BUY" else "SELL",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (trade.isBuyTaker) CryptoGreen else CryptoRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
