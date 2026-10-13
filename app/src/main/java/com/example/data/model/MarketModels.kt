package com.example.data.model

enum class ExchangeSource(val displayName: String, val code: String) {
    BINANCE("منصة بينانس (Binance Spot)", "BINANCE"),
    MEXC("منصة إم إي إكس سي (MEXC Spot)", "MEXC"),
    BYBIT("منصة بايبت (Bybit Spot)", "BYBIT")
}

enum class Timeframe(
    val labelArabic: String,
    val shortLabel: String,
    val binanceInterval: String,
    val mexcInterval: String,
    val bybitInterval: String,
    val durationMs: Long
) {
    ONE_MIN("1 دقيقة", "1m", "1m", "1m", "1", 60_000L),
    FIVE_MIN("5 دقائق", "5m", "5m", "5m", "5", 300_000L),
    TEN_MIN("10 دقائق", "10m", "15m", "15m", "15", 600_000L),
    FIFTEEN_MIN("15 دقيقة", "15m", "15m", "15m", "15", 900_000L),
    THIRTY_MIN("30 دقيقة", "30m", "30m", "30m", "30", 1_800_000L),
    ONE_HOUR("1 ساعة", "1h", "1h", "1h", "60", 3_600_000L),
    ONE_DAY("1 يوم", "1d", "1d", "1d", "D", 86_400_000L)
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
    val isBuyerMaker: Boolean // true: sell taker, false: buy taker
) {
    val isBuyTaker: Boolean get() = !isBuyerMaker
    val tradeVolumeUsdt: Double get() = price * quantity
}

enum class ConnectionStatus(val labelArabic: String) {
    CONNECTED("متصل بالبث اللحظي المباشر"),
    CONNECTING("جارٍ الاتصال بالمنصة..."),
    DISCONNECTED("وضع عدم الاتصال / احتياطي"),
    ERROR("خطأ في الاتصال بالشبكة")
}

data class ConnectionHeartbeat(
    val exchange: ExchangeSource = ExchangeSource.BINANCE,
    val latencyMs: Long = 26L,
    val lastOfficialTickTime: Long = System.currentTimeMillis(),
    val isStale: Boolean = false,
    val packetCount: Long = 0L,
    val reconnectCount: Int = 0,
    val statusMessageArabic: String = "الاتصال الرسمي مستقر وسريع دون انقطاع"
)

enum class NewsSentiment(val labelArabic: String, val badgeColorHex: Long) {
    BULLISH("إيجابي صاعد 🟢", 0xFF00E676),
    BEARISH("سلبي هابط 🔴", 0xFFFF3D57),
    NEUTRAL("محايد ⚪", 0xFF9E9E9E)
}

data class CryptoNewsItem(
    val id: String,
    val titleArabic: String,
    val titleOriginal: String,
    val sourceName: String,
    val sourceUrl: String,
    val publishedTimeMs: Long,
    val sentiment: NewsSentiment,
    val summaryArabic: String,
    val isFlashWire: Boolean = false
)

data class MarketSentiment(
    val fearAndGreedIndex: Int = 64,
    val fearAndGreedLabelArabic: String = "طمع معتدل (تفاؤل بالصعود)",
    val newsBullishPercentage: Int = 70,
    val newsBearishPercentage: Int = 30,
    val positiveNewsCount: Int = 14,
    val negativeNewsCount: Int = 4,
    val totalAnalyzedArticles: Int = 22,
    val overallSentimentArabic: String = "اتجاه عام إيجابي داعم لارتفاع البيتكوين"
)

data class DerivativesFlowData(
    val fundingRatePercent: Double = 0.0100, // standard 0.01%
    val fundingRateStatusArabic: String = "معدل تمويل متوازن (0.010%) - لا ضغوط تصفية فورية",
    val openInterestEstimatedUsd: String = "$34.8B",
    val openInterestDelta24h: Double = +2.8,
    val liquidationDominanceArabic: String = "تصفية مراكز البيع (Short Squeeze) تدعم استمرار الصعود",
    val shortLiquidationEstimatedUsd: String = "$68.4M",
    val longLiquidationEstimatedUsd: String = "$18.2M",
    val bidLiquidityWallPrice: Double = 0.0,
    val askLiquidityWallPrice: Double = 0.0
)

data class TradingViewGauge(
    val summaryRatingArabic: String = "شراء قوي (Strong Buy)",
    val oscillatorsRatingArabic: String = "شراء (Buy)",
    val movingAveragesRatingArabic: String = "شراء قوي (Strong Buy)",
    val buyCount: Int = 16,
    val neutralCount: Int = 4,
    val sellCount: Int = 2
)

data class InstantFlashReport(
    val timeframe: Timeframe,
    val bestReportTitleArabic: String,
    val expectedDirectionArabic: String, // "ارتفاع البيتكوين مقابل USDT 📈"
    val isBullish: Boolean,
    val probabilityAccuracy: Int, // e.g. 91%
    val targetPrice: Double,
    val stopLoss: Double,
    val expectedPriceRangeLow: Double,
    val expectedPriceRangeHigh: Double,
    val primarySourceConsensusArabic: String,
    val institutionalInsightArabic: String,
    val reliabilityRatingArabic: String
)
