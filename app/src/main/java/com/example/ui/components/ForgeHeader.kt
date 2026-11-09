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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForgeEmerald
import com.example.ui.theme.ForgeLine
import com.example.ui.theme.ForgePurple
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary
import com.example.ui.theme.ForgeViolet

@Composable
fun ForgeHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Hexagon Icon Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ForgeViolet.copy(alpha = 0.25f))
                    .border(1.dp, ForgePurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = "⬡",
                    fontSize = 24.sp,
                    color = ForgePurple,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MODDING WORKSPACE",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForgePurple.copy(alpha = 0.9f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BEDROCK ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = ForgeTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "FORGE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = ForgePurple,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = " STUDIO",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = ForgeTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            // Local Builder Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ForgeEmerald.copy(alpha = 0.12f))
                    .border(1.dp, ForgeEmerald.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(ForgeEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LOCAL BUILDER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForgeEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Crie, organize e empacote addons para Minecraft Bedrock diretamente no dispositivo.",
            fontSize = 13.sp,
            color = ForgeTextSecondary,
            lineHeight = 18.sp
        )
    }
}
