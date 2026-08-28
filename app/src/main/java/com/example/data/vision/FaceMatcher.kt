package com.example.data.vision

import com.example.data.models.FaceEmbedding

data class MatchResult(
    val studentId: String,
    val distance: Float,
    val confidence: Float
)

object FaceMatcher {

    /**
     * Matches query embedding against list of stored FaceEmbedding entities.
     * Returns best match if distance < threshold.
     */
    fun findBestMatch(
        queryEmbedding: FloatArray,
        knownEmbeddings: List<FaceEmbedding>,
        threshold: Float = 0.55f
    ): MatchResult? {
        if (knownEmbeddings.isEmpty()) return null

        // Group embeddings by student ID
        val studentEmbeddings = knownEmbeddings.groupBy { it.studentId }

        var bestStudentId: String? = null
        var minDistance = Float.MAX_VALUE

        for ((studentId, embeddings) in studentEmbeddings) {
            for (embEntity in embeddings) {
                val storedVector = FaceEmbeddingExtractor.fromJson(embEntity.embeddingJson)
                val dist = FaceEmbeddingExtractor.euclideanDistance(queryEmbedding, storedVector)
                if (dist < minDistance) {
                    minDistance = dist
                    bestStudentId = studentId
                }
            }
        }

        return if (bestStudentId != null && minDistance < threshold) {
            val confidence = (1.0f - (minDistance / threshold)).coerceIn(0.0f, 1.0f)
            MatchResult(
                studentId = bestStudentId,
                distance = minDistance,
                confidence = confidence
            )
        } else {
            null
        }
    }
}
