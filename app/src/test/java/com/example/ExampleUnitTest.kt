package com.example

import com.example.engine.BedrockBugDiagnostics
import com.example.engine.BedrockManifestGenerator
import com.example.engine.BedrockTemplateGenerator
import com.example.model.AddonFile
import com.example.model.DiagnosticSeverity
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testManifestGenerator_createsValidLinkedManifests() {
        val manifests = BedrockManifestGenerator.createLinkedManifests("TestAddon", "1.2.3")
        assertNotNull(manifests.bpManifestJson)
        assertNotNull(manifests.rpManifestJson)

        val bpJson = JSONObject(manifests.bpManifestJson)
        assertEquals(2, bpJson.getInt("format_version"))
        val header = bpJson.getJSONObject("header")
        assertEquals("TestAddon BP", header.getString("name"))

        val rpJson = JSONObject(manifests.rpManifestJson)
        assertEquals(2, rpJson.getInt("format_version"))

        // Check BP dependency points to RP header UUID
        val deps = bpJson.getJSONArray("dependencies")
        val depUuid = deps.getJSONObject(0).getString("uuid")
        assertEquals(manifests.rpHeaderUuid, depUuid)
    }

    @Test
    fun testTrailingCommaDetection_andAutoFix() {
        val brokenJson = """
        {
          "format_version": "1.20.0",
          "minecraft:item": {
            "description": {
              "identifier": "test:crystal",
            },
          },
        }
        """.trimIndent()

        val files = listOf(
            AddonFile("item.json", brokenJson.length.toLong(), "items/item.json", brokenJson.toByteArray(Charsets.UTF_8))
        )

        val diag = BedrockBugDiagnostics.diagnose(files)
        assertTrue(diag.issues.any { it.title.contains("Vírgula") })

        val fixed = BedrockBugDiagnostics.autoFixFiles(files, "Reparado")
        val fixedText = String(fixed.first().content, Charsets.UTF_8)

        // Must parse as valid JSON now
        val parsed = JSONObject(fixedText)
        assertNotNull(parsed)
    }

    @Test
    fun testJavaModDetection() {
        val files = listOf(
            AddonFile("coolmod.jar", 1024L, "coolmod.jar", ByteArray(10))
        )
        val diag = BedrockBugDiagnostics.diagnose(files)
        assertTrue(diag.isJavaModDetected)
        assertTrue(diag.issues.any { it.severity == DiagnosticSeverity.ERROR })
    }

    @Test
    fun testTemplateGenerator() {
        val entity = BedrockTemplateGenerator.generateEntityBehavior("forge", "mystic_mob", "Mystic Mob", "mob forte")
        assertTrue(entity.contains("\"identifier\": \"forge:mystic_mob\""))
        assertTrue(entity.contains("\"minecraft:health\""))

        val item = BedrockTemplateGenerator.generateItemBehavior("forge", "sword_void", "Void Sword", "espada com muito dano")
        assertTrue(item.contains("\"minecraft:damage\""))
    }
}
