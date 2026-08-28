package com.example.data.vision

import com.google.mlkit.vision.face.Face

enum class LivenessStatus {
    UNKNOWN,
    WAITING_FOR_BLINK,
    BLINK_DETECTED,
    VERIFIED_LIVE,
    SPOOF_SUSPECTED
}

class LivenessDetector {
    private var lastEyeClosedTime: Long = 0
    private var hasClosedEyes = false
    private var isVerified = false
    private var frameCount = 0
    private var totalMovement = 0.0f
    private var lastHeadX = 0.0f
    private var lastHeadY = 0.0f

    fun processFace(face: Face): LivenessStatus {
        frameCount++

        val leftEyeOpen = face.leftEyeOpenProbability ?: -1.0f
        val rightEyeOpen = face.rightEyeOpenProbability ?: -1.0f

        val headX = face.headEulerAngleX ?: 0.0f
        val headY = face.headEulerAngleY ?: 0.0f

        if (frameCount > 1) {
            totalMovement += abs(headX - lastHeadX) + abs(headY - lastHeadY)
        }
        lastHeadX = headX
        lastHeadY = headY

        // If probabilities are available (e.g. ML Kit classification)
        if (leftEyeOpen >= 0.0f && rightEyeOpen >= 0.0f) {
            val avgEyeOpen = (leftEyeOpen + rightEyeOpen) / 2.0f

            // Eye closed threshold (< 0.25)
            if (avgEyeOpen < 0.25f) {
                hasClosedEyes = true
                lastEyeClosedTime = System.currentTimeMillis()
            }

            // Eye reopened (> 0.70) within 1.5 seconds of blink
            if (hasClosedEyes && avgEyeOpen > 0.70f) {
                val duration = System.currentTimeMillis() - lastEyeClosedTime
                if (duration in 80..1800) {
                    isVerified = true
                    return LivenessStatus.VERIFIED_LIVE
                }
            }
        }

        // Alternative organic micro-movement test for natural human face vs static photo
        if (frameCount >= 15 && totalMovement > 1.2f) {
            isVerified = true
            return LivenessStatus.VERIFIED_LIVE
        }

        return if (hasClosedEyes) {
            LivenessStatus.BLINK_DETECTED
        } else if (isVerified) {
            LivenessStatus.VERIFIED_LIVE
        } else {
            LivenessStatus.WAITING_FOR_BLINK
        }
    }

    fun isVerified(): Boolean = isVerified

    fun reset() {
        hasClosedEyes = false
        isVerified = false
        frameCount = 0
        totalMovement = 0.0f
        lastEyeClosedTime = 0
    }

    private fun abs(value: Float): Float = if (value < 0) -value else value
}
