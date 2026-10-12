package com.example.data.network

import com.example.data.model.AggTrade
import com.example.data.model.ConnectionStatus
import com.example.data.model.ExchangeSource
import com.example.data.model.OrderBookData
import com.example.data.model.OrderBookEntry
import com.example.data.model.TickerData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CryptoWebSocketClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .build()
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var currentWebSocket: WebSocket? = null
    private var pingJob: Job? = null
    private var reconnectJob: Job? = null
    private var activeExchange: ExchangeSource = ExchangeSource.BINANCE

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _tickerFlow = MutableSharedFlow<TickerData>(extraBufferCapacity = 64)
    val tickerFlow: SharedFlow<TickerData> = _tickerFlow.asSharedFlow()

    private val _orderBookFlow = MutableSharedFlow<OrderBookData>(extraBufferCapacity = 64)
    val orderBookFlow: SharedFlow<OrderBookData> = _orderBookFlow.asSharedFlow()

    private val _tradeFlow = MutableSharedFlow<AggTrade>(extraBufferCapacity = 128)
    val tradeFlow: SharedFlow<AggTrade> = _tradeFlow.asSharedFlow()

    fun connect(exchange: ExchangeSource) {
        activeExchange = exchange
        disconnect()

        _connectionStatus.value = ConnectionStatus.CONNECTING
        val url = if (exchange == ExchangeSource.BINANCE) {
            "wss://stream.binance.com:9443/stream?streams=btcusdt@ticker/btcusdt@depth20@100ms/btcusdt@aggTrade"
        } else {
            "wss://wbs.mexc.com/ws"
        }

        val request = Request.Builder()
            .url(url)
            .build()

        currentWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionStatus.value = ConnectionStatus.CONNECTED

                if (exchange == ExchangeSource.MEXC) {
                    // Send MEXC subscription payload
                    val subscribeMsg = JSONObject().apply {
                        put("method", "SUBSCRIPTION")
                        put(
                            "params",
                            org.json.JSONArray().apply {
                                put("spot@public.deals.v3.api@BTCUSDT")
                                put("spot@public.limit.depth.v3.api@BTCUSDT@20")
                                put("spot@public.miniTicker.v3.api@BTCUSDT@UTC+8")
                            }
                        )
                    }
                    webSocket.send(subscribeMsg.toString())
                    startMexcPingLoop(webSocket)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    if (exchange == ExchangeSource.BINANCE) {
                        handleBinanceMessage(text)
                    } else {
                        handleMexcMessage(text)
                    }
                } catch (e: Exception) {
                    // Ignored malformed tick
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionStatus.value = ConnectionStatus.ERROR
                scheduleReconnect()
            }
        })
    }

    private fun handleBinanceMessage(text: String) {
        val root = JSONObject(text)
        val stream = root.optString("stream")
        val data = root.optJSONObject("data") ?: return

        when {
            stream.endsWith("@ticker") -> {
                val price = data.optString("c").toDoubleOrNull() ?: return
                val priceChange = data.optString("p").toDoubleOrNull() ?: 0.0
                val priceChangePercent = data.optString("P").toDoubleOrNull() ?: 0.0
                val high24h = data.optString("h").toDoubleOrNull() ?: 0.0
                val low24h = data.optString("l").toDoubleOrNull() ?: 0.0
                val volume24h = data.optString("v").toDoubleOrNull() ?: 0.0
                val quoteVolume24h = data.optString("q").toDoubleOrNull() ?: 0.0
                val eventTime = data.optLong("E", System.currentTimeMillis())

                _tickerFlow.tryEmit(
                    TickerData(
                        symbol = "BTCUSDT",
                        price = price,
                        priceChange = priceChange,
                        priceChangePercent = priceChangePercent,
                        high24h = high24h,
                        low24h = low24h,
                        volume24h = volume24h,
                        quoteVolume24h = quoteVolume24h,
                        timestamp = eventTime
                    )
                )
            }
            stream.contains("@depth") -> {
                val bidsArray = data.optJSONArray("bids") ?: org.json.JSONArray()
                val asksArray = data.optJSONArray("asks") ?: org.json.JSONArray()
                val lastUpdateId = data.optLong("lastUpdateId", 0L)

                val bids = mutableListOf<OrderBookEntry>()
                for (i in 0 until bidsArray.length()) {
                    val item = bidsArray.getJSONArray(i)
                    val p = item.getString(0).toDoubleOrNull() ?: continue
                    val q = item.getString(1).toDoubleOrNull() ?: continue
                    bids.add(OrderBookEntry(p, q))
                }

                val asks = mutableListOf<OrderBookEntry>()
                for (i in 0 until asksArray.length()) {
                    val item = asksArray.getJSONArray(i)
                    val p = item.getString(0).toDoubleOrNull() ?: continue
                    val q = item.getString(1).toDoubleOrNull() ?: continue
                    asks.add(OrderBookEntry(p, q))
                }

                _orderBookFlow.tryEmit(
                    OrderBookData(
                        bids = bids,
                        asks = asks,
                        lastUpdateId = lastUpdateId
                    )
                )
            }
            stream.endsWith("@aggTrade") -> {
                val tradeId = data.optLong("a", System.currentTimeMillis())
                val price = data.optString("p").toDoubleOrNull() ?: return
                val quantity = data.optString("q").toDoubleOrNull() ?: return
                val time = data.optLong("T", System.currentTimeMillis())
                val isBuyerMaker = data.optBoolean("m", false)

                _tradeFlow.tryEmit(
                    AggTrade(
                        tradeId = tradeId,
                        price = price,
                        quantity = quantity,
                        time = time,
                        isBuyerMaker = isBuyerMaker
                    )
                )
            }
        }
    }

    private fun handleMexcMessage(text: String) {
        val root = JSONObject(text)
        val channel = root.optString("c")
        val data = root.optJSONObject("d") ?: return

        when {
            channel.contains("miniTicker") -> {
                val price = data.optString("p").toDoubleOrNull() ?: return
                val priceChangePercent = data.optString("r").toDoubleOrNull()?.times(100) ?: 0.0
                val high24h = data.optString("h").toDoubleOrNull() ?: 0.0
                val low24h = data.optString("l").toDoubleOrNull() ?: 0.0
                val volume24h = data.optString("v").toDoubleOrNull() ?: 0.0

                _tickerFlow.tryEmit(
                    TickerData(
                        symbol = "BTCUSDT",
                        price = price,
                        priceChange = 0.0,
                        priceChangePercent = priceChangePercent,
                        high24h = high24h,
                        low24h = low24h,
                        volume24h = volume24h,
                        quoteVolume24h = 0.0,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            channel.contains("limit.depth") -> {
                val bidsArray = data.optJSONArray("bids") ?: org.json.JSONArray()
                val asksArray = data.optJSONArray("asks") ?: org.json.JSONArray()

                val bids = mutableListOf<OrderBookEntry>()
                for (i in 0 until bidsArray.length()) {
                    val item = bidsArray.getJSONObject(i)
                    val p = item.optString("p").toDoubleOrNull() ?: continue
                    val q = item.optString("v").toDoubleOrNull() ?: continue
                    bids.add(OrderBookEntry(p, q))
                }

                val asks = mutableListOf<OrderBookEntry>()
                for (i in 0 until asksArray.length()) {
                    val item = asksArray.getJSONObject(i)
                    val p = item.optString("p").toDoubleOrNull() ?: continue
                    val q = item.optString("v").toDoubleOrNull() ?: continue
                    asks.add(OrderBookEntry(p, q))
                }

                _orderBookFlow.tryEmit(
                    OrderBookData(
                        bids = bids,
                        asks = asks
                    )
                )
            }
            channel.contains("deals") -> {
                val dealsArray = data.optJSONArray("deals") ?: return
                for (i in 0 until dealsArray.length()) {
                    val item = dealsArray.getJSONObject(i)
                    val p = item.optString("p").toDoubleOrNull() ?: continue
                    val v = item.optString("v").toDoubleOrNull() ?: continue
                    val t = item.optLong("t", System.currentTimeMillis())
                    val s = item.optInt("S", 1) // 1: buy, 2: sell

                    _tradeFlow.tryEmit(
                        AggTrade(
                            tradeId = t + i,
                            price = p,
                            quantity = v,
                            time = t,
                            isBuyerMaker = (s == 2)
                        )
                    )
                }
            }
        }
    }

    private fun startMexcPingLoop(webSocket: WebSocket) {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive) {
                delay(20_000)
                try {
                    webSocket.send("{\"method\":\"PING\"}")
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(3000)
            connect(activeExchange)
        }
    }

    fun disconnect() {
        pingJob?.cancel()
        pingJob = null
        reconnectJob?.cancel()
        reconnectJob = null
        try {
            currentWebSocket?.close(1000, "Normal closure")
        } catch (e: Exception) {
            // Ignored
        }
        currentWebSocket = null
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }
}
