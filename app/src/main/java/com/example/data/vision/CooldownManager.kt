package com.example.data.vision

import java.util.concurrent.ConcurrentHashMap

class CooldownManager {
    // Maps key "$studentId:$sessionId" -> timestamp in millis
    private val checkInMap = ConcurrentHashMap<String, Long>()

    fun canCheckIn(studentId: String, sessionId: String, cooldownMinutes: Int): Boolean {
        val key = "$studentId:$sessionId"
        val lastTime = checkInMap[key] ?: return true
        val elapsedMs = System.currentTimeMillis() - lastTime
        val cooldownMs = cooldownMinutes * 60 * 1000L
        return elapsedMs >= cooldownMs
    }

    fun recordCheckIn(studentId: String, sessionId: String) {
        val key = "$studentId:$sessionId"
        checkInMap[key] = System.currentTimeMillis()
    }

    fun getRemainingCooldownSeconds(studentId: String, sessionId: String, cooldownMinutes: Int): Int {
        val key = "$studentId:$sessionId"
        val lastTime = checkInMap[key] ?: return 0
        val elapsedMs = System.currentTimeMillis() - lastTime
        val cooldownMs = cooldownMinutes * 60 * 1000L
        val remainingMs = cooldownMs - elapsedMs
        return if (remainingMs > 0) (remainingMs / 1000).toInt() else 0
    }

    fun clear() {
        checkInMap.clear()
    }
}
