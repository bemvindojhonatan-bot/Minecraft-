package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogType
import com.example.model.ProcessLog
import com.example.ui.theme.ForgeEmerald
import com.example.ui.theme.ForgeLine
import com.example.ui.theme.ForgePurple
import com.example.ui.theme.ForgeRose
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeViolet

@Composable
fun ForgeTerminal(
    title: String = "Console de processamento",
    subtitle: String = "LOG EM TEMPO REAL",
    statusBadge: String = "AGUARDANDO",
    logs: List<ProcessLog>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF12101E).copy(alpha = 0.95f))
            .border(1.dp, ForgeLine, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForgeTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = ForgeTextMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0C0A14))
                    .border(1.dp, ForgeLine, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusBadge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = when (statusBadge) {
                        "PROCESSANDO" -> ForgePurple
                        "CONCLUÍDO" -> ForgeEmerald
                        "ERRO" -> ForgeRose
                        else -> ForgeTextMuted
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF07070B))
                .border(1.dp, Color(0xFF252035), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(logs) { log ->
                    val color = when (log.type) {
                        LogType.INFO -> Color(0xFFA7F3D0) // Mint green
                        LogType.SUCCESS -> ForgeEmerald
                        LogType.WARNING -> Color(0xFFFBBF24)
                        LogType.ERROR -> ForgeRose
                        LogType.ACCENT -> ForgePurple
                    }

                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = "[${log.timestamp}] ",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ForgeTextMuted
                        )
                        Text(
                            text = log.message,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = color,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
