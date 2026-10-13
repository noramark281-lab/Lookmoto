package com.example.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.data.model.ExchangeSource
import com.example.data.model.SignalScore
import com.example.data.model.TickerData
import com.example.data.model.Timeframe
import com.example.ui.components.BestReportView
import com.example.ui.components.CandleStickChart
import com.example.ui.components.ConnectionHealthBar
import com.example.ui.components.DerivativesFlowCard
import com.example.ui.components.IndicatorDashboard
import com.example.ui.components.NewsSentimentView
import com.example.ui.components.OrderBookAndTradesView
import com.example.ui.components.PredictionsView
import com.example.ui.components.RiskMatrixCard
import com.example.ui.components.SignalBreakdownDialog
import com.example.ui.components.TradingViewGaugeCard
import com.example.ui.theme.CryptoBgDark
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardDark
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenGlow
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoRedGlow
import com.example.ui.theme.CryptoSurfaceDark
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CryptoDashboardScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val currentPrice = state.ticker.price.takeIf { it > 0 } ?: state.candles.lastOrNull()?.close ?: 65000.0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (isLandscape) {
            // Landscape Mode (الوضع الأفقي): High-density professional dual-pane trading terminal
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Control Pane (Ticker, Connection Health, Timeframe Selector)
                Column(
                    modifier = Modifier
                        .weight(0.38f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TopDashboardHeader(
                        activeExchange = state.activeExchange,
                        connectionStatus = state.connectionStatus,
                        onSwitchExchange = { viewModel.switchExchange(it) },
                        onRefresh = { viewModel.refreshData() }
                    )

                    ConnectionHealthBar(heartbeat = state.heartbeat)

                    LiveTickerHeader(
                        ticker = state.ticker,
                        tickDirection = state.tickDirection
                    )

                    TimeframeSelectorRow(
                        selectedTimeframe = state.selectedTimeframe,
                        onSelectTimeframe = { viewModel.selectTimeframe(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Right Content Pane (Tabs + Scrollable Tab Views)
                Column(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight()
                ) {
                    DashboardTabsRow(
                        selectedTab = state.selectedTab,
                        onSelectTab = { viewModel.selectTab(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        ActiveTabContent(
                            state = state,
                            currentPrice = currentPrice,
                            viewModel = viewModel
                        )
                    }
                }
            }
        } else {
            // Portrait Mode (الوضع العمودي): Standard mobile stream
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TopDashboardHeader(
                    activeExchange = state.activeExchange,
                    connectionStatus = state.connectionStatus,
                    onSwitchExchange = { viewModel.switchExchange(it) },
                    onRefresh = { viewModel.refreshData() }
                )

                ConnectionHealthBar(heartbeat = state.heartbeat)

                LiveTickerHeader(
                    ticker = state.ticker,
                    tickDirection = state.tickDirection
                )

                TimeframeSelectorRow(
                    selectedTimeframe = state.selectedTimeframe,
                    onSelectTimeframe = { viewModel.selectTimeframe(it) }
                )

                DashboardTabsRow(
                    selectedTab = state.selectedTab,
                    onSelectTab = { viewModel.selectTab(it) }
                )

                ActiveTabContent(
                    state = state,
                    currentPrice = currentPrice,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Algorithmic Signal Breakdown Dialog
        if (state.showSignalBreakdown) {
            SignalBreakdownDialog(
                currentSignal = state.currentSignal,
                multiTimeframe = state.multiTimeframe,
                onDismiss = { viewModel.toggleSignalBreakdown(false) }
            )
        }
    }
}

@Composable
private fun ActiveTabContent(
    state: CryptoUiState,
    currentPrice: Double,
    viewModel: CryptoViewModel
) {
    if (state.isLoading && state.candles.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(color = CryptoCyan)
                Text(
                    text = "جارٍ ربط وتجميع بيانات المنصات العالمية...",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    } else {
        when (state.selectedTab) {
            DashboardTab.BEST_REPORT -> {
                BestReportView(
                    multiSignals = state.multiTimeframe,
                    selectedTimeframe = state.selectedTimeframe,
                    currentPrice = currentPrice,
                    onSelectTimeframe = { viewModel.selectTimeframe(it) }
                )
            }

            DashboardTab.PREDICTIONS -> {
                PredictionsView(
                    multiSignals = state.multiTimeframe,
                    selectedTimeframe = state.selectedTimeframe,
                    currentPrice = currentPrice,
                    onSelectTimeframe = { viewModel.selectTimeframe(it) }
                )
            }

            DashboardTab.TRADINGVIEW_GAUGES -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TradingViewGaugeCard(gauge = state.tradingViewGauge)
                    IndicatorDashboard(
                        indicators = state.indicators,
                        currentPrice = currentPrice
                    )
                }
            }

            DashboardTab.DERIVATIVES_COINGLASS -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DerivativesFlowCard(derivatives = state.derivativesFlow)
                    OrderBookAndTradesView(
                        orderBook = state.orderBook,
                        recentTrades = state.recentTrades
                    )
                }
            }

            DashboardTab.CHART_SIGNALS -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeroSignalBanner(
                        signal = state.currentSignal,
                        onOpenBreakdown = { viewModel.toggleSignalBreakdown(true) }
                    )

                    CandleStickChart(
                        candles = state.candles,
                        onCandleSelected = { viewModel.inspectCandle(it) }
                    )
                }
            }

            DashboardTab.NEWS_SENTIMENT -> {
                NewsSentimentView(
                    marketSentiment = state.marketSentiment,
                    newsFeed = state.newsFeed
                )
            }

            DashboardTab.ORDER_BOOK -> {
                OrderBookAndTradesView(
                    orderBook = state.orderBook,
                    recentTrades = state.recentTrades
                )
            }

            DashboardTab.RISK_MATRIX -> {
                RiskMatrixCard(
                    risk = state.currentSignal?.riskManagement,
                    isBullish = state.currentSignal?.signalType?.isBullish ?: true
                )
            }
        }
    }
}

@Composable
private fun TopDashboardHeader(
    activeExchange: ExchangeSource,
    connectionStatus: ConnectionStatus,
    onSwitchExchange: (ExchangeSource) -> Unit,
    onRefresh: () -> Unit
) {
    var showExchangeMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_dashboard_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "BTC Signal Pro",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CryptoCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "BTC/USDT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoCyan
                    )
                }
            }

            // Connection Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val statusColor = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> CryptoGreen
                    ConnectionStatus.CONNECTING -> CryptoYellow
                    else -> CryptoRed
                }
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = connectionStatus.labelArabic,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Exchange Switcher
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoCardDark)
                        .border(1.dp, CryptoCardBorder, RoundedCornerShape(8.dp))
                        .clickable { showExchangeMenu = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "تبديل المنصة",
                        tint = CryptoCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = activeExchange.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                DropdownMenu(
                    expanded = showExchangeMenu,
                    onDismissRequest = { showExchangeMenu = false },
                    modifier = Modifier.background(CryptoSurfaceDark)
                ) {
                    ExchangeSource.values().forEach { exchange ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = exchange.displayName,
                                    color = if (exchange == activeExchange) CryptoCyan else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (exchange == activeExchange) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSwitchExchange(exchange)
                                showExchangeMenu = false
                            }
                        )
                    }
                }
            }

            // Refresh Button
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CryptoCardDark)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(8.dp))
                    .testTag("refresh_dashboard_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun LiveTickerHeader(
    ticker: TickerData,
    tickDirection: TickDirection
) {
    val isPositive = ticker.priceChangePercent >= 0
    val tickColor by animateColorAsState(
        targetValue = when (tickDirection) {
            TickDirection.UP -> CryptoGreen
            TickDirection.DOWN -> CryptoRed
            TickDirection.NONE -> TextPrimary
        },
        animationSpec = tween(300),
        label = "tick_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp))
            .testTag("live_ticker_card"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (ticker.price > 0) "$${String.format("%,.2f", ticker.price)}" else "--",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = tickColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isPositive) CryptoGreen.copy(alpha = 0.15f) else CryptoRed.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format("%+.2f%%", ticker.priceChangePercent),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) CryptoGreen else CryptoRed
                        )
                    }

                    Text(
                        text = "التغير: ${String.format("%+.1f", ticker.priceChange)} USDT",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "أعلى 24h: $${String.format("%,.0f", ticker.high24h)}",
                    fontSize = 10.sp,
                    color = CryptoGreen
                )
                Text(
                    text = "أدنى 24h: $${String.format("%,.0f", ticker.low24h)}",
                    fontSize = 10.sp,
                    color = CryptoRed
                )
                Text(
                    text = "الحجم: ${String.format("%,.0f", ticker.volume24h)} BTC",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun TimeframeSelectorRow(
    selectedTimeframe: Timeframe,
    onSelectTimeframe: (Timeframe) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeframe_selector_row"),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Timeframe.values().forEach { tf ->
            val isSelected = tf == selectedTimeframe
            val bg = if (isSelected) CryptoCyan.copy(alpha = 0.2f) else CryptoCardDark
            val border = if (isSelected) CryptoCyan else CryptoCardBorder
            val textCol = if (isSelected) CryptoCyan else TextMuted

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(8.dp))
                    .clickable { onSelectTimeframe(tf) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tf.shortLabel,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textCol
                )
            }
        }
    }
}

@Composable
private fun DashboardTabsRow(
    selectedTab: DashboardTab,
    onSelectTab: (DashboardTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Color.Transparent,
        contentColor = CryptoCyan,
        edgePadding = 0.dp,
        divider = {},
        indicator = { tabPositions ->
            if (selectedTab.ordinal < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = CryptoCyan,
                    height = 2.dp
                )
            }
        }
    ) {
        DashboardTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            Tab(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                text = {
                    Text(
                        text = tab.titleArabic,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CryptoCyan else TextMuted
                    )
                }
            )
        }
    }
}

@Composable
private fun HeroSignalBanner(
    signal: SignalScore?,
    onOpenBreakdown: () -> Unit
) {
    if (signal == null) return

    val isBullish = signal.probabilityBullish >= 50
    val primaryColor = if (isBullish) CryptoGreen else CryptoRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable { onOpenBreakdown() }
            .testTag("hero_signal_banner"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBullish) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = signal.signalType.titleArabic,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryColor
                    )
                    Text(
                        text = "احتمال الصعود: ${signal.probabilityBullish}% | الهبوط: ${signal.probabilityBearish}%",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "التفاصيل",
                    fontSize = 10.sp,
                    color = CryptoCyan,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "تفاصيل الإشارة",
                    tint = CryptoCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
