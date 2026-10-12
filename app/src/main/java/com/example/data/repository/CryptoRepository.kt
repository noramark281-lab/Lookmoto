package com.example.data.repository

import com.example.data.engine.IndicatorEngine
import com.example.data.model.AggTrade
import com.example.data.model.Candle
import com.example.data.model.ConnectionStatus
import com.example.data.model.ExchangeSource
import com.example.data.model.MultiTimeframeSignals
import com.example.data.model.OrderBookData
import com.example.data.model.SignalScore
import com.example.data.model.SignalType
import com.example.data.model.TechnicalIndicators
import com.example.data.model.TickerData
import com.example.data.model.Timeframe
import com.example.data.network.CryptoRestClient
import com.example.data.network.CryptoWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CryptoRepository(
    private val restClient: CryptoRestClient = CryptoRestClient(),
    private val webSocketClient: CryptoWebSocketClient = CryptoWebSocketClient()
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _activeExchange = MutableStateFlow(ExchangeSource.BINANCE)
    val activeExchange: StateFlow<ExchangeSource> = _activeExchange.asStateFlow()

    private val _connectionStatus = webSocketClient.connectionStatus

    private val _ticker = MutableStateFlow(TickerData())
    val ticker: StateFlow<TickerData> = _ticker.asStateFlow()

    private val _orderBook = MutableStateFlow(OrderBookData())
    val orderBook: StateFlow<OrderBookData> = _orderBook.asStateFlow()

    private val _recentTrades = MutableStateFlow<List<AggTrade>>(emptyList())
    val recentTrades: StateFlow<List<AggTrade>> = _recentTrades.asStateFlow()

    // Multi-timeframe Candles map
    private val candlesMap = mutableMapOf<Timeframe, MutableList<Candle>>()
    private val _candlesFlow = MutableStateFlow<Map<Timeframe, List<Candle>>>(emptyMap())
    val candlesFlow: StateFlow<Map<Timeframe, List<Candle>>> = _candlesFlow.asStateFlow()

    // Multi-timeframe Indicators map
    private val _indicatorsFlow = MutableStateFlow<Map<Timeframe, TechnicalIndicators>>(emptyMap())
    val indicatorsFlow: StateFlow<Map<Timeframe, TechnicalIndicators>> = _indicatorsFlow.asStateFlow()

    // Multi-timeframe Signals
    private val _multiTimeframeSignals = MutableStateFlow(MultiTimeframeSignals())
    val multiTimeframeSignals: StateFlow<MultiTimeframeSignals> = _multiTimeframeSignals.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Collect real-time WebSocket flows
        scope.launch {
            webSocketClient.tickerFlow.collect { newTicker ->
                _ticker.value = newTicker
                updateFormingCandle(newTicker.price, newTicker.timestamp, 0.0)
            }
        }

        scope.launch {
            webSocketClient.orderBookFlow.collect { newBook ->
                _orderBook.value = newBook
            }
        }

        scope.launch {
            webSocketClient.tradeFlow.collect { newTrade ->
                val current = _recentTrades.value.toMutableList()
                current.add(0, newTrade)
                if (current.size > 50) {
                    current.removeAt(current.size - 1)
                }
                _recentTrades.value = current
                updateFormingCandle(newTrade.price, newTrade.time, newTrade.quantity)
            }
        }
    }

    suspend fun initialize(exchange: ExchangeSource = ExchangeSource.BINANCE) {
        _isLoading.value = true
        _activeExchange.value = exchange

        // 1. Fetch historical OHLCV candles for 1m, 5m, 30m
        Timeframe.values().forEach { tf ->
            val result = restClient.fetchHistoricalKlines(exchange, tf, limit = 100)
            result.onSuccess { list ->
                synchronized(candlesMap) {
                    candlesMap[tf] = list.toMutableList()
                }
            }
        }

        // 2. Initial calculation of all indicators and signals
        recalculateAllIndicators()
        _isLoading.value = false

        // 3. Connect real-time WebSocket
        webSocketClient.connect(exchange)
    }

    fun switchExchange(exchange: ExchangeSource) {
        if (_activeExchange.value == exchange) return
        scope.launch {
            initialize(exchange)
        }
    }

    fun refreshHistoricalData() {
        scope.launch {
            _isLoading.value = true
            val currentExchange = _activeExchange.value
            Timeframe.values().forEach { tf ->
                val result = restClient.fetchHistoricalKlines(currentExchange, tf, limit = 100)
                result.onSuccess { list ->
                    synchronized(candlesMap) {
                        candlesMap[tf] = list.toMutableList()
                    }
                }
            }
            recalculateAllIndicators()
            _isLoading.value = false
        }
    }

    private fun updateFormingCandle(price: Double, timestamp: Long, volumeIncrement: Double) {
        if (price <= 0.0) return

        var anyChanged = false
        synchronized(candlesMap) {
            Timeframe.values().forEach { tf ->
                val list = candlesMap[tf] ?: return@forEach
                if (list.isEmpty()) return@forEach

                val lastCandle = list.last()
                val candleEnd = lastCandle.openTime + tf.durationMs

                if (timestamp < candleEnd) {
                    // Update current in-progress candle
                    val updated = lastCandle.copy(
                        high = max(lastCandle.high, price),
                        low = min(lastCandle.low, price),
                        close = price,
                        volume = lastCandle.volume + volumeIncrement
                    )
                    list[list.lastIndex] = updated
                    anyChanged = true
                } else {
                    // Start new candle
                    val newCandle = Candle(
                        openTime = candleEnd,
                        open = lastCandle.close,
                        high = max(lastCandle.close, price),
                        low = min(lastCandle.close, price),
                        close = price,
                        volume = volumeIncrement,
                        closeTime = candleEnd + tf.durationMs
                    )
                    list.add(newCandle)
                    if (list.size > 120) {
                        list.removeAt(0)
                    }
                    anyChanged = true
                }
            }
        }

        if (anyChanged) {
            recalculateAllIndicators()
        }
    }

    private fun recalculateAllIndicators() {
        val snapshot = synchronized(candlesMap) {
            candlesMap.mapValues { it.value.toList() }
        }
        _candlesFlow.value = snapshot

        val newIndicators = mutableMapOf<Timeframe, TechnicalIndicators>()
        val newSignals = mutableMapOf<Timeframe, SignalScore>()

        Timeframe.values().forEach { tf ->
            val candles = snapshot[tf] ?: emptyList()
            val indicators = IndicatorEngine.calculateIndicators(candles)
            val signal = IndicatorEngine.evaluateSignal(tf, candles, indicators)

            newIndicators[tf] = indicators
            newSignals[tf] = signal
        }

        _indicatorsFlow.value = newIndicators

        // Multi-timeframe consensus:
        // 1m weight: 20%, 5m weight: 35%, 30m weight: 45%
        val score1m = newSignals[Timeframe.ONE_MIN]?.probabilityBullish ?: 50
        val score5m = newSignals[Timeframe.FIVE_MIN]?.probabilityBullish ?: 50
        val score30m = newSignals[Timeframe.THIRTY_MIN]?.probabilityBullish ?: 50

        val weightedBullish = ((score1m * 0.20) + (score5m * 0.35) + (score30m * 0.45)).roundToInt()
        val overallSignal = when {
            weightedBullish >= 75 -> SignalType.STRONG_BUY
            weightedBullish >= 58 -> SignalType.BUY
            weightedBullish in 43..57 -> SignalType.NEUTRAL
            weightedBullish in 26..42 -> SignalType.SELL
            else -> SignalType.STRONG_SELL
        }

        _multiTimeframeSignals.value = MultiTimeframeSignals(
            signalsByTimeframe = newSignals,
            overallBullishProbability = weightedBullish,
            overallSignalType = overallSignal
        )
    }

    fun getConnectionStatus(): StateFlow<ConnectionStatus> = _connectionStatus
}
