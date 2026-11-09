package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.CreatorUiState
import com.example.model.PackCategory
import com.example.ui.components.MinecraftCard
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
import java.io.File

@Composable
fun CreatorScreen(
    state: CreatorUiState,
    onPromptChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onVersionChange: (String) -> Unit,
    onNamespaceChange: (String) -> Unit,
    onMinEngineVersionChange: (String) -> Unit,
    onToggleAutoManifest: (Boolean) -> Unit,
    onSetManifestTab: (Int) -> Unit,
    onRegenerateUuids: () -> Unit,
    onCategoryChange: (PackCategory) -> Unit,
    onSymbolChange: (String) -> Unit,
    onPickIcon: (Uri) -> Unit,
    onAddModels: (List<Uri>) -> Unit,
    onApplyPreset: (String, String, PackCategory) -> Unit,
    onToggleDownloadGuide: (Boolean) -> Unit,
    onSaveToCustomUri: (Uri, File) -> Unit,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onPickIcon(uri)
    }

    val modelPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) onAddModels(uris)
    }

    // Native file saver so the user can choose where to download/save .mcaddon
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null && state.resultFile != null) {
            onSaveToCustomUri(uri, state.resultFile)
        }
    }

    val glyphs = listOf("⬡", "⚔️", "👾", "💎", "🛡️", "🔨", "🔮", "🧱")
    val engineVersions = listOf("1.21.50", "1.21.0", "1.20.0")

    val manifests = state.getPreviewManifests()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Idea & Description Card
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
                            text = "FERRAMENTA 02",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = ForgePurple
                        )
                        Text(
                            text = "Custom Addon Maker",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ForgeTextPrimary
                        )
                        Text(
                            text = "Descreva sua ideia e configure o addon Bedrock.",
                            fontSize = 13.sp,
                            color = ForgeTextSecondary
                        )
                    }

                    // Help Download Button
                    IconButton(
                        onClick = { onToggleDownloadGuide(!state.showDownloadGuide) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0C0A14))
                            .border(1.dp, ForgeLine, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = "Como fazer o download",
                            tint = ForgePurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Interactive Download Guide if expanded
                AnimatedVisibility(visible = state.showDownloadGuide) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF131024))
                            .border(1.dp, ForgePurple.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Como fazer o Download e Instalar no Minecraft:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD8B4FE)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Toque em '✦ Gerar Addon' no final da página.\n" +
                                    "2. Quando pronto, toque em 'Salvar no Celular' ou 'Baixar' para gravar o arquivo .mcaddon.\n" +
                                    "3. Para instalar sem procurar pastas, toque em 'Abrir direto no Minecraft' — o jogo iniciará e importará o pacote automaticamente!\n" +
                                    "4. No Minecraft, entre em 'Criar Novo Mundo' > 'Pacotes de Comportamento' > ative seu addon.",
                                fontSize = 12.sp,
                                color = ForgeTextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Text(
                    text = "Ideias Rápidas (Toque para preencher):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ForgeTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip("🍄 Infecção Fúngica") {
                        onApplyPreset(
                            "Infecção Fúngica",
                            "Crie uma infecção fúngica com blocos contaminados que espalham esporos, criaturas mutantes agressivas e vegetação tóxica.",
                            PackCategory.COMPLETE
                        )
                    }
                    PresetChip("⚔️ Espada do Vazio") {
                        onApplyPreset(
                            "Espada do Vazio",
                            "Espada lendária forjada com partículas do Void, dano crítico massivo e lentidão ao golpear inimigos.",
                            PackCategory.ITEM
                        )
                    }
                    PresetChip("🗿 Gólem Guardião") {
                        onApplyPreset(
                            "Gólem Guardião",
                            "Criatura guardiã gigante resistente a fogo com alta armadura e soco sísmico que afasta monstros.",
                            PackCategory.ENTITY
                        )
                    }
                    PresetChip("💎 Minério Cósmico") {
                        onApplyPreset(
                            "Minério Cósmico",
                            "Bloco de minério luminoso que brilha no escuro, dropa cristais arcanos e serve para criar itens mágicos.",
                            PackCategory.BLOCK
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category selector
                Text(
                    text = "Tipo de Conteúdo:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForgeTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PackCategory.entries.forEach { cat ->
                        val isSelected = cat == state.category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ForgeViolet else Color(0xFF0C0A14))
                                .border(1.dp, if (isSelected) ForgePurple else ForgeLine, RoundedCornerShape(10.dp))
                                .clickable { onCategoryChange(cat) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${cat.icon} ${cat.displayName}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else ForgeTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prompt TextArea
                Text(
                    text = "Descrição do addon:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ForgeTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                        .testTag("addon_prompt_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0C0A14),
                        unfocusedContainerColor = Color(0xFF0C0A14),
                        focusedBorderColor = ForgePurple,
                        unfocusedBorderColor = Color(0xFF342B4C),
                        focusedTextColor = ForgeTextPrimary,
                        unfocusedTextColor = ForgeTextPrimary
                    ),
                    placeholder = {
                        Text(
                            "Ex.: Crie uma infecção com blocos contaminados, criaturas mutantes...",
                            color = ForgeTextMuted,
                            fontSize = 13.sp
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Name & Version Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1.4f)) {
                        Text("Nome do addon:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ForgeTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = onNameChange,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0C0A14),
                                unfocusedContainerColor = Color(0xFF0C0A14),
                                focusedBorderColor = ForgePurple,
                                unfocusedBorderColor = Color(0xFF342B4C),
                                focusedTextColor = ForgeTextPrimary,
                                unfocusedTextColor = ForgeTextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("addon_name_input")
                        )
                    }

                    Column(modifier = Modifier.weight(0.8f)) {
                        Text("Versão:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ForgeTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = state.version,
                            onValueChange = onVersionChange,
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0C0A14),
                                unfocusedContainerColor = Color(0xFF0C0A14),
                                focusedBorderColor = ForgePurple,
                                unfocusedBorderColor = Color(0xFF342B4C),
                                focusedTextColor = ForgeTextPrimary,
                                unfocusedTextColor = ForgeTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // =========================================================
        // AUTO MANIFEST CARD (Solução solicitada pelo usuário!)
        // =========================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ForgePanel.copy(alpha = 0.95f))
                .border(1.dp, ForgePurple.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚙️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto Manifest & UUIDs",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = ForgeTextPrimary
                            )
                        }
                        Text(
                            text = "Gera e sincroniza os manifests Bedrock com UUIDs únicos v4",
                            fontSize = 12.sp,
                            color = ForgeTextSecondary
                        )
                    }

                    Switch(
                        checked = state.autoManifestEnabled,
                        onCheckedChange = onToggleAutoManifest,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ForgeViolet,
                            uncheckedThumbColor = ForgeTextMuted,
                            uncheckedTrackColor = Color(0xFF0C0A14)
                        )
                    )
                }

                AnimatedVisibility(visible = state.autoManifestEnabled) {
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        // Namespace and Engine Version Configuration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Namespace / Prefixo:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ForgeTextMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = state.namespace,
                                    onValueChange = onNamespaceChange,
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF0C0A14),
                                        unfocusedContainerColor = Color(0xFF0C0A14),
                                        focusedBorderColor = ForgePurple,
                                        unfocusedBorderColor = Color(0xFF342B4C),
                                        focusedTextColor = ForgeTextPrimary,
                                        unfocusedTextColor = ForgeTextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Versão Mínima do Motor:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ForgeTextMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    engineVersions.forEach { ev ->
                                        val isSel = state.minEngineVersion == ev
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) ForgeViolet else Color(0xFF0C0A14))
                                                .border(1.dp, if (isSel) ForgePurple else ForgeLine, RoundedCornerShape(8.dp))
                                                .clickable { onMinEngineVersionChange(ev) }
                                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = ev,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) Color.White else ForgeTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // UUIDs information & Regenerate button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C0A14))
                                .border(1.dp, Color(0xFF29233D), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ForgeEmerald)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "UUIDs Vinculados (Auto)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForgeEmerald
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = onRegenerateUuids,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ForgePurple.copy(alpha = 0.5f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgePurple),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Regerar UUIDs", fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "BP UUID: ${state.bpHeaderUuid}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ForgeTextMuted
                                )
                                Text(
                                    text = "RP UUID: ${state.rpHeaderUuid} (Dependency Link OK)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ForgeTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live Manifest Code Viewer (BP vs RP)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (state.manifestTabSelected == 0) ForgeViolet else Color(0xFF0C0A14))
                                    .clickable { onSetManifestTab(0) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "manifest.json (BP)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.manifestTabSelected == 0) Color.White else ForgeTextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (state.manifestTabSelected == 1) ForgeViolet else Color(0xFF0C0A14))
                                    .clickable { onSetManifestTab(1) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "manifest.json (RP)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.manifestTabSelected == 1) Color.White else ForgeTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Copy button
                            val currentJson = if (state.manifestTabSelected == 0) manifests.bpManifestJson else manifests.rpManifestJson
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("manifest.json", currentJson))
                                    Toast.makeText(context, "Manifest copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeLine),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgeTextSecondary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copiar", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Code Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF07070B))
                                .border(1.dp, Color(0xFF252035), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = if (state.manifestTabSelected == 0) manifests.bpManifestJson else manifests.rpManifestJson,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFA7F3D0),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 3D Models Section Card
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
                    text = "Modelos 3D do addon",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForgeTextPrimary
                )
                Text(
                    text = "Adicione modelos Bedrock e arquivos 3D. Os arquivos são organizados no Resource Pack.",
                    fontSize = 12.sp,
                    color = ForgeTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF100D1B))
                        .border(1.5.dp, Color(0xFF594276), RoundedCornerShape(14.dp))
                        .clickable { modelPicker.launch("*/*") }
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🧊", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Importar modelos 3D (.geo.json, .obj, texturas)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForgeTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (state.modelFiles.isEmpty()) "Nenhum arquivo 3D extra selecionado (o gerador cria a geometria padrão)"
                            else "${state.modelFiles.size} arquivo(s) 3D anexado(s)",
                            fontSize = 11.sp,
                            color = if (state.modelFiles.isEmpty()) ForgeTextMuted else ForgeEmerald
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { modelPicker.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForgeViolet,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Selecionar modelos", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Visual Identity Card with Live Minecraft Card Preview
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
                    text = "Identidade Visual & Ícone",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForgeTextPrimary
                )
                Text(
                    text = "Escolha um ícone temático ou envie uma imagem da sua galeria.",
                    fontSize = 12.sp,
                    color = ForgeTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Glyph Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    glyphs.forEach { g ->
                        val isPicked = state.iconSymbol == g && state.iconBitmap == null
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPicked) ForgeViolet else Color(0xFF0C0A14))
                                .border(1.dp, if (isPicked) ForgePurple else ForgeLine, RoundedCornerShape(8.dp))
                                .clickable { onSymbolChange(g) }
                        ) {
                            Text(text = g, fontSize = 18.sp)
                        }
                    }

                    // Gallery image button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C0A14))
                            .border(1.dp, ForgeLine, RoundedCornerShape(8.dp))
                            .clickable { imagePicker.launch("image/*") }
                            .padding(horizontal = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = ForgePurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galeria", fontSize = 11.sp, color = ForgeTextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Preview: Authentic Minecraft Card
                MinecraftCard(
                    packName = state.name,
                    version = state.version,
                    description = "Pacote de comportamento e recursos",
                    iconBitmap = state.iconBitmap,
                    iconSymbol = state.iconSymbol
                )
            }
        }

        // Generate Action Card
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gerar pacote Bedrock",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForgeTextPrimary
                        )
                        Text(
                            text = "Cria Behavior Pack + Resource Pack vinculados (.mcaddon).",
                            fontSize = 12.sp,
                            color = ForgeTextSecondary
                        )
                    }

                    Button(
                        onClick = onGenerate,
                        enabled = !state.isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForgePurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("generate_addon_button")
                    ) {
                        if (state.isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gerando...", fontSize = 12.sp)
                        } else {
                            Text("✦ Gerar Addon", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Erro: ${state.error}",
                        fontSize = 12.sp,
                        color = ForgeRose
                    )
                }

                // =========================================================
                // DOWNLOAD & EXPORT PANEL (Após geração)
                // =========================================================
                if (state.resultFile != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ForgeEmerald.copy(alpha = 0.08f))
                            .border(1.dp, ForgeEmerald.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForgeEmerald, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Addon pronto para Download e Jogo!",
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

                            // Action 1: Direct Open in Minecraft (Easiest option for players)
                            Button(
                                onClick = { MinecraftLauncher.openInMinecraft(context, state.resultFile) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF15803D),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_minecraft_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🚀 Abrir direto no Minecraft (Importar Addon)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action 2 & 3: Save to Device (Downloads or Custom Folder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Save with native Android file picker
                                Button(
                                    onClick = { createDocLauncher.launch(state.resultFile.name) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ForgeViolet,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("save_as_button")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salvar no Celular", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Quick save to Downloads folder
                                Button(
                                    onClick = { MinecraftLauncher.saveToDownloads(context, state.resultFile) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E1B33),
                                        contentColor = ForgeTextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Downloads", fontSize = 12.sp)
                                }

                                // Share
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
            }
        }

        Text(
            text = "O gerador cria manifests e pastas padrão. Modelos Bedrock .geo.json são incluídos na estrutura; arquivos 3D OBJ/GLTF/GLB são preservados como fontes de referência.",
            fontSize = 11.sp,
            color = ForgeTextMuted,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PresetChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF19162A))
            .border(1.dp, Color(0xFF342B4C), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFD8B4FE)
        )
    }
}
