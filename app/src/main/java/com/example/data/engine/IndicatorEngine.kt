package com.example.data.engine

import com.example.data.model.BollingerBandsResult
import com.example.data.model.Candle
import com.example.data.model.MacdResult
import com.example.data.model.MomentumState
import com.example.data.model.RiskManagementLevels
import com.example.data.model.SignalScore
import com.example.data.model.SignalType
import com.example.data.model.TechnicalIndicators
import com.example.data.model.Timeframe
import com.example.data.model.TrendDirection
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

object IndicatorEngine {

    fun calculateIndicators(candles: List<Candle>): TechnicalIndicators {
        if (candles.isEmpty()) return TechnicalIndicators()

        val closes = candles.map { it.close }
        val rsi = calculateRSI(closes, 14)
        val macd = calculateMACD(closes, 12, 26, 9)
        val ema9Series = calculateEMASeries(closes, 9)
        val ema21Series = calculateEMASeries(closes, 21)
        val ema50Series = calculateEMASeries(closes, 50)
        val bb = calculateBollingerBands(closes, 20, 2.0)
        val atr = calculateATR(candles, 14)

        return TechnicalIndicators(
            rsi = rsi,
            macd = macd,
            ema9 = ema9Series.lastOrNull(),
            ema21 = ema21Series.lastOrNull(),
            ema50 = ema50Series.lastOrNull(),
            bollingerBands = bb,
            atr = atr
        )
    }

    /**
     * Relative Strength Index (RSI - 14 periods) using Wilder's smoothed method
     */
    fun calculateRSI(prices: List<Double>, period: Int = 14): Double? {
        if (prices.size < period + 1) return null

        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = prices[i] - prices[i - 1]
            if (change > 0) gains += change else losses += abs(change)
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in (period + 1) until prices.size) {
            val change = prices[i] - prices[i - 1]
            val currentGain = if (change > 0) change else 0.0
            val currentLoss = if (change < 0) abs(change) else 0.0

            avgGain = (avgGain * (period - 1) + currentGain) / period
            avgLoss = (avgLoss * (period - 1) + currentLoss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    /**
     * Exponential Moving Average (EMA) series computation
     */
    fun calculateEMASeries(prices: List<Double>, period: Int): List<Double> {
        if (prices.isEmpty()) return emptyList()
        val result = mutableListOf<Double>()
        val multiplier = 2.0 / (period + 1.0)

        var ema = prices.first()
        result.add(ema)

        for (i in 1 until prices.size) {
            val price = prices[i]
            ema = (price * multiplier) + (ema * (1.0 - multiplier))
            result.add(ema)
        }
        return result
    }

    /**
     * Moving Average Convergence Divergence (MACD 12, 26, 9)
     */
    fun calculateMACD(
        prices: List<Double>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): MacdResult? {
        if (prices.size < slowPeriod) return null

        val fastEma = calculateEMASeries(prices, fastPeriod)
        val slowEma = calculateEMASeries(prices, slowPeriod)

        val macdLine = mutableListOf<Double>()
        for (i in prices.indices) {
            macdLine.add(fastEma[i] - slowEma[i])
        }

        val signalLine = calculateEMASeries(macdLine, signalPeriod)

        val currentMacd = macdLine.last()
        val currentSignal = signalLine.last()
        val histogram = currentMacd - currentSignal

        return MacdResult(
            macd = currentMacd,
            signal = currentSignal,
            histogram = histogram
        )
    }

    /**
     * Bollinger Bands (20 periods, 2 standard deviations)
     */
    fun calculateBollingerBands(
        prices: List<Double>,
        period: Int = 20,
        stdDevMultiplier: Double = 2.0
    ): BollingerBandsResult? {
        if (prices.size < period) return null

        val window = prices.takeLast(period)
        val mean = window.average()
        val variance = window.sumOf { (it - mean) * (it - mean) } / period
        val stdDev = sqrt(variance)

        val upper = mean + (stdDevMultiplier * stdDev)
        val lower = mean - (stdDevMultiplier * stdDev)
        val bandwidth = if (mean > 0) ((upper - lower) / mean) * 100.0 else 0.0
        val currentPrice = prices.last()
        val percentB = if (upper != lower) (currentPrice - lower) / (upper - lower) else 0.5

        return BollingerBandsResult(
            upper = upper,
            middle = mean,
            lower = lower,
            bandwidth = bandwidth,
            percentB = percentB
        )
    }

    /**
     * Average True Range (ATR - 14 periods)
     */
    fun calculateATR(candles: List<Candle>, period: Int = 14): Double? {
        if (candles.size < period + 1) return null

        val trueRanges = mutableListOf<Double>()
        for (i in 1 until candles.size) {
            val high = candles[i].high
            val low = candles[i].low
            val prevClose = candles[i - 1].close

            val tr = max(high - low, max(abs(high - prevClose), abs(low - prevClose)))
            trueRanges.add(tr)
        }

        var atr = trueRanges.take(period).average()
        for (i in period until trueRanges.size) {
            atr = (atr * (period - 1) + trueRanges[i]) / period
        }
        return atr
    }

    /**
     * Dynamic Risk Management Matrix: Support/Resistance, SL, TP1, TP2, TP3
     */
    fun calculateRiskManagement(
        currentPrice: Double,
        candles: List<Candle>,
        indicators: TechnicalIndicators,
        isBullish: Boolean
    ): RiskManagementLevels {
        val window = candles.takeLast(min(candles.size, 30))
        val highestHigh = window.maxOfOrNull { it.high } ?: currentPrice
        val lowestLow = window.minOfOrNull { it.low } ?: currentPrice

        // Pivot calculations
        val lastCandle = candles.lastOrNull() ?: Candle(0, currentPrice, currentPrice, currentPrice, currentPrice, 0.0, 0)
        val pivot = (lastCandle.high + lastCandle.low + lastCandle.close) / 3.0
        val pivotR1 = (2.0 * pivot) - lastCandle.low
        val pivotS1 = (2.0 * pivot) - lastCandle.high

        val dynamicResistance = max(highestHigh, indicators.bollingerBands?.upper ?: pivotR1)
        val dynamicSupport = min(lowestLow, indicators.bollingerBands?.lower ?: pivotS1)

        val atr = indicators.atr ?: (currentPrice * 0.008) // Fallback to 0.8% volatility

        val stopLoss: Double
        val stopLossPercent: Double
        val recommendedEntry = currentPrice
        val tp1: Double
        val tp2: Double
        val tp3: Double

        if (isBullish) {
            // Long Setup
            val calculatedSl = currentPrice - (1.5 * atr)
            stopLoss = max(calculatedSl, dynamicSupport * 0.998)
            val riskDistance = max(currentPrice - stopLoss, currentPrice * 0.005)
            stopLossPercent = ((stopLoss - currentPrice) / currentPrice) * 100.0

            tp1 = currentPrice + (1.5 * riskDistance)
            tp2 = currentPrice + (2.5 * riskDistance)
            tp3 = currentPrice + (4.0 * riskDistance)
        } else {
            // Short Setup
            val calculatedSl = currentPrice + (1.5 * atr)
            stopLoss = min(calculatedSl, dynamicResistance * 1.002)
            val riskDistance = max(stopLoss - currentPrice, currentPrice * 0.005)
            stopLossPercent = ((stopLoss - currentPrice) / currentPrice) * 100.0

            tp1 = currentPrice - (1.5 * riskDistance)
            tp2 = currentPrice - (2.5 * riskDistance)
            tp3 = currentPrice - (4.0 * riskDistance)
        }

        val tp1Percent = ((tp1 - currentPrice) / currentPrice) * 100.0
        val tp2Percent = ((tp2 - currentPrice) / currentPrice) * 100.0
        val tp3Percent = ((tp3 - currentPrice) / currentPrice) * 100.0

        return RiskManagementLevels(
            currentPrice = currentPrice,
            supportLevel = dynamicSupport,
            resistanceLevel = dynamicResistance,
            recommendedEntry = recommendedEntry,
            stopLoss = stopLoss,
            stopLossPercent = stopLossPercent,
            takeProfit1 = tp1,
            takeProfit1Percent = tp1Percent,
            takeProfit2 = tp2,
            takeProfit2Percent = tp2Percent,
            takeProfit3 = tp3,
            takeProfit3Percent = tp3Percent,
            riskRewardRatio = 2.5,
            atr = atr
        )
    }

    /**
     * Probabilistic Technical Signal Scoring Engine
     */
    fun evaluateSignal(
        timeframe: Timeframe,
        candles: List<Candle>,
        indicators: TechnicalIndicators
    ): SignalScore {
        val currentPrice = candles.lastOrNull()?.close ?: 0.0
        if (candles.size < 15 || currentPrice == 0.0) {
            return SignalScore(
                probabilityBullish = 50,
                probabilityBearish = 50,
                signalType = SignalType.NEUTRAL,
                trendDirection = TrendDirection.SIDEWAYS,
                momentumState = MomentumState.BALANCED,
                confluenceScore = 5.0,
                keyConfluences = listOf("Awaiting sufficient tick and candle history"),
                riskManagement = null,
                timeframe = timeframe
            )
        }

        val confluences = mutableListOf<String>()
        var bullishPoints = 0
        var bearishPoints = 0
        var totalPointsPossible = 0

        // 1. EMA Trend Stack (30 points weight)
        val ema9 = indicators.ema9
        val ema21 = indicators.ema21
        val ema50 = indicators.ema50
        totalPointsPossible += 30

        if (ema9 != null && ema21 != null) {
            if (ema9 > ema21) {
                bullishPoints += 12
                confluences.add("EMA 9 ($${formatPrice(ema9)}) > EMA 21: Bullish Short Trend")
            } else {
                bearishPoints += 12
                confluences.add("EMA 9 < EMA 21: Bearish Short Trend")
            }
        }

        if (ema21 != null && ema50 != null) {
            if (ema21 > ema50) {
                bullishPoints += 10
                confluences.add("EMA 21 > EMA 50: Bullish Medium Trend Alignment")
            } else {
                bearishPoints += 10
                confluences.add("EMA 21 < EMA 50: Bearish Medium Trend Alignment")
            }
        }

        if (ema21 != null) {
            if (currentPrice > ema21) {
                bullishPoints += 8
            } else {
                bearishPoints += 8
            }
        }

        // 2. Momentum & RSI (25 points weight)
        val rsi = indicators.rsi
        totalPointsPossible += 25
        val momentumState: MomentumState
        if (rsi != null) {
            when {
                rsi >= 70.0 -> {
                    momentumState = MomentumState.OVERBOUGHT
                    bearishPoints += 18
                    confluences.add("RSI Overbought (${rsi.roundToInt()}): Elevated Pullback Risk")
                }
                rsi in 55.0..69.9 -> {
                    momentumState = MomentumState.BULLISH_MOMENTUM
                    bullishPoints += 22
                    confluences.add("RSI Bullish Zone (${rsi.roundToInt()}): Strong upward momentum")
                }
                rsi in 45.0..54.9 -> {
                    momentumState = MomentumState.BALANCED
                    bullishPoints += 12
                    bearishPoints += 12
                    confluences.add("RSI Neutral (${rsi.roundToInt()}): Market in consolidation")
                }
                rsi in 30.1..44.9 -> {
                    momentumState = MomentumState.BEARISH_MOMENTUM
                    bearishPoints += 20
                    confluences.add("RSI Bearish Zone (${rsi.roundToInt()}): Downward drift")
                }
                else -> {
                    momentumState = MomentumState.OVERSOLD
                    bullishPoints += 20
                    confluences.add("RSI Oversold (${rsi.roundToInt()}): Mean reversion bounce likely")
                }
            }
        } else {
            momentumState = MomentumState.BALANCED
        }

        // 3. MACD Convergence/Divergence (25 points weight)
        val macd = indicators.macd
        totalPointsPossible += 25
        if (macd != null) {
            if (macd.isBullishCrossover) {
                bullishPoints += 15
                confluences.add("MACD Line > Signal Line: Bullish Momentum Bias")
            } else {
                bearishPoints += 15
                confluences.add("MACD Line < Signal Line: Bearish Momentum Bias")
            }

            if (macd.isHistogramPositive) {
                bullishPoints += 10
                confluences.add("Positive MACD Histogram (${String.format("%.2f", macd.histogram)})")
            } else {
                bearishPoints += 10
                confluences.add("Negative MACD Histogram (${String.format("%.2f", macd.histogram)})")
            }
        }

        // 4. Bollinger Bands & Volatility (20 points weight)
        val bb = indicators.bollingerBands
        totalPointsPossible += 20
        if (bb != null) {
            when {
                bb.percentB > 0.95 -> {
                    bearishPoints += 12
                    confluences.add("Testing Upper Bollinger Band (%B: ${String.format("%.2f", bb.percentB)})")
                }
                bb.percentB < 0.05 -> {
                    bullishPoints += 15
                    confluences.add("Rebounding near Lower Bollinger Band (%B: ${String.format("%.2f", bb.percentB)})")
                }
                bb.isSqueeze -> {
                    confluences.add("Bollinger Band Squeeze (${String.format("%.1f", bb.bandwidth)}%): Volatility Expansion Imminent")
                    bullishPoints += 8
                    bearishPoints += 8
                }
                else -> {
                    if (currentPrice >= bb.middle) bullishPoints += 10 else bearishPoints += 10
                }
            }
        }

        // Compute Probability
        val totalRecorded = bullishPoints + bearishPoints
        val rawBullishPct = if (totalRecorded > 0) ((bullishPoints.toDouble() / totalRecorded) * 100.0).roundToInt() else 50
        val clampedBullish = rawBullishPct.coerceIn(5, 95)
        val clampedBearish = 100 - clampedBullish

        // Signal Classification
        val signalType = when {
            clampedBullish >= 75 -> SignalType.STRONG_BUY
            clampedBullish >= 58 -> SignalType.BUY
            clampedBullish in 43..57 -> SignalType.NEUTRAL
            clampedBullish in 26..42 -> SignalType.SELL
            else -> SignalType.STRONG_SELL
        }

        // Trend Classification
        val trendDirection = when {
            indicators.isEmaBullishStack && clampedBullish >= 70 -> TrendDirection.STRONG_UPTREND
            clampedBullish >= 55 -> TrendDirection.UPTREND
            indicators.isEmaBearishStack && clampedBullish <= 30 -> TrendDirection.STRONG_DOWNTREND
            clampedBullish <= 45 -> TrendDirection.DOWNTREND
            else -> TrendDirection.SIDEWAYS
        }

        val confluenceScore = ((abs(clampedBullish - 50) * 2.0) / 10.0 + 3.0).coerceIn(1.0, 9.8)
        val isBullishDecision = clampedBullish >= 50

        val riskLevels = calculateRiskManagement(
            currentPrice = currentPrice,
            candles = candles,
            indicators = indicators,
            isBullish = isBullishDecision
        )

        return SignalScore(
            probabilityBullish = clampedBullish,
            probabilityBearish = clampedBearish,
            signalType = signalType,
            trendDirection = trendDirection,
            momentumState = momentumState,
            confluenceScore = (confluenceScore * 10).roundToInt() / 10.0,
            keyConfluences = confluences,
            riskManagement = riskLevels,
            timeframe = timeframe
        )
    }

    private fun formatPrice(price: Double): String {
        return String.format("%,.1f", price)
    }
}
