package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DerivativesFlowData
import com.example.ui.theme.CryptoBgDark
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardDark
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoOrange
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DerivativesFlowCard(
    derivatives: DerivativesFlowData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(16.dp))
            .testTag("derivatives_flow_card"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = CryptoOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "بيانات مشتقات وسيولة كوين جلاس (CoinGlass Flow)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CryptoOrange.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "تدفقات العقود المباشرة",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Funding Rate Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CryptoBgDark)
                    .border(1.dp, CryptoCardBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "معدل التمويل اللحظي (Funding Rate)",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = String.format("%+.4f%%", derivatives.fundingRatePercent),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (derivatives.fundingRatePercent >= 0) CryptoGreen else CryptoRed
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = derivatives.fundingRateStatusArabic,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Open Interest & Liquidations Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CryptoBgDark)
                        .border(1.dp, CryptoCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text("العقود المفتوحة (OI)", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = derivatives.openInterestEstimatedUsd,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = CryptoCyan
                    )
                    Text(
                        text = String.format("%+.1f%% 24h", derivatives.openInterestDelta24h),
                        fontSize = 10.sp,
                        color = CryptoGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CryptoBgDark)
                        .border(1.dp, CryptoCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text("تصفية شورت (Shorts)", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = derivatives.shortLiquidationEstimatedUsd,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = CryptoGreen
                    )
                    Text(
                        text = "مقابل ${derivatives.longLiquidationEstimatedUsd} لونغ",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Liquidation Heatmap Summary Text
            Text(
                text = derivatives.liquidationDominanceArabic,
                fontSize = 11.sp,
                color = CryptoGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
