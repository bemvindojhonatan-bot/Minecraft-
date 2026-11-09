package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ForgeHeader
import com.example.ui.components.ForgeTabs
import com.example.ui.screens.BugfixScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.CreatorScreen
import com.example.ui.screens.StatusScreen
import com.example.ui.theme.BedrockForgeTheme
import com.example.ui.theme.ForgeBg
import com.example.ui.theme.ForgeViolet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BedrockForgeTheme {
                ForgeApp()
            }
        }
    }
}

@Composable
fun ForgeApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val converterState by viewModel.converterState.collectAsStateWithLifecycle()
    val creatorState by viewModel.creatorState.collectAsStateWithLifecycle()
    val bugfixState by viewModel.bugfixState.collectAsStateWithLifecycle()
    val isMinecraftInstalled by viewModel.isMinecraftInstalled.collectAsStateWithLifecycle()

    BackHandler(enabled = currentTab != AppTab.CONVERTER) {
        viewModel.setTab(AppTab.CONVERTER)
    }

    Scaffold(
        containerColor = ForgeBg,
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Subtle purple ambient aura in the upper-left, identical to the user's web prototype
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ForgeViolet.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.15f, 0f),
                        radius = size.width * 0.8f
                    )
                )
            }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            ForgeHeader()

            ForgeTabs(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentTab) {
                    AppTab.CONVERTER -> {
                        ConverterScreen(
                            state = converterState,
                            onSelectFiles = { viewModel.addConverterFiles(it) },
                            onLoadSample = { viewModel.loadSampleAddon() },
                            onAnalyzeAndPackage = { viewModel.analyzeAndPackage() },
                            onClearFiles = { viewModel.clearConverterFiles() }
                        )
                    }
                    AppTab.CREATOR -> {
                        CreatorScreen(
                            state = creatorState,
                            onPromptChange = { viewModel.updateCreatorPrompt(it) },
                            onNameChange = { viewModel.updateCreatorName(it) },
                            onVersionChange = { viewModel.updateCreatorVersion(it) },
                            onNamespaceChange = { viewModel.updateCreatorNamespace(it) },
                            onMinEngineVersionChange = { viewModel.updateCreatorMinEngineVersion(it) },
                            onToggleAutoManifest = { viewModel.toggleAutoManifest(it) },
                            onSetManifestTab = { viewModel.setManifestTab(it) },
                            onRegenerateUuids = { viewModel.regenerateUuids() },
                            onCategoryChange = { viewModel.updateCreatorCategory(it) },
                            onSymbolChange = { viewModel.updateCreatorSymbol(it) },
                            onPickIcon = { viewModel.setCreatorIconUri(it) },
                            onAddModels = { viewModel.addModelFiles(it) },
                            onApplyPreset = { name, prompt, cat -> viewModel.applyPreset(name, prompt, cat) },
                            onToggleDownloadGuide = { viewModel.toggleDownloadGuide(it) },
                            onSaveToCustomUri = { uri, file -> viewModel.saveToCustomUri(uri, file) },
                            onGenerate = { viewModel.generateAddon() }
                        )
                    }
                    AppTab.BUGFIX -> {
                        BugfixScreen(
                            state = bugfixState,
                            onSelectFiles = { viewModel.addBugfixFiles(it) },
                            onLoadBrokenSample = { viewModel.loadBrokenSample() },
                            onAnalyze = { viewModel.runBugDiagnostics() },
                            onFixAndPackage = { viewModel.fixAndPackageBugfix() },
                            onClearFiles = { viewModel.clearBugfixFiles() }
                        )
                    }
                    AppTab.STATUS -> {
                        StatusScreen(
                            isMinecraftInstalled = isMinecraftInstalled,
                            onCheckMinecraft = { viewModel.checkMinecraft() }
                        )
                    }
                }
            }
        }
    }
}
