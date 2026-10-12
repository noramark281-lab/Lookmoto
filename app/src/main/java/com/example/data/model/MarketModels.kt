package com.example.data.model

enum class ExchangeSource(val displayName: String, val code: String) {
    BINANCE("Binance Spot", "BINANCE"),
    MEXC("MEXC Spot", "MEXC")
}

enum class Timeframe(val label: String, val binanceInterval: String, val mexcInterval: String, val durationMs: Long) {
    ONE_MIN("1m", "1m", "1m", 60_000L),
    FIVE_MIN("5m", "5m", "5m", 300_000L),
    THIRTY_MIN("30m", "30m", "30m", 1_800_000L)
}

data class Candle(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val closeTime: Long
) {
    val isBullish: Boolean get() = close >= open
    val bodyHeight: Double get() = kotlin.math.abs(close - open)
    val totalHeight: Double get() = high - low
}

data class TickerData(
    val symbol: String = "BTCUSDT",
    val price: Double = 0.0,
    val priceChange: Double = 0.0,
    val priceChangePercent: Double = 0.0,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume24h: Double = 0.0,
    val quoteVolume24h: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class OrderBookEntry(
    val price: Double,
    val quantity: Double
) {
    val total: Double get() = price * quantity
}

data class OrderBookData(
    val bids: List<OrderBookEntry> = emptyList(),
    val asks: List<OrderBookEntry> = emptyList(),
    val lastUpdateId: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    val highestBid: Double get() = bids.firstOrNull()?.price ?: 0.0
    val lowestAsk: Double get() = asks.firstOrNull()?.price ?: 0.0
    val spread: Double get() = if (lowestAsk > 0 && highestBid > 0) lowestAsk - highestBid else 0.0
    val spreadPercent: Double get() = if (highestBid > 0) (spread / highestBid) * 100 else 0.0
    val totalBidVolume: Double get() = bids.sumOf { it.quantity }
    val totalAskVolume: Double get() = asks.sumOf { it.quantity }
    val bidAskRatio: Double get() = if (totalAskVolume > 0) totalBidVolume / totalAskVolume else 1.0
}

data class AggTrade(
    val tradeId: Long,
    val price: Double,
    val quantity: Double,
    val time: Long,
    val isBuyerMaker: Boolean // true: sell taker (bearish execution), false: buy taker (bullish execution)
) {
    val isBuyTaker: Boolean get() = !isBuyerMaker
    val tradeVolumeUsdt: Double get() = price * quantity
}

enum class ConnectionStatus(val label: String) {
    CONNECTED("Live WebSocket"),
    CONNECTING("Connecting..."),
    DISCONNECTED("Offline / Fallback"),
    ERROR("Connection Error")
}
