package kr.sjh.setting.screen

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object AvatarImageCompressor {
    const val TARGET_SIZE = 512
    const val TARGET_FILE_NAME = "avatar.jpg"
    const val JPEG_QUALITY = 80

    fun compress(contentResolver: ContentResolver, uri: Uri): ByteArray {
        val sourceBitmap = contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: throw IllegalArgumentException("선택한 이미지를 읽을 수 없어요.")

        val normalizedBitmap = sourceBitmap.centerCropAndResize(TARGET_SIZE)

        return try {
            ByteArrayOutputStream().use { out ->
                check(
                    normalizedBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        JPEG_QUALITY,
                        out
                    )
                ) { "이미지 인코딩에 실패했어요." }
                out.toByteArray()
            }
        } finally {
            if (normalizedBitmap !== sourceBitmap) {
                sourceBitmap.recycle()
            }
            normalizedBitmap.recycle()
        }
    }

    private fun Bitmap.centerCropAndResize(targetSize: Int): Bitmap {
        val minSize = minOf(width, height)
        val offsetX = (width - minSize) / 2
        val offsetY = (height - minSize) / 2

        val squareBitmap = if (width == height) {
            this
        } else {
            Bitmap.createBitmap(this, offsetX, offsetY, minSize, minSize)
        }
        if (squareBitmap.width == targetSize && squareBitmap.height == targetSize) {
            return squareBitmap
        }

        val resizedBitmap = Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true)
        if (squareBitmap !== this && squareBitmap !== resizedBitmap) {
            squareBitmap.recycle()
        }
        return resizedBitmap
    }
}
