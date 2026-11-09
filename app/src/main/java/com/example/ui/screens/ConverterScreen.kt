package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ConverterUiState
import com.example.ui.components.ForgeTerminal
import com.example.ui.theme.ForgeEmerald
import com.example.ui.theme.ForgeLine
import com.example.ui.theme.ForgePanel
import com.example.ui.theme.ForgePurple
import com.example.ui.theme.ForgeRose
import com.example.ui.theme.ForgeTextMuted
import com.example.ui.theme.ForgeTextPrimary
import com.example.ui.theme.ForgeTextSecondary
import com.example.ui.theme.ForgeViolet
import com.example.util.MinecraftLauncher

@Composable
fun ConverterScreen(
    state: ConverterUiState,
    onSelectFiles: (List<android.net.Uri>) -> Unit,
    onLoadSample: () -> Unit,
    onAnalyzeAndPackage: () -> Unit,
    onClearFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onSelectFiles(uris)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Tool Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ForgePanel.copy(alpha = 0.95f))
                .border(1.dp, ForgeLine, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FERRAMENTA 01",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = ForgePurple
                        )
                        Text(
                            text = "Conversor universal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ForgeTextPrimary
                        )
                        Text(
                            text = "Analise e empacote arquivos de addons Bedrock.",
                            fontSize = 13.sp,
                            color = ForgeTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, ForgeLine, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ZIP · MCPACK · MCADDON",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ForgeTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drop / Picker Zone
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF100D1B))
                        .border(1.5.dp, Color(0xFF594276), RoundedCornerShape(16.dp))
                        .clickable { filePickerLauncher.launch("*/*") }
                        .padding(22.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "📦", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selecione ou carregue seus arquivos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForgeTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Suporta ZIP, MCPACK, MCADDON, .geo.json e pastas de recursos",
                            fontSize = 12.sp,
                            color = ForgeTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForgeViolet,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("select_files_button")
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Selecionar arquivos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onLoadSample,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgePurple),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ForgePurple.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("load_sample_button")
                            ) {
                                Text("Usar Exemplo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // File List Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0C0A14))
                        .border(1.dp, Color(0xFF29233D), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    if (state.files.isEmpty()) {
                        Text(
                            text = "Nenhum arquivo selecionado.",
                            fontSize = 12.sp,
                            color = ForgeTextMuted
                        )
                    } else {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Arquivos preparados (${state.files.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForgeEmerald
                                )
                                Text(
                                    text = "${state.files.sumOf { it.size } / 1024} KB total",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ForgeTextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.files.take(6).joinToString(" · ") { "${it.name} (${it.size / 1024}k)" } +
                                    if (state.files.size > 6) " · +${state.files.size - 6} outros" else "",
                                fontSize = 11.sp,
                                color = ForgeTextSecondary,
                                maxLines = 2
                            )
                        }
                    }
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ ${state.error}",
                        fontSize = 12.sp,
                        color = ForgeRose
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAnalyzeAndPackage,
                        enabled = state.files.isNotEmpty() && !state.isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForgePurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analyze_convert_button")
                    ) {
                        if (state.isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processando...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("⚡ Analisar e empacotar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onClearFiles,
                        enabled = state.files.isNotEmpty() && !state.isProcessing,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ForgeLine),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgeTextSecondary)
                    ) {
                        Text("Limpar", fontSize = 13.sp)
                    }
                }
            }
        }

        // Processing Console Terminal
        ForgeTerminal(
            title = "Console de processamento",
            subtitle = "LOG EM TEMPO REAL",
            statusBadge = if (state.isProcessing) "PROCESSANDO" else if (state.resultFile != null) "CONCLUÍDO" else "AGUARDANDO",
            logs = state.logs
        )

        // Result Card if generated
        if (state.resultFile != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ForgeEmerald.copy(alpha = 0.08f))
                    .border(1.dp, ForgeEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ForgeEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pacote preparado com sucesso!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForgeEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.resultInfo,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ForgeTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { MinecraftLauncher.openInMinecraft(context, state.resultFile) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF15803D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abrir no Minecraft", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { MinecraftLauncher.saveToDownloads(context, state.resultFile) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForgeViolet,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Baixar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { MinecraftLauncher.shareAddon(context, state.resultFile) },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForgePurple.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgePurple),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Info Note
        Text(
            text = "Suporte 3D: modelos Bedrock .geo.json e texturas são preservados no pacote. OBJ/GLTF/GLB podem ser carregados como arquivos-fonte. Nota: mods Java (Forge/Fabric) não são traduzíveis automaticamente para Bedrock. O conversor valida manifests Bedrock e reempacota estruturas compatíveis; arquivos Java sem estrutura Bedrock serão recusados para evitar um pacote corrompido.",
            fontSize = 11.sp,
            color = ForgeTextMuted,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
    }
}
