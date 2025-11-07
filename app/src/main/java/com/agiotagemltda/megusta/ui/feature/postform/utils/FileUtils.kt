package com.agiotagemltda.megusta.ui.feature.postform.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID


fun copyImageToInternalStorage(
    context: Context,
    uri: Uri,
    fileName: String = UUID.randomUUID().toString()
): String?{
    return try {
        context.contentResolver.openInputStream(uri)?.use{ input ->
            val file = File(context.filesDir, "images/$fileName.jpg")
            file.parentFile?.mkdirs()
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            file.absolutePath
        }
    } catch (e: Exception){
        e.printStackTrace()
        null
    }
}