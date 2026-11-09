package com.example.model

import android.net.Uri

enum class PackCategory(val displayName: String, val icon: String) {
    COMPLETE("Addon Completo", "📦"),
    ENTITY("Criatura / Mob", "👾"),
    ITEM("Item / Arma", "⚔️"),
    BLOCK("Bloco Especial", "🧱")
}

data class AddonFile(
    val name: String,
    val size: Long,
    val path: String = name,
    val content: ByteArray = ByteArray(0),
    val isDirectory: Boolean = false,
    val uri: Uri? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AddonFile
        return name == other.name && path == other.path && size == other.size
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + size.hashCode()
        return result
    }
}

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    SUCCESS,
    INFO
}

data class DiagnosticIssue(
    val severity: DiagnosticSeverity,
    val title: String,
    val detail: String,
    val fileName: String? = null,
    val autoFixable: Boolean = true
)

data class DiagnosticReport(
    val issues: List<DiagnosticIssue> = emptyList(),
    val totalFilesScanned: Int = 0,
    val errorsCount: Int = 0,
    val warningsCount: Int = 0,
    val isJavaModDetected: Boolean = false,
    val manifestFound: Boolean = false,
    val summary: String = ""
)

data class ProcessLog(
    val timestamp: String,
    val message: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    ACCENT
}
