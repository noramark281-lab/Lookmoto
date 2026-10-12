package com.example.ui

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.data.model.ExchangeSource
import com.example.data.model.SignalScore
import com.example.data.model.SignalType
import com.example.data.model.TickerData
import com.example.data.model.Timeframe
import com.example.ui.components.CandleStickChart
import com.example.ui.components.IndicatorDashboard
import com.example.ui.components.OrderBookAndTradesView
import com.example.ui.components.RiskMatrixCard
import com.example.ui.components.SignalBreakdownDialog
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CryptoDashboardScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // 1. Top Header Bar (Pair, Exchange Switcher, Connection Status, Refresh)
            TopDashboardHeader(
                activeExchange = state.activeExchange,
                connectionStatus = state.connectionStatus,
                onSwitchExchange = { viewModel.switchExchange(it) },
                onRefresh = { viewModel.refresh() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Real-Time Price Hero Card with Tick Flash Animation
            PriceHeroCard(
                ticker = state.ticker,
                tickDirection = state.tickDirection
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Probabilistic Signal Master Meter
            SignalProbabilisticCard(
                signal = state.currentSignal,
                timeframe = state.selectedTimeframe,
                onOpenDetails = { viewModel.toggleSignalBreakdown(true) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Interactive Timeframe Toggle Bar (1m, 5m, 30m)
            TimeframeToggleBar(
                selectedTimeframe = state.selectedTimeframe,
                onSelectTimeframe = { viewModel.setTimeframe(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Dashboard View Navigation Tabs
            DashboardTabBar(
                selectedTab = state.selectedTab,
                onSelectTab = { viewModel.setTab(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Tab Content Switcher
            when (state.selectedTab) {
                DashboardTab.CHART_SIGNALS -> {
                    // Candlestick Chart with EMAs and Bollinger Overlays
                    CandleStickChart(
                        candles = state.candles,
                        onCandleSelected = { viewModel.inspectCandle(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Risk Matrix Summary
                    RiskMatrixCard(
                        risk = state.currentSignal?.riskManagement,
                        isBullish = state.currentSignal?.probabilityBullish?.let { it >= 50 } ?: true
                    )
                }

                DashboardTab.INDICATORS -> {
                    // Full Technical Indicators Dashboard
                    IndicatorDashboard(
                        indicators = state.indicators,
                        currentPrice = state.ticker.price
                    )
                }

                DashboardTab.RISK_MATRIX -> {
                    // Dedicated Risk Levels & Dynamic Support/Resistance View
                    RiskMatrixCard(
                        risk = state.currentSignal?.riskManagement,
                        isBullish = state.currentSignal?.probabilityBullish?.let { it >= 50 } ?: true
                    )
                }

                DashboardTab.ORDER_BOOK -> {
                    // Live Order Book Depth & Agg Trades Tape
                    OrderBookAndTradesView(
                        orderBook = state.orderBook,
                        recentTrades = state.recentTrades
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Signal Breakdown Details Modal Dialog
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
fun TopDashboardHeader(
    activeExchange: ExchangeSource,
    connectionStatus: ConnectionStatus,
    onSwitchExchange: (ExchangeSource) -> Unit,
    onRefresh: () -> Unit
) {
    var exchangeMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_dashboard_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pair Badge & Exchange Dropdown
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CryptoYellow.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "₿ BTC/USDT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoYellow
                )
            }

            // Exchange Switcher Chip
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoCardDark)
                        .border(1.dp, CryptoCardBorder, RoundedCornerShape(8.dp))
                        .clickable { exchangeMenuExpanded = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = activeExchange.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Exchange",
                        tint = CryptoCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = exchangeMenuExpanded,
                    onDismissRequest = { exchangeMenuExpanded = false },
                    modifier = Modifier.background(CryptoSurfaceDark)
                ) {
                    DropdownMenuItem(
                        text = { Text("Binance Spot (Primary)", color = TextPrimary, fontSize = 12.sp) },
                        onClick = {
                            onSwitchExchange(ExchangeSource.BINANCE)
                            exchangeMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("MEXC Spot (High Frequency)", color = TextPrimary, fontSize = 12.sp) },
                        onClick = {
                            onSwitchExchange(ExchangeSource.MEXC)
                            exchangeMenuExpanded = false
                        }
                    )
                }
            }
        }

        // Connection Status & Refresh Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live Status Indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CryptoCardDark)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val dotColor = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> CryptoGreen
                    ConnectionStatus.CONNECTING -> CryptoYellow
                    else -> CryptoRed
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = connectionStatus.label,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CryptoCardDark)
                    .border(1.dp, CryptoCardBorder, CircleShape)
                    .testTag("refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Feed",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PriceHeroCard(
    ticker: TickerData,
    tickDirection: TickDirection
) {
    // Millisecond tick flash animation colors
    val animatedTextColor by animateColorAsState(
        targetValue = when (tickDirection) {
            TickDirection.UP -> CryptoGreenGlow
            TickDirection.DOWN -> CryptoRedGlow
            TickDirection.NONE -> TextPrimary
        },
        animationSpec = tween(durationMillis = 250),
        label = "price_color_flash"
    )

    val animatedBgFlash by animateColorAsState(
        targetValue = when (tickDirection) {
            TickDirection.UP -> CryptoGreen.copy(alpha = 0.12f)
            TickDirection.DOWN -> CryptoRed.copy(alpha = 0.12f)
            TickDirection.NONE -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 250),
        label = "bg_color_flash"
    )

    val isPositive24h = ticker.priceChangePercent >= 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CryptoCardDark)
            .background(animatedBgFlash)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("price_hero_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Live Price
                Column {
                    Text("Spot Index Price", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = if (ticker.price > 0) "$${String.format("%,.2f", ticker.price)}" else "$--",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = animatedTextColor
                    )
                }

                // 24h Change Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPositive24h) CryptoGreen.copy(alpha = 0.2f) else CryptoRed.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive24h) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive24h) CryptoGreen else CryptoRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%+.2f%%", ticker.priceChangePercent),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive24h) CryptoGreen else CryptoRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 24h Stats Row (High, Low, 24h Volume)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x0CFFFFFF))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("24h High", fontSize = 9.sp, color = TextMuted)
                    Text("$${String.format("%,.1f", ticker.high24h)}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("24h Low", fontSize = 9.sp, color = TextMuted)
                    Text("$${String.format("%,.1f", ticker.low24h)}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("24h Vol (BTC)", fontSize = 9.sp, color = TextMuted)
                    Text("${String.format("%,.0f", ticker.volume24h)} ₿", fontSize = 11.sp, color = CryptoCyan, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun SignalProbabilisticCard(
    signal: SignalScore?,
    timeframe: Timeframe,
    onOpenDetails: () -> Unit
) {
    val bullishPct = signal?.probabilityBullish ?: 50
    val bearishPct = signal?.probabilityBearish ?: 50
    val signalType = signal?.signalType ?: SignalType.NEUTRAL

    val signalColor = when (signalType) {
        SignalType.STRONG_BUY -> CryptoGreenGlow
        SignalType.BUY -> CryptoGreen
        SignalType.NEUTRAL -> CryptoYellow
        SignalType.SELL -> CryptoRed
        SignalType.STRONG_SELL -> CryptoRedGlow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        CryptoCardDark,
                        signalColor.copy(alpha = 0.08f)
                    )
                )
            )
            .border(1.dp, signalColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("signal_probabilistic_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PROBABILISTIC SIGNAL (${timeframe.label})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = signalType.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = signalColor
                    )
                }

                IconButton(
                    onClick = onOpenDetails,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CryptoCardBorder)
                        .testTag("open_signal_logic_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View Signal Logic",
                        tint = CryptoCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Probability Dual Meter Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bullish $bullishPct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoGreen
                )
                Text(
                    text = "Bearish $bearishPct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoRed
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Two-tone progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CryptoRed)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(bullishPct / 100f)
                        .height(8.dp)
                        .background(CryptoGreen)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Key Confluence Teaser
            val firstConfluence = signal?.keyConfluences?.firstOrNull() ?: "Aggregating technical metrics..."
            Text(
                text = "• $firstConfluence",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TimeframeToggleBar(
    selectedTimeframe: Timeframe,
    onSelectTimeframe: (Timeframe) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CryptoCardDark)
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(10.dp))
            .padding(4.dp)
            .testTag("timeframe_toggle_bar"),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Timeframe.values().forEach { tf ->
            val isSelected = tf == selectedTimeframe
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) CryptoCyan.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelectTimeframe(tf) }
                    .padding(vertical = 7.dp)
                    .testTag("timeframe_button_${tf.label}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tf.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) CryptoCyan else TextSecondary
                )
            }
        }
    }
}

@Composable
fun DashboardTabBar(
    selectedTab: DashboardTab,
    onSelectTab: (DashboardTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Color.Transparent,
        contentColor = TextPrimary,
        edgePadding = 0.dp,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = CryptoCyan,
                height = 2.dp
            )
        },
        divider = {}
    ) {
        DashboardTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            Tab(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CryptoCyan else TextSecondary
                    )
                },
                modifier = Modifier.testTag("dashboard_tab_${tab.name}")
            )
        }
    }
}
