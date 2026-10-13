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
import androidx.compose.material.icons.filled.Security
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
import com.example.data.model.RiskManagementLevels
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCardDark
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RiskMatrixCard(
    risk: RiskManagementLevels?,
    isBullish: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (risk == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CryptoCardBorder, RoundedCornerShape(14.dp))
            .testTag("risk_matrix_card"),
        colors = CardDefaults.cardColors(containerColor = CryptoCardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        imageVector = Icons.Default.Security,
                        contentDescription = "إدارة المخاطر",
                        tint = CryptoYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "مصفوفة إدارة المخاطر وأهداف الصفقة",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CryptoYellow.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "العائد / المخاطرة 1 : ${String.format("%.1f", risk.riskRewardRatio)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic S/R Channel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CryptoCardBorder.copy(alpha = 0.5f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الدعم الديناميكي", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "$${formatPrice(risk.supportLevel)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .background(CryptoCardBorder)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("المقاومة الديناميكية", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "$${formatPrice(risk.resistanceLevel)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoRed
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .background(CryptoCardBorder)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("معدل التقلب (ATR)", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "$${formatPrice(risk.atr)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stop Loss Row
            RiskTargetRow(
                label = "وقف الخسارة المقترح (SL)",
                price = risk.stopLoss,
                percent = risk.stopLossPercent,
                color = CryptoRed,
                badge = "حماية رأس المال"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Take-Profit 1 Row
            RiskTargetRow(
                label = "الهدف الأول السريع (TP1)",
                price = risk.takeProfit1,
                percent = risk.takeProfit1Percent,
                color = CryptoGreen.copy(alpha = 0.85f),
                badge = "هدف سريع 1.5x"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Take-Profit 2 Row
            RiskTargetRow(
                label = "الهدف الثاني الرئيسي (TP2)",
                price = risk.takeProfit2,
                percent = risk.takeProfit2Percent,
                color = CryptoGreen,
                badge = "هدف رئيسي 2.5x"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Take-Profit 3 Row
            RiskTargetRow(
                label = "الهدف الثالث الممتد (TP3)",
                price = risk.takeProfit3,
                percent = risk.takeProfit3Percent,
                color = CryptoCyan,
                badge = "امتداد اتجاهي 4.0x"
            )
        }
    }
}

@Composable
private fun RiskTargetRow(
    label: String,
    price: Double,
    percent: Double,
    color: Color,
    badge: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x0CFFFFFF))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$${formatPrice(price)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "(${String.format("%+.2f%%", percent)})",
                fontSize = 10.sp,
                color = color.copy(alpha = 0.8f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CryptoCardBorder)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 8.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    return String.format("%,.1f", price)
}
