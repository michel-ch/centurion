package com.century.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ExerciseImageHelper {

    // Only allow alphanumeric and underscores to prevent path traversal
    private val SAFE_ID_REGEX = Regex("^[a-zA-Z0-9_]+$")
    private const val MAX_IMAGE_DIMENSION = 4096 // reject absurdly large images

    private fun sanitizeId(illustrationId: String): String? {
        if (illustrationId.isBlank()) return null
        return if (SAFE_ID_REGEX.matches(illustrationId)) illustrationId else null
    }

    fun getExerciseImageDir(context: Context): File {
        val dir = File(context.filesDir, "exercise_images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveCustomImage(context: Context, illustrationId: String, sourceUri: Uri): String? {
        val safeId = sanitizeId(illustrationId) ?: return null
        return try {
            // First pass: read bounds only to guard against OOM
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            if (bounds.outWidth > MAX_IMAGE_DIMENSION || bounds.outHeight > MAX_IMAGE_DIMENSION) return null

            // Second pass: decode with appropriate sub-sampling
            val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, 800)
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            } ?: return null

            val scaled = scaleBitmap(bitmap, 800)
            val dir = getExerciseImageDir(context)
            val file = File(dir, "$safeId.webp")

            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out)
            }

            // Avoid double-recycle when scaleBitmap returns the original object unchanged
            if (scaled !== bitmap) bitmap.recycle()
            scaled.recycle()

            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun deleteCustomImage(context: Context, illustrationId: String) {
        val safeId = sanitizeId(illustrationId) ?: return
        val file = File(getExerciseImageDir(context), "$safeId.webp")
        if (file.exists()) file.delete()
    }

    fun getCustomImageFile(context: Context, illustrationId: String): File? {
        val safeId = sanitizeId(illustrationId) ?: return null
        val file = File(getExerciseImageDir(context), "$safeId.webp")
        return if (file.exists()) file else null
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int): Bitmap {
        if (bitmap.width <= maxWidth) return bitmap
        val ratio = maxWidth.toFloat() / bitmap.width
        val newHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
    }

    private fun calculateSampleSize(width: Int, height: Int, targetWidth: Int): Int {
        var sampleSize = 1
        while (width / (sampleSize * 2) >= targetWidth && height / (sampleSize * 2) > 0) {
            sampleSize *= 2
        }
        return sampleSize
    }

    fun getDrawableResId(context: Context, illustrationId: String): Int {
        val safeId = sanitizeId(illustrationId) ?: return 0
        val resName = "exercise_$safeId"
        return context.resources.getIdentifier(resName, "drawable", context.packageName)
    }
}
