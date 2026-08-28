package com.example.data.vision

import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import org.json.JSONArray
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object FaceEmbeddingExtractor {

    /**
     * Extracts a normalized 128-dimensional facial embedding vector from ML Kit Face landmarks
     * and bitmap spatial intensity features.
     */
    fun extractEmbedding(face: Face, bitmap: Bitmap?): FloatArray {
        val embedding = FloatArray(128)
        val box = face.boundingBox

        val faceWidth = abs(box.width()).toFloat().coerceAtLeast(1.0f)
        val faceHeight = abs(box.height()).toFloat().coerceAtLeast(1.0f)
        val centerX = box.centerX().toFloat()
        val centerY = box.centerY().toFloat()

        // 1. Spatial landmark geometric ratio features (indices 0 to 39)
        val landmarks = listOf(
            face.getLandmark(FaceLandmark.LEFT_EYE),
            face.getLandmark(FaceLandmark.RIGHT_EYE),
            face.getLandmark(FaceLandmark.NOSE_BASE),
            face.getLandmark(FaceLandmark.MOUTH_LEFT),
            face.getLandmark(FaceLandmark.MOUTH_RIGHT),
            face.getLandmark(FaceLandmark.MOUTH_BOTTOM),
            face.getLandmark(FaceLandmark.LEFT_EAR),
            face.getLandmark(FaceLandmark.RIGHT_EAR),
            face.getLandmark(FaceLandmark.LEFT_CHEEK),
            face.getLandmark(FaceLandmark.RIGHT_CHEEK)
        )

        var idx = 0
        for (i in landmarks.indices) {
            val l1 = landmarks[i]
            if (l1 != null) {
                embedding[idx++] = (l1.position.x - centerX) / faceWidth
                embedding[idx++] = (l1.position.y - centerY) / faceHeight
            } else {
                embedding[idx++] = 0.0f
                embedding[idx++] = 0.0f
            }
        }

        // Pairwise landmark distance metrics (indices 20 to 39)
        for (i in landmarks.indices) {
            for (j in i + 1 until landmarks.size) {
                if (idx >= 40) break
                val l1 = landmarks[i]
                val l2 = landmarks[j]
                if (l1 != null && l2 != null) {
                    val dx = (l1.position.x - l2.position.x) / faceWidth
                    val dy = (l1.position.y - l2.position.y) / faceHeight
                    embedding[idx++] = sqrt(dx * dx + dy * dy)
                } else {
                    embedding[idx++] = 0.0f
                }
            }
        }

        // Euler Angles head pose rotation metrics (indices 40 to 42)
        embedding[40] = (face.headEulerAngleX ?: 0.0f) / 90.0f
        embedding[41] = (face.headEulerAngleY ?: 0.0f) / 90.0f
        embedding[42] = (face.headEulerAngleZ ?: 0.0f) / 90.0f

        // Facial Classification Features (indices 43 to 45)
        embedding[43] = face.smilingProbability ?: 0.5f
        embedding[44] = face.leftEyeOpenProbability ?: 0.5f
        embedding[45] = face.rightEyeOpenProbability ?: 0.5f

        // 2. Image Bitmap Pixel Spatial Features (indices 46 to 127)
        if (bitmap != null && !bitmap.isRecycled) {
            try {
                // Crop face area
                val startX = box.left.coerceIn(0, bitmap.width - 1)
                val startY = box.top.coerceIn(0, bitmap.height - 1)
                val width = box.width().coerceAtMost(bitmap.width - startX).coerceAtLeast(1)
                val height = box.height().coerceAtMost(bitmap.height - startY).coerceAtLeast(1)

                val faceCrop = Bitmap.createBitmap(bitmap, startX, startY, width, height)
                val scaled = Bitmap.createScaledBitmap(faceCrop, 9, 9, true)

                var featIdx = 46
                for (x in 0 until 9) {
                    for (y in 0 until 9) {
                        if (featIdx >= 128) break
                        val pixel = scaled.getPixel(x, y)
                        val r = (pixel shr 16 and 0xFF) / 255.0f
                        val g = (pixel shr 8 and 0xFF) / 255.0f
                        val b = (pixel and 0xFF) / 255.0f
                        val gray = 0.299f * r + 0.587f * g + 0.114f * b
                        embedding[featIdx++] = gray
                    }
                }
            } catch (e: Exception) {
                // Fallback deterministic fill for unreadable crop
                for (i in 46 until 128) {
                    embedding[i] = (i * 0.0137f) % 1.0f
                }
            }
        } else {
            for (i in 46 until 128) {
                embedding[i] = (i * 0.0137f) % 1.0f
            }
        }

        return normalize(embedding)
    }

    /**
     * L2 Normalization of feature vector
     */
    fun normalize(vector: FloatArray): FloatArray {
        var normSq = 0.0f
        for (v in vector) {
            normSq += v * v
        }
        val norm = sqrt(normSq.coerceAtLeast(1e-6f))
        for (i in vector.indices) {
            vector[i] /= norm
        }
        return vector
    }

    /**
     * Computes Euclidean Distance between two 128-d vectors
     */
    fun euclideanDistance(v1: FloatArray, v2: FloatArray): Float {
        var sum = 0.0f
        val len = minOf(v1.size, v2.size)
        for (i in 0 until len) {
            val diff = v1[i] - v2[i]
            sum += diff * diff
        }
        return sqrt(sum)
    }

    /**
     * Computes Cosine Similarity between two 128-d vectors
     */
    fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dot = 0.0f
        var norm1 = 0.0f
        var norm2 = 0.0f
        val len = minOf(v1.size, v2.size)
        for (i in 0 until len) {
            dot += v1[i] * v2[i]
            norm1 += v1[i] * v1[i]
            norm2 += v2[i] * v2[i]
        }
        val denom = sqrt(norm1) * sqrt(norm2)
        return if (denom == 0.0f) 0.0f else dot / denom
    }

    fun toJson(vector: FloatArray): String {
        val jsonArray = JSONArray()
        for (v in vector) {
            jsonArray.put(v.toDouble())
        }
        return jsonArray.toString()
    }

    fun fromJson(jsonStr: String): FloatArray {
        return try {
            val jsonArray = JSONArray(jsonStr)
            val result = FloatArray(jsonArray.length())
            for (i in 0 until jsonArray.length()) {
                result[i] = jsonArray.getDouble(i).toFloat()
            }
            result
        } catch (e: Exception) {
            FloatArray(128)
        }
    }
}
