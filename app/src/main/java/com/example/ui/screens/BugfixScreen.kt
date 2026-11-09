package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BugfixUiState
import com.example.model.DiagnosticSeverity
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
fun BugfixScreen(
    state: BugfixUiState,
    onSelectFiles: (List<Uri>) -> Unit,
    onLoadBrokenSample: () -> Unit,
    onAnalyze: () -> Unit,
    onFixAndPackage: () -> Unit,
    onClearFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) onSelectFiles(uris)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Bugfix Card
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
                    text = "FERRAMENTA 03",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = ForgePurple
                )
                Text(
                    text = "Corretor de bugs Bedrock",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ForgeTextPrimary
                )
                Text(
                    text = "Envie um addon ou arquivos soltos. O analisador inspeciona sintaxe JSON, UUIDs e aplica correções seguras.",
                    fontSize = 13.sp,
                    color = ForgeTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Picker Box
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF100D1B))
                        .border(1.5.dp, Color(0xFF594276), RoundedCornerShape(16.dp))
                        .clickable { filePicker.launch("*/*") }
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🧰", fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Selecione o arquivo para diagnosticar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForgeTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ZIP, MCPACK, MCADDON, JSON, JS, MCFUNCTION",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ForgeTextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { filePicker.launch("*/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForgeViolet,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("bug_select_files_button")
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Escolher arquivos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onLoadBrokenSample,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ForgePurple.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgePurple),
                                modifier = Modifier.testTag("load_broken_sample_button")
                            ) {
                                Text("Arquivo Quebrado (Demo)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // File Status Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0C0A14))
                        .border(1.dp, Color(0xFF29233D), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    if (state.files.isEmpty()) {
                        Text("Nenhum arquivo selecionado.", fontSize = 12.sp, color = ForgeTextMuted)
                    } else {
                        Text(
                            text = "${state.files.size} arquivo(s) prontos para análise: " +
                                state.files.take(4).joinToString(", ") { it.name } +
                                if (state.files.size > 4) "..." else "",
                            fontSize = 12.sp,
                            color = ForgeTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAnalyze,
                        enabled = state.files.isNotEmpty() && !state.isAnalyzing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForgePurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analyze_bugs_button")
                    ) {
                        if (state.isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analisando...", fontSize = 12.sp)
                        } else {
                            Text("🔎 Analisar bugs", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onClearFiles,
                        enabled = state.files.isNotEmpty() && !state.isAnalyzing,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ForgeLine),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgeTextSecondary)
                    ) {
                        Text("Limpar", fontSize = 13.sp)
                    }
                }
            }
        }

        // Diagnostic Issues Summary (if report available)
        if (state.report != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141122))
                    .border(1.dp, ForgeLine, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resultado da Inspeção",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForgeTextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.report.errorsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ForgeRose.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${state.report.errorsCount} Erros",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForgeRose
                                    )
                                }
                            }
                            if (state.report.warningsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFBBF24).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${state.report.warningsCount} Avisos",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFBBF24)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    state.report.issues.forEach { issue ->
                        val (icon, color) = when (issue.severity) {
                            DiagnosticSeverity.ERROR -> "❌" to ForgeRose
                            DiagnosticSeverity.WARNING -> "⚠️" to Color(0xFFFBBF24)
                            DiagnosticSeverity.SUCCESS -> "✓" to ForgeEmerald
                            DiagnosticSeverity.INFO -> "ℹ️" to Color(0xFFA7F3D0)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = issue.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                                Text(
                                    text = issue.detail,
                                    fontSize = 11.sp,
                                    color = ForgeTextSecondary
                                )
                            }
                        }
                    }

                    if (!state.report.isJavaModDetected && state.report.issues.any { it.autoFixable }) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onFixAndPackage,
                            enabled = !state.isFixing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForgeEmerald,
                                contentColor = Color(0xFF042F2E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fix_and_package_button")
                        ) {
                            if (state.isFixing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF042F2E), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Aplicando correções...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🛠 Corrigir e Baixar Addon Limpo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Diagnostic Terminal
        ForgeTerminal(
            title = "Relatório de diagnóstico",
            subtitle = "ANÁLISE LOCAL DOS ARQUIVOS",
            statusBadge = if (state.isAnalyzing) "ANALISANDO" else if (state.fixedFile != null) "CORRIGIDO" else "AGUARDANDO",
            logs = state.logs
        )

        // Result Card if fixed
        if (state.fixedFile != null) {
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
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForgeEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Addon reparado com sucesso!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForgeEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.fixedInfo,
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
                            onClick = { MinecraftLauncher.openInMinecraft(context, state.fixedFile) },
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
                            onClick = { MinecraftLauncher.saveToDownloads(context, state.fixedFile) },
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
                            onClick = { MinecraftLauncher.shareAddon(context, state.fixedFile) },
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

        Text(
            text = "Limite técnico: a ferramenta verifica estrutura e erros conhecidos (vírgulas extras, UUIDs, namespaces e formato Bedrock). Não executa scripts JS. Faça backup do arquivo original antes de substituir.",
            fontSize = 11.sp,
            color = ForgeTextMuted,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
    }
}
