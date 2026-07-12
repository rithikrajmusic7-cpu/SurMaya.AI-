package com.example.core.audio

import android.content.Context
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class SurmayaProjectPackager(private val context: Context) {

    /**
     * Packs the project state JSON and loaded wav assets into a singular .surmaya project archive
     */
    fun packProject(
        projectId: String,
        projectJson: String,
        audioAssets: List<File>,
        outputFile: File
    ): Result<File> {
        return try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
                // 1. Write the metadata JSON
                val jsonEntry = ZipEntry("project.json")
                zos.putNextEntry(jsonEntry)
                zos.write(projectJson.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 2. Write individual audio sample files
                for (file in audioAssets) {
                    if (file.exists() && file.isFile) {
                        val assetEntry = ZipEntry("assets/${file.name}")
                        zos.putNextEntry(assetEntry)
                        
                        FileInputStream(file).use { fis ->
                            val buffer = ByteArray(4096)
                            var readBytes: Int
                            while (fis.read(buffer).also { readBytes = it } != -1) {
                                zos.write(buffer, 0, readBytes)
                            }
                        }
                        zos.closeEntry()
                    }
                }
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unpacks a .surmaya package into a temporary directory and extracts the metadata json and audio paths
     */
    fun unpackProject(
        packageFile: File,
        targetDir: File
    ): Result<UnpackedProject> {
        return try {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            var projectJson = ""
            val extractedAssets = mutableListOf<File>()

            ZipInputStream(FileInputStream(packageFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val file = File(targetDir, entry.name)
                    
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        // Ensure parent directories exist
                        file.parentFile?.mkdirs()
                        
                        if (entry.name == "project.json") {
                            projectJson = zis.bufferedReader(Charsets.UTF_8).readText()
                        } else {
                            FileOutputStream(file).use { fos ->
                                val buffer = ByteArray(4096)
                                var read: Int
                                while (zis.read(buffer).also { read = it } != -1) {
                                    fos.write(buffer, 0, read)
                                }
                            }
                            extractedAssets.add(file)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            
            if (projectJson.isEmpty()) {
                Result.failure(Exception("Invalid .surmaya package: project.json metadata is missing."))
            } else {
                Result.success(UnpackedProject(projectJson, extractedAssets))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class UnpackedProject(
        val projectJson: String,
        val extractedFiles: List<File>
    )
}
