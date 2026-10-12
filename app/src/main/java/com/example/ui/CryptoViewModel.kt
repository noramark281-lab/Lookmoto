package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AggTrade
import com.example.data.model.Candle
import com.example.data.model.ConnectionStatus
import com.example.data.model.ExchangeSource
import com.example.data.model.MultiTimeframeSignals
import com.example.data.model.OrderBookData
import com.example.data.model.SignalScore
import com.example.data.model.TechnicalIndicators
import com.example.data.model.TickerData
import com.example.data.model.Timeframe
import com.example.data.repository.CryptoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TickDirection {
    NONE, UP, DOWN
}

enum class DashboardTab(val title: String) {
    CHART_SIGNALS("Chart & Signal"),
    INDICATORS("Indicators"),
    RISK_MATRIX("Risk Levels"),
    ORDER_BOOK("Order Book")
}

data class CryptoUiState(
    val selectedTimeframe: Timeframe = Timeframe.FIVE_MIN,
    val selectedTab: DashboardTab = DashboardTab.CHART_SIGNALS,
    val activeExchange: ExchangeSource = ExchangeSource.BINANCE,
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTING,
    val ticker: TickerData = TickerData(),
    val tickDirection: TickDirection = TickDirection.NONE,
    val candles: List<Candle> = emptyList(),
    val indicators: TechnicalIndicators = TechnicalIndicators(),
    val currentSignal: SignalScore? = null,
    val multiTimeframe: MultiTimeframeSignals = MultiTimeframeSignals(),
    val orderBook: OrderBookData = OrderBookData(),
    val recentTrades: List<AggTrade> = emptyList(),
    val isLoading: Boolean = true,
    val inspectedCandle: Candle? = null,
    val showSignalBreakdown: Boolean = false
)

class CryptoViewModel(
    private val repository: CryptoRepository = CryptoRepository()
) : ViewModel() {

    private val _selectedTimeframe = MutableStateFlow(Timeframe.FIVE_MIN)
    private val _selectedTab = MutableStateFlow(DashboardTab.CHART_SIGNALS)
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
        repository.ticker,
        repository.candlesFlow,
        repository.indicatorsFlow,
        repository.multiTimeframeSignals,
        repository.orderBook,
        repository.recentTrades,
        repository.isLoading,
        _tickDirection,
        _inspectedCandle,
        _showSignalBreakdown
    ) { args ->
        val tf = args[0] as Timeframe
        val tab = args[1] as DashboardTab
        val exchange = args[2] as ExchangeSource
        val status = args[3] as ConnectionStatus
        val ticker = args[4] as TickerData
        val candlesMap = args[5] as Map<Timeframe, List<Candle>>
        val indicatorsMap = args[6] as Map<Timeframe, TechnicalIndicators>
        val mtfSignals = args[7] as MultiTimeframeSignals
        val orderBook = args[8] as OrderBookData
        val trades = args[9] as List<AggTrade>
        val loading = args[10] as Boolean
        val tickDir = args[11] as TickDirection
        val inspected = args[12] as Candle?
        val showBreakdown = args[13] as Boolean

        val candles = candlesMap[tf] ?: emptyList()
        val indicators = indicatorsMap[tf] ?: TechnicalIndicators()
        val currentSignal = mtfSignals.signalsByTimeframe[tf]

        CryptoUiState(
            selectedTimeframe = tf,
            selectedTab = tab,
            activeExchange = exchange,
            connectionStatus = status,
            ticker = ticker,
            tickDirection = tickDir,
            candles = candles,
            indicators = indicators,
            currentSignal = currentSignal,
            multiTimeframe = mtfSignals,
            orderBook = orderBook,
            recentTrades = trades,
            isLoading = loading,
            inspectedCandle = inspected,
            showSignalBreakdown = showBreakdown
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CryptoUiState()
    )

    init {
        viewModelScope.launch {
            repository.initialize()
        }

        // Monitor ticker changes for tick animations
        viewModelScope.launch {
            repository.ticker.collect { ticker ->
                if (ticker.price > 0.0 && lastRecordedPrice > 0.0) {
                    if (ticker.price > lastRecordedPrice) {
                        triggerTick(TickDirection.UP)
                    } else if (ticker.price < lastRecordedPrice) {
                        triggerTick(TickDirection.DOWN)
                    }
                }
                if (ticker.price > 0.0) {
                    lastRecordedPrice = ticker.price
                }
            }
        }
    }

    private fun triggerTick(direction: TickDirection) {
        _tickDirection.value = direction
        tickResetJob?.cancel()
        tickResetJob = viewModelScope.launch {
            delay(400)
            _tickDirection.value = TickDirection.NONE
        }
    }

    fun setTimeframe(tf: Timeframe) {
        _selectedTimeframe.value = tf
        _inspectedCandle.value = null
    }

    fun setTab(tab: DashboardTab) {
        _selectedTab.value = tab
    }

    fun switchExchange(exchange: ExchangeSource) {
        repository.switchExchange(exchange)
    }

    fun refresh() {
        repository.refreshHistoricalData()
    }

    fun inspectCandle(candle: Candle?) {
        _inspectedCandle.value = candle
    }

    fun toggleSignalBreakdown(show: Boolean) {
        _showSignalBreakdown.value = show
    }
}
