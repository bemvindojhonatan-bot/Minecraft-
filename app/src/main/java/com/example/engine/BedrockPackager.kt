package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import com.example.model.AddonFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BedrockPackager {

    fun extractFilesFromZip(inputStream: InputStream): List<AddonFile> {
        val files = mutableListOf<AddonFile>()
        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val name = entry.name.substringAfterLast('/')
                    val path = entry.name
                    val baos = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                        baos.write(buffer, 0, len)
                    }
                    val bytes = baos.toByteArray()
                    files.add(
                        AddonFile(
                            name = name,
                            size = bytes.size.toLong(),
                            path = path,
                            content = bytes,
                            isDirectory = false
                        )
                    )
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return files
    }

    fun packageToMcaddon(
        context: Context,
        packName: String,
        files: List<AddonFile>,
        iconBitmap: Bitmap? = null
    ): File {
        val safeName = packName.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_").ifEmpty { "BedrockAddon" }
        val outputDir = File(context.cacheDir, "packs").apply { mkdirs() }
        val outputFile = File(outputDir, "$safeName.mcaddon")

        val iconBytes = (iconBitmap ?: generateDefaultPackIcon(safeName)).let { bmp ->
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }

        FileOutputStream(outputFile).use { fos ->
            ZipOutputStream(fos).use { zos ->
                // Check if files are already structured into BP/RP folders
                val hasBpDir = files.any { it.path.startsWith("bp/") || it.path.startsWith("behavior_pack/") }
                val hasRpDir = files.any { it.path.startsWith("rp/") || it.path.startsWith("resource_pack/") }

                val writtenPaths = mutableSetOf<String>()

                for (file in files) {
                    val targetPath = if (hasBpDir || hasRpDir || file.path.contains("/")) {
                        file.path
                    } else {
                        // Place manifest/icon at root or in BP
                        if (file.name == "pack_icon.png") "pack_icon.png" else "bp/${file.name}"
                    }

                    if (writtenPaths.add(targetPath)) {
                        val entry = ZipEntry(targetPath)
                        zos.putNextEntry(entry)
                        zos.write(file.content)
                        zos.closeEntry()
                    }
                }

                // Ensure pack_icon.png is present at root and in bp/rp
                val iconPaths = listOf("pack_icon.png", "bp/pack_icon.png", "rp/pack_icon.png")
                for (p in iconPaths) {
                    if (writtenPaths.add(p)) {
                        zos.putNextEntry(ZipEntry(p))
                        zos.write(iconBytes)
                        zos.closeEntry()
                    }
                }
            }
        }

        return outputFile
    }

    fun generateDefaultPackIcon(title: String, symbol: String = "⬡"): Bitmap {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark Bedrock gradient / forge block
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = Color.rgb(18, 16, 30)
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

        // Inner glowing border
        paint.color = Color.rgb(124, 58, 237)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawRoundRect(RectF(8f, 8f, size - 8f, size - 8f), 16f, 16f, paint)

        // Bedrock block pattern accents
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(40, 168, 85, 247)
        canvas.drawRect(24f, 24f, size - 24f, size - 24f, paint)

        // Center Symbol
        paint.color = Color.rgb(245, 240, 255)
        paint.textSize = 72f
        paint.textAlign = Paint.Align.CENTER
        val textBounds = Rect()
        paint.getTextBounds(symbol, 0, symbol.length, textBounds)
        canvas.drawText(symbol, size / 2f, size / 2f + 16f, paint)

        // Pack Title in bottom
        val shortName = if (title.length > 12) title.take(11) + "…" else title
        paint.textSize = 18f
        paint.color = Color.rgb(94, 234, 212)
        paint.isFakeBoldText = true
        canvas.drawText(shortName.uppercase(), size / 2f, size - 32f, paint)

        return bitmap
    }
}
