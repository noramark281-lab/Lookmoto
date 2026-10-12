package com.example

import com.example.data.engine.IndicatorEngine
import com.example.data.model.Candle
import com.example.data.model.Timeframe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IndicatorEngineTest {

    @Test
    fun testRsiCalculation() {
        // Generating price series with upward momentum
        val prices = mutableListOf(100.0)
        for (i in 1..25) {
            prices.add(prices.last() + (if (i % 3 == 0) -1.0 else 2.5))
        }

        val rsi = IndicatorEngine.calculateRSI(prices, 14)
        assertNotNull(rsi)
        assertTrue("RSI should be bullish (> 50)", rsi!! > 50.0)
    }

    @Test
    fun testEmaCalculation() {
        val prices = listOf(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0)
        val emaSeries = IndicatorEngine.calculateEMASeries(prices, 9)

        assertEquals(prices.size, emaSeries.size)
        assertTrue(emaSeries.last() > emaSeries.first())
    }

    @Test
    fun testMacdCalculation() {
        val prices = mutableListOf<Double>()
        for (i in 1..40) {
            prices.add(50000.0 + (i * 150.0))
        }

        val macd = IndicatorEngine.calculateMACD(prices, 12, 26, 9)
        assertNotNull(macd)
        assertTrue("MACD line should be positive in strong uptrend", macd!!.macd > 0)
    }

    @Test
    fun testBollingerBandsCalculation() {
        val prices = (1..30).map { 60000.0 + (it % 5) * 50.0 }
        val bb = IndicatorEngine.calculateBollingerBands(prices, 20, 2.0)

        assertNotNull(bb)
        assertTrue("Upper band must be greater than middle band", bb!!.upper > bb.middle)
        assertTrue("Lower band must be less than middle band", bb.lower < bb.middle)
    }

    @Test
    fun testSignalScoringEngine() {
        val candles = mutableListOf<Candle>()
        var price = 65000.0
        val baseTime = System.currentTimeMillis() - 3600_000

        for (i in 0 until 40) {
            price += 100.0
            val t = baseTime + (i * 60_000)
            candles.add(
                Candle(
                    openTime = t,
                    open = price - 50.0,
                    high = price + 70.0,
                    low = price - 60.0,
                    close = price,
                    volume = 12.5,
                    closeTime = t + 60_000
                )
            )
        }

        val indicators = IndicatorEngine.calculateIndicators(candles)
        val signal = IndicatorEngine.evaluateSignal(Timeframe.FIVE_MIN, candles, indicators)

        assertNotNull(signal)
        assertTrue(signal.probabilityBullish in 5..95)
        assertEquals(100 - signal.probabilityBullish, signal.probabilityBearish)
        assertNotNull(signal.riskManagement)
        assertTrue(signal.riskManagement!!.takeProfit1 > signal.riskManagement!!.currentPrice)
    }
}
