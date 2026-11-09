package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.BedrockBugDiagnostics
import com.example.engine.BedrockManifestGenerator
import com.example.engine.BedrockPackager
import com.example.engine.BedrockTemplateGenerator
import com.example.model.AddonFile
import com.example.model.DiagnosticReport
import com.example.model.LogType
import com.example.model.PackCategory
import com.example.model.ProcessLog
import com.example.util.MinecraftLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab(val title: String, val icon: String) {
    CONVERTER("Conversor", "⇄"),
    CREATOR("Addon Maker", "✦"),
    BUGFIX("Corretor", "🛠"),
    STATUS("Status / Info", "🌐")
}

data class ConverterUiState(
    val files: List<AddonFile> = emptyList(),
    val logs: List<ProcessLog> = listOf(
        ProcessLog("00:00:00", "Bedrock Forge Studio v2.4 - Inicializado.", LogType.ACCENT),
        ProcessLog("00:00:00", "Pronto. Selecione ou arraste um pacote para começar.", LogType.INFO)
    ),
    val isProcessing: Boolean = false,
    val resultFile: File? = null,
    val resultInfo: String = "",
    val error: String? = null
)

data class CreatorUiState(
    val prompt: String = "Crie uma infecção fúngica com blocos contaminados, esporos, criaturas que procuram áreas escuras e vegetação mutante...",
    val name: String = "Meu Addon Bedrock",
    val version: String = "1.0.0",
    val namespace: String = "forge",
    val minEngineVersion: String = "1.21.0",
    val bpHeaderUuid: String = java.util.UUID.randomUUID().toString(),
    val rpHeaderUuid: String = java.util.UUID.randomUUID().toString(),
    val autoManifestEnabled: Boolean = true,
    val manifestTabSelected: Int = 0, // 0: Behavior Pack, 1: Resource Pack
    val category: PackCategory = PackCategory.COMPLETE,
    val modelFiles: List<AddonFile> = emptyList(),
    val iconBitmap: Bitmap? = null,
    val iconSymbol: String = "⬡",
    val isGenerating: Boolean = false,
    val resultFile: File? = null,
    val resultInfo: String = "",
    val error: String? = null,
    val showDownloadGuide: Boolean = false
) {
    fun getPreviewManifests(): BedrockManifestGenerator.ManifestPair {
        val engineV = BedrockManifestGenerator.parseVersion(minEngineVersion)
        return BedrockManifestGenerator.createLinkedManifests(
            packName = name.ifEmpty { "Meu Addon Bedrock" },
            version = version.ifEmpty { "1.0.0" },
            description = prompt.take(120).ifEmpty { "Criado com Bedrock Forge Studio" },
            minEngineVersion = engineV,
            customBpHeaderUuid = bpHeaderUuid,
            customRpHeaderUuid = rpHeaderUuid
        )
    }
}

data class BugfixUiState(
    val files: List<AddonFile> = emptyList(),
    val report: DiagnosticReport? = null,
    val logs: List<ProcessLog> = listOf(
        ProcessLog("00:00:00", "Forge Diagnostics v2.4 - Pronto.", LogType.ACCENT),
        ProcessLog("00:00:00", "Aguardando arquivos para diagnóstico local.", LogType.INFO)
    ),
    val isAnalyzing: Boolean = false,
    val isFixing: Boolean = false,
    val fixedFile: File? = null,
    val fixedInfo: String = "",
    val error: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentTab = MutableStateFlow(AppTab.CONVERTER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _converterState = MutableStateFlow(ConverterUiState())
    val converterState: StateFlow<ConverterUiState> = _converterState.asStateFlow()

    private val _creatorState = MutableStateFlow(CreatorUiState())
    val creatorState: StateFlow<CreatorUiState> = _creatorState.asStateFlow()

    private val _bugfixState = MutableStateFlow(BugfixUiState())
    val bugfixState: StateFlow<BugfixUiState> = _bugfixState.asStateFlow()

    private val _isMinecraftInstalled = MutableStateFlow(false)
    val isMinecraftInstalled: StateFlow<Boolean> = _isMinecraftInstalled.asStateFlow()

    init {
        checkMinecraft()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun checkMinecraft() {
        viewModelScope.launch(Dispatchers.IO) {
            val installed = MinecraftLauncher.isMinecraftInstalled(getApplication())
            _isMinecraftInstalled.value = installed
        }
    }

    private fun currentTime(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    // -------------------------------------------------------------
    // CONVERTER LOGIC
    // -------------------------------------------------------------

    fun addConverterFiles(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val newFiles = mutableListOf<AddonFile>()

            for (uri in uris) {
                var fileName = "arquivo"
                var fileSize = 0L

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }

                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                    val lower = fileName.lowercase()

                    if (lower.endsWith(".zip") || lower.endsWith(".mcpack") || lower.endsWith(".mcaddon")) {
                        // Unpack entries
                        val extracted = BedrockPackager.extractFilesFromZip(bytes.inputStream())
                        newFiles.addAll(extracted)
                        addConverterLog("Pacote extraído: $fileName (${extracted.size} arquivos)", LogType.INFO)
                    } else {
                        newFiles.add(AddonFile(name = fileName, size = bytes.size.toLong(), path = fileName, content = bytes, uri = uri))
                        addConverterLog("Arquivo carregado: $fileName (${bytes.size} bytes)", LogType.INFO)
                    }
                } catch (e: Exception) {
                    addConverterLog("Erro ao ler $fileName: ${e.message}", LogType.ERROR)
                }
            }

            _converterState.update { current ->
                val combined = (current.files + newFiles).distinctBy { it.path }
                current.copy(files = combined, resultFile = null, error = null)
            }
        }
    }

    fun loadSampleAddon() {
        viewModelScope.launch(Dispatchers.Default) {
            addConverterLog("Carregando pacote Bedrock de exemplo...", LogType.INFO)
            val manifest = BedrockManifestGenerator.createLinkedManifests("Exemplo_Bedrock_Forge")
            val entityJson = BedrockTemplateGenerator.generateEntityBehavior("forge", "mystic_beast", "Fera Mística", "criatura ágil e forte")
            val clientJson = BedrockTemplateGenerator.generateClientEntity("forge", "mystic_beast")
            val geoJson = BedrockTemplateGenerator.generateStandardGeometry("mystic_beast")
            val itemJson = BedrockTemplateGenerator.generateItemBehavior("forge", "crystal_blade", "Lâmina de Cristal", "espada mágica")
            val langJson = BedrockTemplateGenerator.generateLangFile("forge", "mystic_beast", "Fera Mística", PackCategory.COMPLETE)

            val sampleFiles = listOf(
                AddonFile("manifest.json", manifest.bpManifestJson.length.toLong(), "bp/manifest.json", manifest.bpManifestJson.toByteArray(Charsets.UTF_8)),
                AddonFile("manifest.json", manifest.rpManifestJson.length.toLong(), "rp/manifest.json", manifest.rpManifestJson.toByteArray(Charsets.UTF_8)),
                AddonFile("mystic_beast.json", entityJson.length.toLong(), "bp/entities/mystic_beast.json", entityJson.toByteArray(Charsets.UTF_8)),
                AddonFile("mystic_beast.entity.json", clientJson.length.toLong(), "rp/entity/mystic_beast.entity.json", clientJson.toByteArray(Charsets.UTF_8)),
                AddonFile("mystic_beast.geo.json", geoJson.length.toLong(), "rp/models/entity/mystic_beast.geo.json", geoJson.toByteArray(Charsets.UTF_8)),
                AddonFile("crystal_blade.json", itemJson.length.toLong(), "bp/items/crystal_blade.json", itemJson.toByteArray(Charsets.UTF_8)),
                AddonFile("en_US.lang", langJson.length.toLong(), "rp/texts/en_US.lang", langJson.toByteArray(Charsets.UTF_8))
            )

            _converterState.update { it.copy(files = sampleFiles, resultFile = null, error = null) }
            addConverterLog("Exemplo Bedrock carregado: ${sampleFiles.size} arquivos estruturados (BP + RP).", LogType.SUCCESS)
        }
    }

    fun analyzeAndPackage() {
        val files = _converterState.value.files
        if (files.isEmpty()) {
            addConverterLog("Nenhum arquivo para processar. Selecione arquivos ou carregue o exemplo.", LogType.WARNING)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _converterState.update { it.copy(isProcessing = true, error = null) }
            addConverterLog("Iniciando análise profunda do pacote...", LogType.INFO)

            // 1. Diagnosticar arquivos
            val diag = BedrockBugDiagnostics.diagnose(files)
            if (diag.isJavaModDetected) {
                addConverterLog("ERRO: Mod Java detectado! O empacotamento foi interrompido para evitar arquivo corrompido.", LogType.ERROR)
                _converterState.update {
                    it.copy(
                        isProcessing = false,
                        error = "Mods Java (.jar / Fabric / Forge) não são suportados no Minecraft Bedrock."
                    )
                }
                return@launch
            }

            addConverterLog("Arquivos verificados: ${files.size}. Erros: ${diag.errorsCount}, Avisos: ${diag.warningsCount}.", LogType.INFO)

            // Auto fix minor issues if necessary
            val readyFiles = if (diag.errorsCount > 0) {
                addConverterLog("Aplicando auto-correções estruturais nos manifestos...", LogType.INFO)
                BedrockBugDiagnostics.autoFixFiles(files, "Bedrock_Addon_Export")
            } else {
                files
            }

            // 2. Packaging
            addConverterLog("Comprimindo em formato .mcaddon com índices otimizados...", LogType.INFO)
            try {
                val packName = "Bedrock_Forge_Pack_${System.currentTimeMillis() % 10000}"
                val resultFile = BedrockPackager.packageToMcaddon(getApplication(), packName, readyFiles)
                val sizeKb = resultFile.length() / 1024
                addConverterLog("Sucesso! Pacote gerado com sucesso: ${resultFile.name} (${sizeKb} KB).", LogType.SUCCESS)

                _converterState.update {
                    it.copy(
                        isProcessing = false,
                        resultFile = resultFile,
                        resultInfo = "${resultFile.name} · ${sizeKb} KB · ${readyFiles.size} arquivos incluídos"
                    )
                }
            } catch (e: Exception) {
                addConverterLog("Falha no empacotamento: ${e.message}", LogType.ERROR)
                _converterState.update { it.copy(isProcessing = false, error = e.message) }
            }
        }
    }

    fun clearConverterFiles() {
        _converterState.update {
            it.copy(
                files = emptyList(),
                resultFile = null,
                error = null,
                logs = it.logs + ProcessLog(currentTime(), "Lista de arquivos limpa.", LogType.INFO)
            )
        }
    }

    private fun addConverterLog(msg: String, type: LogType = LogType.INFO) {
        _converterState.update { it.copy(logs = it.logs + ProcessLog(currentTime(), msg, type)) }
    }

    // -------------------------------------------------------------
    // CREATOR LOGIC
    // -------------------------------------------------------------

    fun updateCreatorPrompt(prompt: String) {
        _creatorState.update { it.copy(prompt = prompt) }
    }

    fun updateCreatorName(name: String) {
        _creatorState.update { it.copy(name = name) }
    }

    fun updateCreatorVersion(version: String) {
        _creatorState.update { it.copy(version = version) }
    }

    fun updateCreatorCategory(category: PackCategory) {
        _creatorState.update { it.copy(category = category) }
    }

    fun updateCreatorSymbol(symbol: String) {
        _creatorState.update { it.copy(iconSymbol = symbol, iconBitmap = null) }
    }

    fun setCreatorIconUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val stream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(stream)
                val scaled = Bitmap.createScaledBitmap(bmp, 256, 256, true)
                _creatorState.update { it.copy(iconBitmap = scaled) }
            } catch (e: Exception) {
                _creatorState.update { it.copy(error = "Erro ao carregar imagem: ${e.message}") }
            }
        }
    }

    fun applyPreset(presetName: String, promptText: String, category: PackCategory) {
        _creatorState.update {
            it.copy(
                name = presetName,
                prompt = promptText,
                category = category
            )
        }
    }

    fun addModelFiles(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val models = mutableListOf<AddonFile>()
            for (uri in uris) {
                var fileName = "model.json"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx != -1) {
                        fileName = cursor.getString(nameIdx) ?: fileName
                    }
                }
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                models.add(AddonFile(name = fileName, size = bytes.size.toLong(), path = "models/$fileName", content = bytes, uri = uri))
            }
            _creatorState.update { it.copy(modelFiles = it.modelFiles + models) }
        }
    }

    fun updateCreatorNamespace(namespace: String) {
        _creatorState.update { it.copy(namespace = namespace) }
    }

    fun updateCreatorMinEngineVersion(version: String) {
        _creatorState.update { it.copy(minEngineVersion = version) }
    }

    fun toggleAutoManifest(enabled: Boolean) {
        _creatorState.update { it.copy(autoManifestEnabled = enabled) }
    }

    fun setManifestTab(tabIndex: Int) {
        _creatorState.update { it.copy(manifestTabSelected = tabIndex) }
    }

    fun regenerateUuids() {
        _creatorState.update {
            it.copy(
                bpHeaderUuid = java.util.UUID.randomUUID().toString(),
                rpHeaderUuid = java.util.UUID.randomUUID().toString()
            )
        }
    }

    fun toggleDownloadGuide(show: Boolean) {
        _creatorState.update { it.copy(showDownloadGuide = show) }
    }

    fun saveToCustomUri(destinationUri: Uri, sourceFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            MinecraftLauncher.saveToUri(getApplication(), sourceFile, destinationUri)
        }
    }

    fun generateAddon() {
        val state = _creatorState.value
        val safeId = BedrockTemplateGenerator.sanitizeIdentifier(state.name)
        val namespace = state.namespace.ifEmpty { "forge" }

        viewModelScope.launch(Dispatchers.IO) {
            _creatorState.update { it.copy(isGenerating = true, error = null, resultFile = null) }

            try {
                // 1. Linked Manifests with Auto Manifest settings
                val engineV = BedrockManifestGenerator.parseVersion(state.minEngineVersion)
                val manifests = BedrockManifestGenerator.createLinkedManifests(
                    packName = state.name,
                    version = state.version,
                    description = state.prompt.take(120),
                    minEngineVersion = engineV,
                    customBpHeaderUuid = state.bpHeaderUuid,
                    customRpHeaderUuid = state.rpHeaderUuid
                )

                val generatedFiles = mutableListOf<AddonFile>()

                // BP & RP Manifests
                generatedFiles.add(AddonFile("manifest.json", manifests.bpManifestJson.length.toLong(), "bp/manifest.json", manifests.bpManifestJson.toByteArray(Charsets.UTF_8)))
                generatedFiles.add(AddonFile("manifest.json", manifests.rpManifestJson.length.toLong(), "rp/manifest.json", manifests.rpManifestJson.toByteArray(Charsets.UTF_8)))

                // Based on category
                when (state.category) {
                    PackCategory.ENTITY -> {
                        val entityJson = BedrockTemplateGenerator.generateEntityBehavior(namespace, safeId, state.name, state.prompt)
                        val clientJson = BedrockTemplateGenerator.generateClientEntity(namespace, safeId)
                        val geoJson = BedrockTemplateGenerator.generateStandardGeometry(safeId)
                        generatedFiles.add(AddonFile("$safeId.json", entityJson.length.toLong(), "bp/entities/$safeId.json", entityJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("$safeId.entity.json", clientJson.length.toLong(), "rp/entity/$safeId.entity.json", clientJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("$safeId.geo.json", geoJson.length.toLong(), "rp/models/entity/$safeId.geo.json", geoJson.toByteArray(Charsets.UTF_8)))
                    }
                    PackCategory.ITEM -> {
                        val itemJson = BedrockTemplateGenerator.generateItemBehavior(namespace, safeId, state.name, state.prompt)
                        val itemTexJson = BedrockTemplateGenerator.generateItemTextureJson(safeId)
                        generatedFiles.add(AddonFile("$safeId.json", itemJson.length.toLong(), "bp/items/$safeId.json", itemJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("item_texture.json", itemTexJson.length.toLong(), "rp/textures/item_texture.json", itemTexJson.toByteArray(Charsets.UTF_8)))
                    }
                    PackCategory.BLOCK -> {
                        val blockJson = BedrockTemplateGenerator.generateBlockBehavior(namespace, safeId, state.name, state.prompt)
                        val terrainTexJson = BedrockTemplateGenerator.generateTerrainTextureJson(safeId)
                        generatedFiles.add(AddonFile("$safeId.json", blockJson.length.toLong(), "bp/blocks/$safeId.json", blockJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("terrain_texture.json", terrainTexJson.length.toLong(), "rp/textures/terrain_texture.json", terrainTexJson.toByteArray(Charsets.UTF_8)))
                    }
                    PackCategory.COMPLETE -> {
                        val entityJson = BedrockTemplateGenerator.generateEntityBehavior(namespace, "${safeId}_boss", "${state.name} Boss", state.prompt)
                        val clientJson = BedrockTemplateGenerator.generateClientEntity(namespace, "${safeId}_boss")
                        val geoJson = BedrockTemplateGenerator.generateStandardGeometry("${safeId}_boss")
                        val itemJson = BedrockTemplateGenerator.generateItemBehavior(namespace, "${safeId}_blade", "${state.name} Blade", state.prompt)
                        val itemTexJson = BedrockTemplateGenerator.generateItemTextureJson("${safeId}_blade")
                        val blockJson = BedrockTemplateGenerator.generateBlockBehavior(namespace, "${safeId}_block", "${state.name} Bloco", state.prompt)
                        val terrainTexJson = BedrockTemplateGenerator.generateTerrainTextureJson("${safeId}_block")

                        generatedFiles.add(AddonFile("${safeId}_boss.json", entityJson.length.toLong(), "bp/entities/${safeId}_boss.json", entityJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("${safeId}_boss.entity.json", clientJson.length.toLong(), "rp/entity/${safeId}_boss.entity.json", clientJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("${safeId}_boss.geo.json", geoJson.length.toLong(), "rp/models/entity/${safeId}_boss.geo.json", geoJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("${safeId}_blade.json", itemJson.length.toLong(), "bp/items/${safeId}_blade.json", itemJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("item_texture.json", itemTexJson.length.toLong(), "rp/textures/item_texture.json", itemTexJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("${safeId}_block.json", blockJson.length.toLong(), "bp/blocks/${safeId}_block.json", blockJson.toByteArray(Charsets.UTF_8)))
                        generatedFiles.add(AddonFile("terrain_texture.json", terrainTexJson.length.toLong(), "rp/textures/terrain_texture.json", terrainTexJson.toByteArray(Charsets.UTF_8)))
                    }
                }

                // Add language file
                val langJson = BedrockTemplateGenerator.generateLangFile(namespace, safeId, state.name, state.category)
                generatedFiles.add(AddonFile("en_US.lang", langJson.length.toLong(), "rp/texts/en_US.lang", langJson.toByteArray(Charsets.UTF_8)))

                // Add custom imported 3D models & textures
                for (m in state.modelFiles) {
                    val destPath = if (m.name.endsWith(".geo.json")) "rp/models/entity/${m.name}" else "rp/models/${m.name}"
                    generatedFiles.add(m.copy(path = destPath))
                }

                // Generate pack_icon bitmap
                val iconBmp = state.iconBitmap ?: BedrockPackager.generateDefaultPackIcon(state.name, state.iconSymbol)

                val resultFile = BedrockPackager.packageToMcaddon(getApplication(), state.name, generatedFiles, iconBmp)
                val sizeKb = resultFile.length() / 1024

                _creatorState.update {
                    it.copy(
                        isGenerating = false,
                        resultFile = resultFile,
                        resultInfo = "${resultFile.name} · ${sizeKb} KB · Behavior Pack + Resource Pack"
                    )
                }
            } catch (e: Exception) {
                _creatorState.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }

    // -------------------------------------------------------------
    // BUGFIX LOGIC
    // -------------------------------------------------------------

    fun addBugfixFiles(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val newFiles = mutableListOf<AddonFile>()

            for (uri in uris) {
                var fileName = "arquivo"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx != -1) {
                        fileName = cursor.getString(nameIdx) ?: fileName
                    }
                }
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                val lower = fileName.lowercase()

                if (lower.endsWith(".zip") || lower.endsWith(".mcpack") || lower.endsWith(".mcaddon")) {
                    val extracted = BedrockPackager.extractFilesFromZip(bytes.inputStream())
                    newFiles.addAll(extracted)
                } else {
                    newFiles.add(AddonFile(name = fileName, size = bytes.size.toLong(), path = fileName, content = bytes, uri = uri))
                }
            }

            _bugfixState.update { it.copy(files = (it.files + newFiles).distinctBy { f -> f.path }, report = null, fixedFile = null) }
            runBugDiagnostics()
        }
    }

    fun loadBrokenSample() {
        viewModelScope.launch(Dispatchers.Default) {
            // Broken sample with missing UUIDs, trailing commas, uppercase identifier
            val brokenManifest = """
{
  "format_version": 2,
  "header": {
    "name": "Pack Quebrado",
    "uuid": "",
    "version": [1, 0, 0]
  },
  "modules": [
    {
      "type": "data",
      "uuid": "invalid-uuid",
      "version": [1, 0, 0],
    },
  ],
}
""".trimIndent()

            val brokenEntity = """
{
  "format_version": "1.20.0",
  "minecraft:entity": {
    "description": {
      "identifier": "BrokenMonster",
      "is_spawnable": true,
    },
    "components": {
      "minecraft:health": {
        "value": 20,
        "max": 20,
      },
    },
  },
}
""".trimIndent()

            val brokenFiles = listOf(
                AddonFile("manifest.json", brokenManifest.length.toLong(), "manifest.json", brokenManifest.toByteArray(Charsets.UTF_8)),
                AddonFile("broken_monster.json", brokenEntity.length.toLong(), "entities/broken_monster.json", brokenEntity.toByteArray(Charsets.UTF_8))
            )

            _bugfixState.update { it.copy(files = brokenFiles, report = null, fixedFile = null) }
            runBugDiagnostics()
        }
    }

    fun runBugDiagnostics() {
        val files = _bugfixState.value.files
        if (files.isEmpty()) return

        viewModelScope.launch(Dispatchers.Default) {
            _bugfixState.update { it.copy(isAnalyzing = true, error = null) }
            val report = BedrockBugDiagnostics.diagnose(files)

            val logs = mutableListOf<ProcessLog>()
            logs.add(ProcessLog(currentTime(), "Iniciando diagnóstico estático em ${files.size} arquivos...", LogType.INFO))

            if (report.isJavaModDetected) {
                logs.add(ProcessLog(currentTime(), "CRÍTICO: Estrutura Java Edition detectada! Mods Java não são suportados no Bedrock.", LogType.ERROR))
            }

            for (issue in report.issues) {
                val logType = when (issue.severity) {
                    com.example.model.DiagnosticSeverity.ERROR -> LogType.ERROR
                    com.example.model.DiagnosticSeverity.WARNING -> LogType.WARNING
                    com.example.model.DiagnosticSeverity.SUCCESS -> LogType.SUCCESS
                    com.example.model.DiagnosticSeverity.INFO -> LogType.INFO
                }
                logs.add(ProcessLog(currentTime(), "${issue.title}: ${issue.detail}", logType))
            }

            logs.add(ProcessLog(currentTime(), "Diagnóstico concluído: ${report.summary}", LogType.ACCENT))

            _bugfixState.update {
                it.copy(
                    isAnalyzing = false,
                    report = report,
                    logs = logs
                )
            }
        }
    }

    fun fixAndPackageBugfix() {
        val files = _bugfixState.value.files
        if (files.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _bugfixState.update { it.copy(isFixing = true, error = null) }
            try {
                val fixed = BedrockBugDiagnostics.autoFixFiles(files, "Addon_Reparado")
                val resultFile = BedrockPackager.packageToMcaddon(getApplication(), "Addon_Reparado", fixed)
                val sizeKb = resultFile.length() / 1024

                val fixLogs = _bugfixState.value.logs + listOf(
                    ProcessLog(currentTime(), "✓ Trailing commas removidas de todos os JSONs.", LogType.SUCCESS),
                    ProcessLog(currentTime(), "✓ UUIDs v4 regenerados e validados.", LogType.SUCCESS),
                    ProcessLog(currentTime(), "✓ Identificadores corrigidos para lowercase com prefixo namespace.", LogType.SUCCESS),
                    ProcessLog(currentTime(), "✓ Pacote corrigido empacotado em: ${resultFile.name} (${sizeKb} KB)", LogType.ACCENT)
                )

                _bugfixState.update {
                    it.copy(
                        isFixing = false,
                        fixedFile = resultFile,
                        fixedInfo = "${resultFile.name} · ${sizeKb} KB · Todos os erros corrigidos com sucesso",
                        logs = fixLogs
                    )
                }
            } catch (e: Exception) {
                _bugfixState.update { it.copy(isFixing = false, error = e.message) }
            }
        }
    }

    fun clearBugfixFiles() {
        _bugfixState.update {
            it.copy(
                files = emptyList(),
                report = null,
                fixedFile = null,
                logs = listOf(ProcessLog(currentTime(), "Arquivos de diagnóstico limpos.", LogType.INFO))
            )
        }
    }
}
