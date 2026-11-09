package com.example.engine

import com.example.model.AddonFile
import com.example.model.DiagnosticIssue
import com.example.model.DiagnosticReport
import com.example.model.DiagnosticSeverity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object BedrockBugDiagnostics {

    private val UUID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private val TRAILING_COMMA_REGEX = Regex(",\\s*([}\\]])")

    fun diagnose(files: List<AddonFile>): DiagnosticReport {
        val issues = mutableListOf<DiagnosticIssue>()
        var errorsCount = 0
        var warningsCount = 0
        var isJavaMod = false
        var manifestFound = false

        // 1. Check for Java Mod files
        for (f in files) {
            val lower = f.name.lowercase()
            if (lower.endsWith(".jar") || f.path.contains("fabric.mod.json") ||
                f.path.contains("META-INF") || f.path.contains("mcmod.info") ||
                f.path.contains("pack.mcmeta")
            ) {
                isJavaMod = true
                issues.add(
                    DiagnosticIssue(
                        severity = DiagnosticSeverity.ERROR,
                        title = "Mod Java Detectado (Incompatível)",
                        detail = "O arquivo '${f.name}' é um mod de Minecraft Java Edition (Forge/Fabric). Mods Java não são compatíveis diretamente com Bedrock e não podem ser executados.",
                        fileName = f.name,
                        autoFixable = false
                    )
                )
                errorsCount++
            }
        }

        // 2. Scan for manifests
        val manifestFiles = files.filter { it.name.equals("manifest.json", ignoreCase = true) }
        if (manifestFiles.isNotEmpty()) {
            manifestFound = true
            for (mf in manifestFiles) {
                checkManifest(mf, issues)
            }
        } else if (!isJavaMod && files.isNotEmpty()) {
            issues.add(
                DiagnosticIssue(
                    severity = DiagnosticSeverity.ERROR,
                    title = "manifest.json Ausente",
                    detail = "Nenhum arquivo 'manifest.json' foi encontrado na raiz do pacote. O Minecraft Bedrock precisa deste arquivo para registrar o addon.",
                    autoFixable = true
                )
            )
            errorsCount++
        }

        // 3. Scan JSON syntax & trailing commas across all json files
        for (f in files) {
            val lower = f.name.lowercase()
            if (lower.endsWith(".json") || lower.endsWith(".geo.json")) {
                checkJsonFile(f, issues)
            }
        }

        // 4. Check folder structure
        val hasBp = files.any { it.path.contains("entities/") || it.path.contains("items/") || it.path.contains("blocks/") || it.path.contains("loot_tables/") }
        val hasRp = files.any { it.path.contains("textures/") || it.path.contains("models/") || it.path.contains("attachables/") || it.path.contains("sounds/") }

        if (hasBp && hasRp && manifestFiles.size == 1) {
            issues.add(
                DiagnosticIssue(
                    severity = DiagnosticSeverity.WARNING,
                    title = "Mistura de BP e RP em único manifest",
                    detail = "O pacote possui componentes de comportamento e recursos juntos. Para Minecraft Bedrock moderno, recomenda-se separar em duas pastas com seus respectivos manifestos dentro de um .mcaddon.",
                    autoFixable = true
                )
            )
            warningsCount++
        }

        // Summarize
        issues.forEach {
            if (it.severity == DiagnosticSeverity.ERROR) errorsCount++
            if (it.severity == DiagnosticSeverity.WARNING) warningsCount++
        }

        val summary = when {
            isJavaMod -> "Bloqueado: Mod Java detectado. Apenas pacotes Bedrock são aceitos."
            errorsCount > 0 -> "Encontrados $errorsCount erro(s) e $warningsCount aviso(s). Correção recomendada."
            warningsCount > 0 -> "Estrutura válida com $warningsCount aviso(s). Pacote pronto para correção opcional."
            else -> "Nenhum problema detectado. O pacote está pronto e segue os padrões Bedrock."
        }

        return DiagnosticReport(
            issues = issues,
            totalFilesScanned = files.size,
            errorsCount = errorsCount,
            warningsCount = warningsCount,
            isJavaModDetected = isJavaMod,
            manifestFound = manifestFound,
            summary = summary
        )
    }

    private fun checkManifest(file: AddonFile, issues: MutableList<DiagnosticIssue>) {
        val text = String(file.content, Charsets.UTF_8)
        try {
            val json = JSONObject(text)
            val header = json.optJSONObject("header")
            if (header == null) {
                issues.add(
                    DiagnosticIssue(
                        severity = DiagnosticSeverity.ERROR,
                        title = "Manifest: Bloco 'header' ausente",
                        detail = "O manifest.json não possui a chave 'header' obrigatória.",
                        fileName = file.path
                    )
                )
            } else {
                val uuid = header.optString("uuid")
                if (uuid.isNullOrEmpty() || !UUID_REGEX.matches(uuid)) {
                    issues.add(
                        DiagnosticIssue(
                            severity = DiagnosticSeverity.ERROR,
                            title = "Manifest: UUID do header inválido",
                            detail = "O UUID '$uuid' no header é inválido ou ausente. É necessário um UUID v4 no formato 8-4-4-4-12.",
                            fileName = file.path
                        )
                    )
                }

                val version = header.optJSONArray("version")
                if (version == null || version.length() < 3) {
                    issues.add(
                        DiagnosticIssue(
                            severity = DiagnosticSeverity.WARNING,
                            title = "Manifest: Versão fora do padrão",
                            detail = "O campo 'version' deve ser um array com 3 inteiros: [1, 0, 0].",
                            fileName = file.path
                        )
                    )
                }
            }

            val modules = json.optJSONArray("modules")
            if (modules == null || modules.length() == 0) {
                issues.add(
                    DiagnosticIssue(
                        severity = DiagnosticSeverity.ERROR,
                        title = "Manifest: Bloco 'modules' vazio ou ausente",
                        detail = "O manifest precisa declarar pelo menos um módulo ('data' para BP ou 'resources' para RP).",
                        fileName = file.path
                    )
                )
            } else {
                for (i in 0 until modules.length()) {
                    val mod = modules.optJSONObject(i)
                    val modUuid = mod?.optString("uuid")
                    if (modUuid.isNullOrEmpty() || !UUID_REGEX.matches(modUuid)) {
                        issues.add(
                            DiagnosticIssue(
                                severity = DiagnosticSeverity.ERROR,
                                title = "Manifest: UUID de módulo inválido",
                                detail = "O módulo #${i + 1} possui um UUID inválido ou vazio.",
                                fileName = file.path
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            issues.add(
                DiagnosticIssue(
                    severity = DiagnosticSeverity.ERROR,
                    title = "Manifest: Erro de sintaxe JSON",
                    detail = "Não foi possível analisar o manifest.json: ${e.message}",
                    fileName = file.path
                )
            )
        }
    }

    private fun checkJsonFile(file: AddonFile, issues: MutableList<DiagnosticIssue>) {
        val text = String(file.content, Charsets.UTF_8)
        if (TRAILING_COMMA_REGEX.containsMatchIn(text)) {
            issues.add(
                DiagnosticIssue(
                    severity = DiagnosticSeverity.ERROR,
                    title = "Vírgula sobrando (Trailing Comma)",
                    detail = "O arquivo '${file.name}' possui vírgulas extras antes de fechar chaves/colchetes. O parser do Minecraft Bedrock recusa arquivos com vírgulas sobrando.",
                    fileName = file.path
                )
            )
        }

        // Identifier check
        if (text.contains("\"identifier\"")) {
            val idMatch = Regex("\"identifier\"\\s*:\\s*\"([^\"]+)\"").find(text)
            if (idMatch != null) {
                val identifier = idMatch.groupValues[1]
                if (!identifier.contains(":")) {
                    issues.add(
                        DiagnosticIssue(
                            severity = DiagnosticSeverity.WARNING,
                            title = "Identificador sem namespace",
                            detail = "O identificador '$identifier' não possui prefixo/namespace (ex.: 'custom:$identifier'). Pode colidir com itens vanilla do Minecraft.",
                            fileName = file.path
                        )
                    )
                }
                if (identifier.any { it.isUpperCase() }) {
                    issues.add(
                        DiagnosticIssue(
                            severity = DiagnosticSeverity.ERROR,
                            title = "Letras maiúsculas no identificador",
                            detail = "O identificador '$identifier' contém letras maiúsculas. O Minecraft Bedrock exige apenas letras minúsculas, números e underlines.",
                            fileName = file.path
                        )
                    )
                }
            }
        }
    }

    fun sanitizeJsonText(rawText: String): String {
        var clean = rawText
        // Remove trailing commas repeatedly until stable
        var prev = ""
        while (prev != clean) {
            prev = clean
            clean = TRAILING_COMMA_REGEX.replace(clean) { matchResult ->
                matchResult.groupValues[1]
            }
        }
        return clean
    }

    fun autoFixFiles(files: List<AddonFile>, packName: String = "Corrigido_Addon"): List<AddonFile> {
        val fixedList = mutableListOf<AddonFile>()
        var foundManifest = false

        for (f in files) {
            val lower = f.name.lowercase()
            if (lower == "manifest.json") {
                foundManifest = true
                val fixedManifest = fixManifestContent(String(f.content, Charsets.UTF_8), packName)
                fixedList.add(f.copy(content = fixedManifest.toByteArray(Charsets.UTF_8)))
            } else if (lower.endsWith(".json") || lower.endsWith(".geo.json")) {
                var contentStr = sanitizeJsonText(String(f.content, Charsets.UTF_8))
                // Lowercase uppercase identifiers
                contentStr = Regex("\"identifier\"\\s*:\\s*\"([^\"]+)\"").replace(contentStr) { match ->
                    val id = match.groupValues[1]
                    val lowerId = id.lowercase()
                    val namespaced = if (!lowerId.contains(":")) "custom:$lowerId" else lowerId
                    "\"identifier\": \"$namespaced\""
                }
                fixedList.add(f.copy(content = contentStr.toByteArray(Charsets.UTF_8)))
            } else {
                fixedList.add(f)
            }
        }

        if (!foundManifest) {
            // Generate a fresh pair of linked manifests or standard manifest
            val manifest = BedrockManifestGenerator.createLinkedManifests(packName)
            fixedList.add(
                AddonFile(
                    name = "manifest.json",
                    size = manifest.bpManifestJson.length.toLong(),
                    path = "manifest.json",
                    content = manifest.bpManifestJson.toByteArray(Charsets.UTF_8)
                )
            )
        }

        return fixedList
    }

    private fun fixManifestContent(rawText: String, packName: String): String {
        try {
            val clean = sanitizeJsonText(rawText)
            val json = JSONObject(clean)
            json.put("format_version", 2)

            var header = json.optJSONObject("header")
            if (header == null) {
                header = JSONObject()
                json.put("header", header)
            }
            if (!header.has("name") || header.optString("name").isEmpty()) {
                header.put("name", packName)
            }
            if (!header.has("description")) {
                header.put("description", "Corrigido por Bedrock Forge Studio")
            }
            val curUuid = header.optString("uuid")
            if (curUuid.isNullOrEmpty() || !UUID_REGEX.matches(curUuid)) {
                header.put("uuid", UUID.randomUUID().toString())
            }
            if (!header.has("version")) {
                val verArr = JSONArray().apply { put(1); put(0); put(0) }
                header.put("version", verArr)
            }
            if (!header.has("min_engine_version")) {
                val engineArr = JSONArray().apply { put(1); put(20); put(0) }
                header.put("min_engine_version", engineArr)
            }

            var modules = json.optJSONArray("modules")
            if (modules == null || modules.length() == 0) {
                modules = JSONArray()
                val modObj = JSONObject().apply {
                    put("type", "data")
                    put("uuid", UUID.randomUUID().toString())
                    put("version", JSONArray().apply { put(1); put(0); put(0) })
                }
                modules.put(modObj)
                json.put("modules", modules)
            } else {
                for (i in 0 until modules.length()) {
                    val m = modules.getJSONObject(i)
                    val mUuid = m.optString("uuid")
                    if (mUuid.isNullOrEmpty() || !UUID_REGEX.matches(mUuid)) {
                        m.put("uuid", UUID.randomUUID().toString())
                    }
                }
            }

            return json.toString(2)
        } catch (e: Exception) {
            // Fallback to fresh generated manifest
            return BedrockManifestGenerator.createLinkedManifests(packName).bpManifestJson
        }
    }
}
