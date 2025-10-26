package com.example.snapitout.utils

import android.graphics.*
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random
import android.content.Context
import androidx.core.content.ContextCompat


object CollageUtils {

    /** 🎨 1. Clean scrapbook-style collage (random but well-spaced) */
    fun createScrapbookCollage(bitmaps: List<Bitmap>): Bitmap {
        if (bitmaps.isEmpty()) throw IllegalArgumentException("No images provided")

        val collageWidth = 1080
        val collageHeight = 1080
        val collageBitmap = Bitmap.createBitmap(collageWidth, collageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(collageBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background color
        canvas.drawColor(Color.WHITE)

        val random = Random(System.currentTimeMillis())
        val maxImageSize = collageWidth / 3

        bitmaps.forEach { original ->
            val scale = (0.6f + random.nextFloat() * 0.5f)
            val newWidth = (maxImageSize * scale).toInt().coerceAtLeast(100)
            val newHeight = (original.height * newWidth / original.width).coerceAtLeast(100)
            val resized = Bitmap.createScaledBitmap(original, newWidth, newHeight, true)

            val x = random.nextInt(0, (collageWidth - newWidth).coerceAtLeast(1))
            val y = random.nextInt(0, (collageHeight - newHeight).coerceAtLeast(1))

            val rotation = random.nextInt(-25, 25).toFloat()

            canvas.save()
            canvas.rotate(rotation, x + newWidth / 2f, y + newHeight / 2f)
            canvas.drawBitmap(resized, x.toFloat(), y.toFloat(), paint)
            canvas.restore()
        }

        return collageBitmap
    }

    /** 🧩 2. Clean and balanced 2×2 collage with spacing */
    fun createMegaCollage(bitmaps: List<Bitmap>): Bitmap? {
        if (bitmaps.isEmpty()) return null

        val cols = 2
        val rows = 2
        val frameSize = 25
        val finalSize = 900
        val cellSize = (finalSize - frameSize * (cols + 1)) / cols

        val result = Bitmap.createBitmap(finalSize, finalSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        bitmaps.take(4).forEachIndexed { i, bmp ->
            val row = i / cols
            val col = i % cols
            val left = frameSize + col * (cellSize + frameSize)
            val top = frameSize + row * (cellSize + frameSize)

            val scaled = Bitmap.createScaledBitmap(bmp, cellSize, cellSize, true)
            val angle = Random.nextInt(-5, 5)

            canvas.save()
            canvas.rotate(angle.toFloat(), left + cellSize / 2f, top + cellSize / 2f)

            // Drop shadow
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                setShadowLayer(8f, 4f, 4f, Color.argb(80, 0, 0, 0))
            }
            canvas.drawRect(
                left.toFloat(), top.toFloat(),
                (left + cellSize).toFloat(), (top + cellSize).toFloat(), shadowPaint
            )

            // Image
            canvas.drawBitmap(scaled, left.toFloat(), top.toFloat(), paint)

            // Frame
            val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 8f
                color = Color.WHITE
            }
            canvas.drawRect(
                left.toFloat(), top.toFloat(),
                (left + cellSize).toFloat(), (top + cellSize).toFloat(), framePaint
            )

            canvas.restore()
        }

        return result
    }

    /** ❤️ 3. Shape collage (auto-fills shape depending on photo count) */
    fun createShapeCollage(bitmaps: List<Bitmap>, mask: Bitmap): Bitmap? {
        if (bitmaps.isEmpty()) return null

        val collageWidth = mask.width
        val collageHeight = mask.height
        val collage = Bitmap.createBitmap(collageWidth, collageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(collage)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Determine how many grid cells to fill based on shape size
        val gridSize = 15 // higher = smaller photos, more fill precision
        val cellWidth = collageWidth / gridSize
        val cellHeight = collageHeight / gridSize

        // 🔁 Duplicate images if needed to fill shape evenly
        val neededCount = gridSize * gridSize
        val repeatedList = mutableListOf<Bitmap>()
        while (repeatedList.size < neededCount) {
            repeatedList.addAll(bitmaps)
        }

        // Trim the excess
        val filledImages = repeatedList.take(neededCount)

        var index = 0
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val x = j * cellWidth
                val y = i * cellHeight

                // Check if cell center is inside the shape (white area)
                val centerX = (x + cellWidth / 2).coerceAtMost(collageWidth - 1)
                val centerY = (y + cellHeight / 2).coerceAtMost(collageHeight - 1)
                if (Color.red(mask.getPixel(centerX, centerY)) > 200) {
                    val bmp = filledImages[index % filledImages.size]
                    val scaled = Bitmap.createScaledBitmap(bmp, cellWidth, cellHeight, true)
                    canvas.drawBitmap(scaled, x.toFloat(), y.toFloat(), paint)
                    scaled.recycle()
                    index++
                }
            }
        }

        // Apply the mask to keep only the shape visible
        val result = Bitmap.createBitmap(collageWidth, collageHeight, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(result)
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        maskCanvas.drawBitmap(collage, 0f, 0f, null)
        maskCanvas.drawBitmap(mask, 0f, 0f, maskPaint)

        return result
    }

    fun getBitmapFromDrawable(context: Context, drawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)!!
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }




    /** 💾 4. Save any Bitmap to album folder */
    fun saveBitmapToAlbum(bitmap: Bitmap, folder: File, prefix: String = "SnapIt_Collage_"): File? {
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, "$prefix${System.currentTimeMillis()}.jpg")
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}