package com.agiotagemltda.megusta.ui.feature.home.utils

import java.io.*
import java.util.zip.*

fun zipProject(jsonContent: String, imageFolder: File, zipOutputStream: OutputStream) {
    ZipOutputStream(BufferedOutputStream(zipOutputStream)).use { zos ->
        // 1. Adicionar o JSON ao ZIP
        val jsonEntry = ZipEntry("backup.json")
        zos.putNextEntry(jsonEntry)
        zos.write(jsonContent.toByteArray())
        zos.closeEntry()

        // 2. Adicionar as imagens ao ZIP
        if (imageFolder.exists() && imageFolder.isDirectory) {
            imageFolder.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val entry = ZipEntry("images/${file.name}")
                    zos.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }
}