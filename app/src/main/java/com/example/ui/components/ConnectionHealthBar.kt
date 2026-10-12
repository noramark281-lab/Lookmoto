package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionHeartbeat
import com.example.ui.theme.CryptoBgDark
import com.example.ui.theme.CryptoCardBorder
import com.example.ui.theme.CryptoCyan
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoOrange
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConnectionHealthBar(
    heartbeat: ConnectionHeartbeat,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val isWarning = heartbeat.isStale
    val statusColor = if (isWarning) CryptoRed else CryptoGreen

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isWarning) CryptoRed.copy(alpha = 0.12f) else CryptoBgDark)
            .border(1.dp, if (isWarning) CryptoRed.copy(alpha = 0.4f) else CryptoCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("connection_health_bar"),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )

                Text(
                    text = "${heartbeat.exchange.displayName}: ${heartbeat.statusMessageArabic}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isWarning) CryptoRed else TextSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${heartbeat.latencyMs}ms",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (heartbeat.latencyMs < 50) CryptoGreen else CryptoYellow
                )

                Text(
                    text = timeFormatter.format(Date(heartbeat.lastOfficialTickTime)),
                    fontSize = 9.sp,
                    color = TextMuted
                )
            }
        }

        AnimatedVisibility(visible = isWarning) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CryptoRed,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "تنبيه: انقطاع مؤقت في التوقيت الرسمي لمؤشر البيتكوين - الحماية التلقائية نشطة وإعادة الاتصال جارية",
                    fontSize = 9.sp,
                    color = CryptoRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
