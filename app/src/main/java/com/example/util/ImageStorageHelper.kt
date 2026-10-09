package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val imagesDir = File(context.filesDir, "footwear_images").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "shoe_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destinationFile = File(imagesDir, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destinationFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(destinationFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
