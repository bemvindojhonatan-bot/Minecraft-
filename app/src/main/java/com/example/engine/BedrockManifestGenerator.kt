package com.example.engine

import java.util.UUID

object BedrockManifestGenerator {

    data class ManifestPair(
        val bpManifestJson: String,
        val rpManifestJson: String,
        val bpHeaderUuid: String,
        val rpHeaderUuid: String
    )

    fun createLinkedManifests(
        packName: String,
        version: String = "1.0.0",
        description: String = "Criado com Bedrock Forge Studio",
        minEngineVersion: List<Int> = listOf(1, 20, 0),
        customBpHeaderUuid: String? = null,
        customRpHeaderUuid: String? = null
    ): ManifestPair {
        val verList = parseVersion(version)
        val bpHeaderUuid = customBpHeaderUuid ?: UUID.randomUUID().toString()
        val bpModuleUuid = UUID.randomUUID().toString()
        val rpHeaderUuid = customRpHeaderUuid ?: UUID.randomUUID().toString()
        val rpModuleUuid = UUID.randomUUID().toString()

        val engineV = if (minEngineVersion.size >= 3) minEngineVersion else listOf(1, 20, 0)

        val bpJson = """
{
  "format_version": 2,
  "header": {
    "name": "$packName BP",
    "description": "$description",
    "uuid": "$bpHeaderUuid",
    "version": [${verList[0]}, ${verList[1]}, ${verList[2]}],
    "min_engine_version": [${engineV[0]}, ${engineV[1]}, ${engineV[2]}]
  },
  "modules": [
    {
      "type": "data",
      "uuid": "$bpModuleUuid",
      "version": [${verList[0]}, ${verList[1]}, ${verList[2]}]
    }
  ],
  "dependencies": [
    {
      "uuid": "$rpHeaderUuid",
      "version": [${verList[0]}, ${verList[1]}, ${verList[2]}]
    }
  ]
}
""".trimIndent()

        val rpJson = """
{
  "format_version": 2,
  "header": {
    "name": "$packName RP",
    "description": "$description",
    "uuid": "$rpHeaderUuid",
    "version": [${verList[0]}, ${verList[1]}, ${verList[2]}],
    "min_engine_version": [${engineV[0]}, ${engineV[1]}, ${engineV[2]}]
  },
  "modules": [
    {
      "type": "resources",
      "uuid": "$rpModuleUuid",
      "version": [${verList[0]}, ${verList[1]}, ${verList[2]}]
    }
  ]
}
""".trimIndent()

        return ManifestPair(bpJson, rpJson, bpHeaderUuid, rpHeaderUuid)
    }

    fun parseVersion(versionStr: String): List<Int> {
        val parts = versionStr.split(".").mapNotNull { it.trim().toIntOrNull() }
        return when {
            parts.size >= 3 -> parts.take(3)
            parts.size == 2 -> listOf(parts[0], parts[1], 0)
            parts.size == 1 -> listOf(parts[0], 0, 0)
            else -> listOf(1, 0, 0)
        }
    }
}
