package com.example.data.model

data class MacdResult(
    val macd: Double,
    val signal: Double,
    val histogram: Double
) {
    val isBullishCrossover: Boolean get() = macd > signal
    val isHistogramPositive: Boolean get() = histogram > 0
}

data class BollingerBandsResult(
    val upper: Double,
    val middle: Double,
    val lower: Double,
    val bandwidth: Double,
    val percentB: Double
) {
    val isOverbought: Boolean get() = percentB >= 1.0
    val isOversold: Boolean get() = percentB <= 0.0
    val isSqueeze: Boolean get() = bandwidth < 2.5
}

data class TechnicalIndicators(
    val rsi: Double? = null,
    val macd: MacdResult? = null,
    val ema9: Double? = null,
    val ema21: Double? = null,
    val ema50: Double? = null,
    val bollingerBands: BollingerBandsResult? = null,
    val atr: Double? = null
) {
    val isEmaBullishStack: Boolean
        get() = (ema9 != null && ema21 != null && ema50 != null) && (ema9 > ema21 && ema21 > ema50)

    val isEmaBearishStack: Boolean
        get() = (ema9 != null && ema21 != null && ema50 != null) && (ema9 < ema21 && ema21 < ema50)
}

enum class SignalType(val title: String, val isBullish: Boolean, val scoreRange: IntRange) {
    STRONG_BUY("STRONG BUY", true, 80..100),
    BUY("BUY", true, 60..79),
    NEUTRAL("NEUTRAL", true, 41..59),
    SELL("SELL", false, 21..40),
    STRONG_SELL("STRONG SELL", false, 0..20)
}

enum class TrendDirection(val label: String) {
    STRONG_UPTREND("Strong Uptrend"),
    UPTREND("Uptrend"),
    SIDEWAYS("Consolidation"),
    DOWNTREND("Downtrend"),
    STRONG_DOWNTREND("Strong Downtrend")
}

enum class MomentumState(val label: String) {
    OVERBOUGHT("Overbought (>70)"),
    BULLISH_MOMENTUM("Bullish Momentum"),
    BALANCED("Neutral Zone"),
    BEARISH_MOMENTUM("Bearish Momentum"),
    OVERSOLD("Oversold (<30)")
}

data class RiskManagementLevels(
    val currentPrice: Double,
    val supportLevel: Double,
    val resistanceLevel: Double,
    val recommendedEntry: Double,
    val stopLoss: Double,
    val stopLossPercent: Double,
    val takeProfit1: Double,
    val takeProfit1Percent: Double,
    val takeProfit2: Double,
    val takeProfit2Percent: Double,
    val takeProfit3: Double,
    val takeProfit3Percent: Double,
    val riskRewardRatio: Double,
    val atr: Double
)

data class SignalScore(
    val probabilityBullish: Int,
    val probabilityBearish: Int,
    val signalType: SignalType,
    val trendDirection: TrendDirection,
    val momentumState: MomentumState,
    val confluenceScore: Double, // 1.0 to 10.0
    val keyConfluences: List<String>,
    val riskManagement: RiskManagementLevels?,
    val timeframe: Timeframe,
    val calculatedAt: Long = System.currentTimeMillis()
)

data class MultiTimeframeSignals(
    val signalsByTimeframe: Map<Timeframe, SignalScore> = emptyMap(),
    val overallBullishProbability: Int = 50,
    val overallSignalType: SignalType = SignalType.NEUTRAL
)
