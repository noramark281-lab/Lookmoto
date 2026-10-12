package com.example.data.network

import com.example.data.model.Candle
import com.example.data.model.ExchangeSource
import com.example.data.model.Timeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class CryptoRestClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {

    suspend fun fetchHistoricalKlines(
        exchange: ExchangeSource,
        timeframe: Timeframe,
        limit: Int = 100
    ): Result<List<Candle>> = withContext(Dispatchers.IO) {
        val interval = if (exchange == ExchangeSource.BINANCE) timeframe.binanceInterval else timeframe.mexcInterval
        val primaryUrl = if (exchange == ExchangeSource.BINANCE) {
            "https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=$interval&limit=$limit"
        } else {
            "https://api.mexc.com/api/v3/klines?symbol=BTCUSDT&interval=$interval&limit=$limit"
        }

        val fallbackUrl = if (exchange == ExchangeSource.BINANCE) {
            "https://api.mexc.com/api/v3/klines?symbol=BTCUSDT&interval=$interval&limit=$limit"
        } else {
            "https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=$interval&limit=$limit"
        }

        // Try primary
        val primaryResult = executeKlinesRequest(primaryUrl)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        // Fallback to the other exchange REST API if first fails
        executeKlinesRequest(fallbackUrl)
    }

    private fun executeKlinesRequest(url: String): Result<List<Candle>> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BTC-Signal-Pro-Android/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("HTTP error ${response.code}: ${response.message}"))
                }

                val bodyString = response.body?.string() ?: return Result.failure(Exception("Empty response body"))
                val jsonArray = JSONArray(bodyString)
                val candleList = mutableListOf<Candle>()

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONArray(i)
                    // Standard Binance/MEXC kline index schema:
                    // 0: Open time, 1: Open, 2: High, 3: Low, 4: Close, 5: Volume, 6: Close time
                    val openTime = item.getLong(0)
                    val open = item.getString(1).toDoubleOrNull() ?: 0.0
                    val high = item.getString(2).toDoubleOrNull() ?: 0.0
                    val low = item.getString(3).toDoubleOrNull() ?: 0.0
                    val close = item.getString(4).toDoubleOrNull() ?: 0.0
                    val volume = item.getString(5).toDoubleOrNull() ?: 0.0
                    val closeTime = if (item.length() > 6) item.getLong(6) else openTime + 60_000L

                    candleList.add(
                        Candle(
                            openTime = openTime,
                            open = open,
                            high = high,
                            low = low,
                            close = close,
                            volume = volume,
                            closeTime = closeTime
                        )
                    )
                }

                Result.success(candleList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
