package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AggTrade
import com.example.data.model.Candle
import com.example.data.model.ConnectionHeartbeat
import com.example.data.model.ConnectionStatus
import com.example.data.model.CryptoNewsItem
import com.example.data.model.DerivativesFlowData
import com.example.data.model.ExchangeSource
import com.example.data.model.InstantFlashReport
import com.example.data.model.MarketSentiment
import com.example.data.model.MultiTimeframeSignals
import com.example.data.model.OrderBookData
import com.example.data.model.SignalScore
import com.example.data.model.TechnicalIndicators
import com.example.data.model.TickerData
import com.example.data.model.Timeframe
import com.example.data.model.TimeframePrediction
import com.example.data.model.TradingViewGauge
import com.example.data.repository.CryptoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TickDirection {
    NONE, UP, DOWN
}

enum class DashboardTab(val titleArabic: String) {
    BEST_REPORT("التقرير اللحظي الأفضل ⚡"),
    PREDICTIONS("التنبؤات والتوقعات 🎯"),
    TRADINGVIEW_GAUGES("مؤشرات تريدنج فيو 📐"),
    DERIVATIVES_COINGLASS("سيولة كوين جلاس 📊"),
    CHART_SIGNALS("الرسم البياني 📈"),
    NEWS_SENTIMENT("أخبار Tree News 📰"),
    ORDER_BOOK("دفتر الأوامر 📖"),
    RISK_MATRIX("إدارة المخاطر 🛡️")
}

data class CryptoUiState(
    val selectedTimeframe: Timeframe = Timeframe.FIVE_MIN,
    val selectedTab: DashboardTab = DashboardTab.BEST_REPORT,
    val activeExchange: ExchangeSource = ExchangeSource.BINANCE,
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTING,
    val heartbeat: ConnectionHeartbeat = ConnectionHeartbeat(),
    val ticker: TickerData = TickerData(),
    val tickDirection: TickDirection = TickDirection.NONE,
    val candles: List<Candle> = emptyList(),
    val indicators: TechnicalIndicators = TechnicalIndicators(),
    val currentSignal: SignalScore? = null,
    val multiTimeframe: MultiTimeframeSignals = MultiTimeframeSignals(),
    val orderBook: OrderBookData = OrderBookData(),
    val recentTrades: List<AggTrade> = emptyList(),
    val marketSentiment: MarketSentiment = MarketSentiment(),
    val newsFeed: List<CryptoNewsItem> = emptyList(),
    val derivativesFlow: DerivativesFlowData = DerivativesFlowData(),
    val tradingViewGauge: TradingViewGauge = TradingViewGauge(),
    val isLoading: Boolean = true,
    val inspectedCandle: Candle? = null,
    val showSignalBreakdown: Boolean = false
) {
    val currentPrediction: TimeframePrediction?
        get() = multiTimeframe.predictionsByTimeframe[selectedTimeframe]

    val currentFlashReport: InstantFlashReport?
        get() = multiTimeframe.flashReportsByTimeframe[selectedTimeframe] ?: multiTimeframe.bestOverallInstantReport
}

class CryptoViewModel(
    private val repository: CryptoRepository = CryptoRepository()
) : ViewModel() {

    private val _selectedTimeframe = MutableStateFlow(Timeframe.FIVE_MIN)
    private val _selectedTab = MutableStateFlow(DashboardTab.BEST_REPORT)
    private val _inspectedCandle = MutableStateFlow<Candle?>(null)
    private val _showSignalBreakdown = MutableStateFlow(false)

    private val _tickDirection = MutableStateFlow(TickDirection.NONE)
    private var lastRecordedPrice: Double = 0.0
    private var tickResetJob: Job? = null

    val uiState: StateFlow<CryptoUiState> = combine(
        _selectedTimeframe,
        _selectedTab,
        repository.activeExchange,
        repository.getConnectionStatus(),
        repository.heartbeat,
        repository.ticker,
        repository.candlesFlow,
        repository.indicatorsFlow,
        repository.multiTimeframeSignals,
        repository.orderBook,
        repository.recentTrades,
        repository.marketSentiment,
        repository.newsFeed,
        repository.derivativesFlow,
        repository.isLoading
    ) { args ->
        val tf = args[0] as Timeframe
        val tab = args[1] as DashboardTab
        val exchange = args[2] as ExchangeSource
        val status = args[3] as ConnectionStatus
        val heartbeat = args[4] as ConnectionHeartbeat
        val ticker = args[5] as TickerData
        val candlesMap = args[6] as Map<Timeframe, List<Candle>>
        val indicatorsMap = args[7] as Map<Timeframe, TechnicalIndicators>
        val mtfSignals = args[8] as MultiTimeframeSignals
        val orderBook = args[9] as OrderBookData
        val trades = args[10] as List<AggTrade>
        val sentiment = args[11] as MarketSentiment
        val news = args[12] as List<CryptoNewsItem>
        val deriv = args[13] as DerivativesFlowData
        val loading = args[14] as Boolean

        val candles = candlesMap[tf] ?: emptyList()
        val indicators = indicatorsMap[tf] ?: TechnicalIndicators()
        val currentSignal = mtfSignals.signalsByTimeframe[tf]

        CryptoUiState(
            selectedTimeframe = tf,
            selectedTab = tab,
            activeExchange = exchange,
            connectionStatus = status,
            heartbeat = heartbeat,
            ticker = ticker,
            tickDirection = _tickDirection.value,
            candles = candles,
            indicators = indicators,
            currentSignal = currentSignal,
            multiTimeframe = mtfSignals,
            orderBook = orderBook,
            recentTrades = trades,
            marketSentiment = sentiment,
            newsFeed = news,
            derivativesFlow = deriv,
            tradingViewGauge = mtfSignals.tradingViewGauge,
            isLoading = loading,
            inspectedCandle = _inspectedCandle.value,
            showSignalBreakdown = _showSignalBreakdown.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CryptoUiState()
    )

    init {
        viewModelScope.launch {
            repository.initialize(ExchangeSource.BINANCE)
        }

        // Monitor price movement ticks
        viewModelScope.launch {
            repository.ticker.collect { ticker ->
                if (ticker.price > 0 && lastRecordedPrice > 0) {
                    if (ticker.price > lastRecordedPrice) {
                        _tickDirection.value = TickDirection.UP
                    } else if (ticker.price < lastRecordedPrice) {
                        _tickDirection.value = TickDirection.DOWN
                    }

                    tickResetJob?.cancel()
                    tickResetJob = viewModelScope.launch {
                        delay(600)
                        _tickDirection.value = TickDirection.NONE
                    }
                }
                if (ticker.price > 0) {
                    lastRecordedPrice = ticker.price
                }
            }
        }
    }

    fun selectTimeframe(timeframe: Timeframe) {
        _selectedTimeframe.value = timeframe
    }

    fun selectTab(tab: DashboardTab) {
        _selectedTab.value = tab
    }

    fun switchExchange(exchange: ExchangeSource) {
        repository.switchExchange(exchange)
    }

    fun refreshData() {
        repository.refreshHistoricalData()
    }

    fun inspectCandle(candle: Candle?) {
        _inspectedCandle.value = candle
    }

    fun toggleSignalBreakdown(show: Boolean) {
        _showSignalBreakdown.value = show
    }
}
