package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForgeEmerald
import com.example.ui.theme.ForgeLine
import com.example.ui.theme.ForgePanel
import com.example.ui.theme.ForgePurple
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary
import com.example.ui.theme.ForgeViolet
import com.example.util.MinecraftLauncher

@Composable
fun StatusScreen(
    isMinecraftInstalled: Boolean,
    onCheckMinecraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // System Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ForgePanel.copy(alpha = 0.95f))
                .border(1.dp, ForgeLine, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "STATUS DO SISTEMA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = ForgePurple
                )
                Text(
                    text = "Motor Local & Ambiente Bedrock",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ForgeTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Engine State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C0A14))
                        .border(1.dp, ForgeLine, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(ForgeEmerald)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Motor Nativo Android Ativo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForgeTextPrimary
                            )
                            Text(
                                text = "Processamento 100% no dispositivo. Nenhum arquivo ou addon é enviado a servidores externos.",
                                fontSize = 11.sp,
                                color = ForgeTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Minecraft PE Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C0A14))
                        .border(1.dp, ForgeLine, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (isMinecraftInstalled) ForgeEmerald else Color(0xFFFBBF24))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isMinecraftInstalled) "Minecraft Bedrock Detectado" else "Minecraft Bedrock não detectado",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForgeTextPrimary
                            )
                            Text(
                                text = if (isMinecraftInstalled) "Pacote com.mojang.minecraftpe encontrado. Os pacotes podem ser abertos diretamente no jogo."
                                else "O jogo não foi detectado no caminho padrão. Você pode salvar os pacotes em Downloads ou compartilhar para qualquer gerenciador de arquivos.",
                                fontSize = 11.sp,
                                color = ForgeTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCheckMinecraft,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForgeViolet,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verificar Conexão & App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (isMinecraftInstalled) {
                        OutlinedButton(
                            onClick = {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(MinecraftLauncher.MINECRAFT_PACKAGE)
                                if (launchIntent != null) context.startActivity(launchIntent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF15803D)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF86EFAC))
                        ) {
                            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abrir Jogo", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Modding Reference Guide Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ForgePanel.copy(alpha = 0.95f))
                .border(1.dp, ForgeLine, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "GUIA RÁPIDO BEDROCK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = ForgePurple
                )
                Text(
                    text = "Como funcionam addons Bedrock",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForgeTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideItem(
                    title = "Behavior Pack (BP)",
                    desc = "Controla a lógica do jogo: vida, ataque, IA, spawn rules, componentes de blocos e mecânicas de itens."
                )
                GuideItem(
                    title = "Resource Pack (RP)",
                    desc = "Controla a parte visual: modelos 3D (.geo.json), texturas (.png), sons, animações e nomes de tradução (.lang)."
                )
                GuideItem(
                    title = "Formato .mcaddon",
                    desc = "Arquivo compactado que une o Behavior Pack e o Resource Pack com UUIDs vinculados em um único instalador."
                )
                GuideItem(
                    title = "Erro Comum: 'Falha ao importar pacote'",
                    desc = "Geralmente causado por vírgula sobrando (trailing comma) no JSON ou UUID duplicado. Use a aba Corretor para reparar automaticamente!"
                )
            }
        }

        // Privacy Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F0D1C))
                .border(1.dp, ForgeLine, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Security, contentDescription = null, tint = ForgePurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Privacidade e Segurança: Bedrock Forge Studio processa todos os modelos, JSONs e pacotes localmente na memória do seu aparelho Android. Nenhum dado é coletado ou rastreado.",
                    fontSize = 11.sp,
                    color = ForgeTextMuted,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun GuideItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = "• $title",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD8B4FE)
        )
        Text(
            text = desc,
            fontSize = 12.sp,
            color = ForgeTextSecondary,
            lineHeight = 17.sp,
            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
        )
    }
}
